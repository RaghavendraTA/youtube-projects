package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.interfaces.ParkingService;
import com.temkarstudios.parkinglot.manager.TicketManager;
import com.temkarstudios.parkinglot.manager.VehicleManager;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.temkarstudios.parkinglot.manager.ParkingSpotManager;
import com.temkarstudios.parkinglot.model.Vehicle;

import java.util.Date;
import java.util.Optional;

@Service
public class ParkingServiceImpl implements ParkingService {

    @Autowired
    private ParkingSpotManager parkingSpotManager;

    @Autowired
    private VehicleManager vehicleManager;

    @Autowired
    private TicketManager ticketManager;

    // Ticket should get generated
    public Ticket enterVehicle(Request request) throws Exception {
        if (ticketManager.isVehicleAlreadyParked(request.getLicensePlate())) {
            throw new Exception("Vehicle is already parked and has an active ticket.");
        }
        var vehicle = vehicleManager.addVehicleEntry(request);
        var spot = parkingSpotManager.findEmptySpotForVehicle(vehicle);
        if (spot.isEmpty()) {
            throw new Exception("No Parking Spot found");
        }
        parkingSpotManager.occupy(vehicle, spot.get());
        return ticketManager.generateTicket(vehicle, spot.get());
    }

    public Ticket exitVehicle(Long ticketId) throws Exception {
        Optional<Ticket> hasTicket = ticketManager.getTicketById(ticketId);
        if (hasTicket.isEmpty()) {
            throw new Exception("No ticket found");
        }
        Ticket ticket = hasTicket.get();
        Vehicle vehicle = ticket.getVehicle();
        ParkingSpot spot = ticket.getParkingSpot();
        parkingSpotManager.vacate(vehicle, spot);
        ticket.setExitTime(new Date());
        return computePricing(ticket);
    }

    private Ticket computePricing(Ticket ticket) {
        var diffTime = ticket.getExitTime().getTime() - ticket.getEntryTime().getTime();

        float absDiff = Math.abs(diffTime);
        var totalMinutes = Math.floor(absDiff / (1000 * 60));
        var hours = Math.floor(totalMinutes / 60);
        var minutes = totalMinutes % 60;

        float totalPrice = (float) (hours * 2 * ticket.getParkingSpot().getPrice());
        totalPrice += (minutes > 30 ? 2 : 1) * ticket.getParkingSpot().getPrice();

        return ticketManager.updatePrice(ticket, totalPrice);
    }

}
