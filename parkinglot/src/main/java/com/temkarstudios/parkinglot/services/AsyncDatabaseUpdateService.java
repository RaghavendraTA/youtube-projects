package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.manager.ParkingSpotManager;
import com.temkarstudios.parkinglot.manager.TicketManager;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Ticket;
import com.temkarstudios.parkinglot.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncDatabaseUpdateService {

    @Autowired
    private ParkingSpotManager parkingSpotManager;

    @Autowired
    private TicketManager ticketManager;

    /**
     * Asynchronously update vehicle and parking spot in database after check-in
     * This prevents blocking the API response
     */
    @Async
    public void updateDatabaseAfterVehicleEntry(Vehicle vehicle, ParkingSpot parkingSpot, Ticket ticket) {
        try {
            parkingSpotManager.occupy(vehicle, parkingSpot);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.err.println("Error during async database update after entry: " + e.getMessage());
        }
    }

    /**
     * Asynchronously update vehicle, parking spot, and ticket in database after check-out
     * This prevents blocking the API response
     */
    @Async
    public void updateDatabaseAfterVehicleExit(Vehicle vehicle, ParkingSpot parkingSpot, Ticket ticket) {
        try {
            // Step 2: Vacate the spot in DB
            parkingSpotManager.vacate(vehicle, parkingSpot);
            ticketManager.updatePrice(ticket, ticket.getFinalPrice());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            System.err.println("Error during async database update after exit: " + e.getMessage());
        }
    }
}
