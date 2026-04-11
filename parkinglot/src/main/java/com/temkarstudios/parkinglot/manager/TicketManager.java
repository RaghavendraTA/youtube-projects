package com.temkarstudios.parkinglot.manager;

import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Ticket;
import com.temkarstudios.parkinglot.model.Vehicle;
import com.temkarstudios.parkinglot.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class TicketManager {

    @Autowired
    private TicketRepository ticketRepository;

    public Ticket generateTicket(Vehicle vehicle, ParkingSpot spot) {
        var ticket = Ticket.builder()
                .vehicle(vehicle)
                .parkingSpot(spot)
                .entryTime(new Date())
                .build();
        return ticketRepository.saveAndFlush(ticket);
    }

    public Ticket updatePrice(Ticket ticket, float price) {
        ticket.setFinalPrice(price);
        return ticketRepository.saveAndFlush(ticket);
    }

    public boolean isVehicleAlreadyParked(String licensePlate) {
        return ticketRepository.findFirstByVehicleLicensePlateAndIsActiveTrueAndExitTimeIsNull(licensePlate).isPresent();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }
}
