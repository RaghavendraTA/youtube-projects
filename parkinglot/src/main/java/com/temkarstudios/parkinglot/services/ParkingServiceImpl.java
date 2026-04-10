package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.dto.OccupiedVehicleDto;
import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.dto.SpotRequest;
import com.temkarstudios.parkinglot.interfaces.IFareStrategy;
import com.temkarstudios.parkinglot.interfaces.ParkingService;
import com.temkarstudios.parkinglot.manager.FairCalculationFactory;
import com.temkarstudios.parkinglot.manager.TicketManager;
import com.temkarstudios.parkinglot.manager.VehicleManager;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Ticket;
import com.temkarstudios.parkinglot.enums.VehicleType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.temkarstudios.parkinglot.manager.ParkingSpotManager;
import com.temkarstudios.parkinglot.model.Vehicle;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingServiceImpl implements ParkingService {

    private final ParkingSpotManager parkingSpotManager;
    private final VehicleManager vehicleManager;
    private final TicketManager ticketManager;
    private final RedisCacheService redisCacheService;
    private final AsyncDatabaseUpdateService asyncDatabaseUpdateService;

    @Autowired
    public ParkingServiceImpl(ParkingSpotManager parkingSpotManager, VehicleManager vehicleManager, 
                           TicketManager ticketManager, RedisCacheService redisCacheService,
                           AsyncDatabaseUpdateService asyncDatabaseUpdateService) {
        this.parkingSpotManager = parkingSpotManager;
        this.vehicleManager = vehicleManager;
        this.ticketManager = ticketManager;
        this.redisCacheService = redisCacheService;
        this.asyncDatabaseUpdateService = asyncDatabaseUpdateService;
    }

    /**
     * Vehicle Entry Flow with Redis Cache:
     * 1. Check if vehicle has spot available using Redis key: vehicleType
     * 2. If Redis says not available, fallback to DB to re-confirm
     * 3. Once vehicle enters:
     *    - Generate a ticket
     *    - Update Redis (available spots and occupied keys)
     *    - Issue ticket (return API response)
     *    - Update DB in @Async mode
     */
    public Ticket enterVehicle(Request request) throws Exception {
        // Step 1: Check Redis for available spots
        List<Long> availableSpotsFromRedis = redisCacheService.getAvailableSpotsForVehicleType(vehicleType);

        if (availableSpotsFromRedis.isEmpty()) {
            throw new Exception("No Parking Spot found");
        }

        var vehicle = vehicleManager.addVehicleEntry(request);
        VehicleType vehicleType = vehicle.getType();

        ParkingSpot spot = null;

        if (!availableSpotsFromRedis.isEmpty()) {
            // Try to use the first available spot from Redis
            Long spotId = availableSpotsFromRedis.get(0);
            Optional<ParkingSpot> spotFromDb = parkingSpotManager.findSpotById(spotId);
            if (spotFromDb.isPresent() && spotFromDb.get().isAvailable()) {
                spot = spotFromDb.get();
            } else {
                // Redis cache is outdated, remove the stale entry
                redisCacheService.removeAvailableSpot(vehicleType, spotId);
            }
        }

        // Step 2: If Redis doesn't have available spot or it was outdated, fallback to DB
        if (spot == null) {
            Optional<ParkingSpot> dbSpot = parkingSpotManager.findEmptySpotForVehicle(vehicle);
            if (dbSpot.isEmpty()) {
                throw new Exception("No Parking Spot found");
            }
            spot = dbSpot.get();
        }

        // Step 4: Generate ticket
        Ticket ticket = ticketManager.generateTicket(vehicle, spot);

        // Step 5: Update Redis immediately (before returning response)
        // Remove from available spots
        redisCacheService.removeAvailableSpot(vehicleType, spot.getId());

        // Add to occupied vehicles cache
        OccupiedVehicleDto occupiedInfo = new OccupiedVehicleDto(
                spot.getId(),
                vehicle.getType(),
                new Date()
        );
        redisCacheService.addOccupiedVehicle(vehicle.getLicensePlate(), occupiedInfo);

        // Step 6: Async update DB (doesn't block API response)
        asyncDatabaseUpdateService.updateDatabaseAfterVehicleEntry(vehicle, spot, ticket);

        return ticket;
    }

    /**
     * Vehicle Exit Flow with Redis Cache:
     * 1. Check Redis to get vehicle type, entry time, spot type
     * 2. Compute fare based on strategy
     * 3. Update Redis by removing the cache key for vehicle
     * 4. Update available spot array in Redis with spotId
     * 5. Return the ticket
     * 6. Update DB in @Async mode
     */
    public Ticket exitVehicle(Long ticketId) throws Exception {
        Optional<Ticket> hasTicket = ticketManager.getTicketById(ticketId);
        if (hasTicket.isEmpty()) {
            throw new Exception("No ticket found");
        }

        Ticket ticket = hasTicket.get();
        Vehicle vehicle = ticket.getVehicle();
        ParkingSpot spot = ticket.getParkingSpot();

        // Step 3: Compute pricing
        float finalFare = computePricing(ticket);
        
        ticket.setExitTime(new Date());
        ticket.setFinalPrice(finalFare);

        // Step 4: Update Redis
        // Remove occupied vehicle entry
        redisCacheService.removeOccupiedVehicle(vehicle.getLicensePlate());

        // Add spot back to available spots
        redisCacheService.addAvailableSpot(vehicle.getType(), spot.getId());

        // Step 5: Async update DB (doesn't block API response)
        asyncDatabaseUpdateService.updateDatabaseAfterVehicleExit(vehicle, spot, ticket);

        return ticket;
    }

    @Override
    public void addNewSpot(SpotRequest request) throws Exception {
        ParkingSpot spot = parkingSpotManager.createNewSpot(request);
        // Add new spot to Redis available spots cache
        redisCacheService.addAvailableSpot(request.getSpotType().toVehicleType(), spot.getId());
    }

    private float computePricing(Ticket ticket) {
        int hour = LocalDateTime.now().getHour();
        boolean isPeakHour = (hour >= 7 && hour <= 10) || (hour >= 16 && hour <= 19);
        IFareStrategy strategy = FairCalculationFactory.getFareCalculator(isPeakHour);
        return strategy.CalculateFare(ticket);
    }

}
