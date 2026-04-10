package com.temkarstudios.parkinglot.repository;

import com.temkarstudios.parkinglot.model.Ticket;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Optional<Ticket> findFirstByVehicleLicensePlateAndIsActiveTrueAndExitTimeIsNull(String licensePlate);
}
