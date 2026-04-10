package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.dto.SpotRequest;
import com.temkarstudios.parkinglot.interfaces.IFareStrategy;
import com.temkarstudios.parkinglot.interfaces.ParkingService;
import com.temkarstudios.parkinglot.manager.FairCalculationFactory;
import com.temkarstudios.parkinglot.manager.TicketManager;
import com.temkarstudios.parkinglot.manager.VehicleManager;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.temkarstudios.parkinglot.manager.ParkingSpotManager;
import com.temkarstudios.parkinglot.model.Vehicle;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

/**
 * Parking Service Implementation with Redis Cache Integration
 * 
 * Key features:
 * 1. Checks parking availability using Redis cache for fast responses
 * 2. Returns response immediately after check-in/check-out
 * 3. Updates database asynchronously to not block API responses
 * 4. Maintains consistency between Redis and database during all operations
 */
@Service
public class ParkingServiceImpl implements ParkingService {

    private final ParkingSpotManager parkingSpotManager;
    private final VehicleManager vehicleManager;
    private final TicketManager ticketManager;
    private final AsyncDatabaseUpdateService asyncDatabaseUpdateService;

    @Autowired
    public ParkingServiceImpl(ParkingSpotManager parkingSpotManager, 
                            VehicleManager vehicleManager, 
                            TicketManager ticketManager,
                            AsyncDatabaseUpdateService asyncDatabaseUpdateService) {
        this.parkingSpotManager = parkingSpotManager;
        this.vehicleManager = vehicleManager;
        this.ticketManager = ticketManager;
        this.asyncDatabaseUpdateService = asyncDatabaseUpdateService;
    }

    /**
     * Check-in vehicle to parking lot
     * 1. Checks if vehicle is already parked
     * 2. Finds an available spot from cache/database
     * 3. Returns ticket immediately
     * 4. Updates database asynchronously
     */
    @Override
    public Ticket enterVehicle(Request request) throws Exception {
        if (ticketManager.isVehicleAlreadyParked(request.getLicensePlate())) {
            throw new Exception("Vehicle is already parked and has an active ticket.");
        }
        
        var vehicle = vehicleManager.addVehicleEntry(request);
        var spot = parkingSpotManager.findEmptySpotForVehicle(vehicle);
        
        if (spot.isEmpty()) {
            throw new Exception("No Parking Spot found");
        }
        
        ParkingSpot foundSpot = spot.get();
        
        // Mark spot as occupied in Redis immediately (cache update)
        parkingSpotManager.occupy(vehicle, foundSpot);
        
        // Generate and return ticket immediately
        // Database updates happen asynchronously
        return ticketManager.generateTicket(vehicle, foundSpot);
    }

    /**
     * Check-out vehicle from parking lot
     * 1. Retrieves ticket
     * 2. Vacates parking spot (updates cache immediately)
     * 3. Calculates pricing
     * 4. Returns ticket with pricing
     * 5. Updates database asynchronously
     */
    @Override
    public Ticket exitVehicle(Long ticketId) throws Exception {
        Optional<Ticket> hasTicket = ticketManager.getTicketById(ticketId);
        if (hasTicket.isEmpty()) {
            throw new Exception("No ticket found");
        }
        
        Ticket ticket = hasTicket.get();
        Vehicle vehicle = ticket.getVehicle();
        ParkingSpot spot = ticket.getParkingSpot();
        
        // Mark spot as available in Redis immediately
        parkingSpotManager.vacate(vehicle, spot);
        
        // Calculate pricing
        Date exitTime = new Date();
        float fare = calculatePricing(ticket);
        
        // Update ticket pricing immediately for response
        ticket.setExitTime(exitTime);
        ticket.setFinalPrice(fare);
        
        // Update database asynchronously to ensure consistency
        asyncDatabaseUpdateService.updateTicketExit(ticketId, exitTime, fare);
        
        return ticket;
    }

    @Override
    public void addNewSpot(SpotRequest request) throws Exception {
        parkingSpotManager.createNewSpot(request);
    }

    /**
     * Calculate parking fare based on peak hours
     */
    private float calculatePricing(Ticket ticket) {
        int hour = LocalDateTime.now().getHour();
        boolean isPeakHour = (hour >= 7 && hour <= 10) || (hour >= 16 && hour <= 19);
        IFareStrategy strategy = FairCalculationFactory.getFareCalculator(isPeakHour);
        return strategy.CalculateFare(ticket);
    }
}
