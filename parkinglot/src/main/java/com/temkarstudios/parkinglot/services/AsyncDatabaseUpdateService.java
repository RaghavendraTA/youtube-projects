package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Ticket;
import com.temkarstudios.parkinglot.model.Vehicle;
import com.temkarstudios.parkinglot.repository.ParkingSpotRepository;
import com.temkarstudios.parkinglot.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * Service to handle asynchronous database updates
 * This allows the API to return responses quickly while syncing data to the database in background
 */
@Service
public class AsyncDatabaseUpdateService {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ParkingSpotCacheService cacheService;

    /**
     * Asynchronously update parking spot occupancy in the database
     * Called after a vehicle occupies a parking spot
     */
    @Async
    public void updateParkingSpotOccupancy(Long spotId, Vehicle vehicle, boolean isOccupied) {
        try {
            Optional<ParkingSpot> spotOptional = parkingSpotRepository.findById(spotId);
            if (spotOptional.isPresent()) {
                ParkingSpot spot = spotOptional.get();
                if (isOccupied) {
                    spot.setLicensePlate(vehicle);
                    spot.setSpotAvailability(false);
                } else {
                    spot.setLicensePlate(null);
                    spot.setSpotAvailability(true);
                }
                parkingSpotRepository.saveAndFlush(spot);
                // Invalidate cache after DB update to ensure consistency
                cacheService.invalidateParkingSpotCache(spotId);
            }
        } catch (Exception e) {
            System.err.println("Error updating parking spot occupancy in database: " + e.getMessage());
        }
    }

    /**
     * Asynchronously update ticket with exit time and pricing in the database
     * Called after a vehicle exits a parking spot
     */
    @Async
    public void updateTicketExit(Long ticketId, Date exitTime, float fare) {
        try {
            Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
            if (ticketOptional.isPresent()) {
                Ticket ticket = ticketOptional.get();
                ticket.setExitTime(exitTime);
                ticket.setFinalPrice(fare);
                ticketRepository.saveAndFlush(ticket);
            }
        } catch (Exception e) {
            System.err.println("Error updating ticket exit in database: " + e.getMessage());
        }
    }

    /**
     * Synchronize Redis cache with database
     * Used for consistency checks and periodic sync
     */
    @Async
    public void syncCacheWithDatabase(Long spotId, ParkingSpot spot) {
        try {
            if (spot != null) {
                cacheService.cacheParkingSpot(spot);
            } else {
                // If spot is null, invalidate cache
                cacheService.invalidateParkingSpotCache(spotId);
            }
        } catch (Exception e) {
            System.err.println("Error syncing cache with database: " + e.getMessage());
        }
    }

    /**
     * Batch synchronize multiple parking spots to ensure consistency
     */
    @Async
    public void batchSyncSpotsToCache(Iterable<ParkingSpot> spots) {
        try {
            for (ParkingSpot spot : spots) {
                cacheService.cacheParkingSpot(spot);
            }
        } catch (Exception e) {
            System.err.println("Error in batch sync: " + e.getMessage());
        }
    }
}
