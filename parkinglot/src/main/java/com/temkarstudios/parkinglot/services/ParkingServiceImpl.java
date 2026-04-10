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

@Service
public class ParkingServiceImpl implements ParkingService {

    private final ParkingSpotManager parkingSpotManager;
    private final VehicleManager vehicleManager;
    private final TicketManager ticketManager;

    @Autowired
    public ParkingServiceImpl(ParkingSpotManager parkingSpotManager, VehicleManager vehicleManager, TicketManager ticketManager) {
        this.parkingSpotManager = parkingSpotManager;
        this.vehicleManager = vehicleManager;
        this.ticketManager = ticketManager;
    }

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

    @Override
    public void addNewSpot(SpotRequest request) throws Exception {
        parkingSpotManager.createNewSpot(request);
    }

    private Ticket computePricing(Ticket ticket) {
        int hour = LocalDateTime.now().getHour();
        boolean isPeakHour = (hour >= 7 && hour <= 10) || (hour >= 16 && hour <= 19);
        IFareStrategy strategy = FairCalculationFactory.getFareCalculator(isPeakHour);
        float fare = strategy.CalculateFare(ticket);
        return ticketManager.updatePrice(ticket, fare);
    }

}
