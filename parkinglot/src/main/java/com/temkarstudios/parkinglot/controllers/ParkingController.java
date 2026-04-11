package com.temkarstudios.parkinglot.controllers;

import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.dto.SpotRequest;
import com.temkarstudios.parkinglot.dto.TicketDto;
import com.temkarstudios.parkinglot.interfaces.ParkingService;
import com.temkarstudios.parkinglot.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ParkingController {

    private final ParkingService parkingService;

    @Autowired
    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }
    
    @PutMapping(path = "/checkin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TicketDto> checkIn(@RequestBody Request request) {
        log.info("Check-in request received for license plate {}", request.getLicensePlate());
        try {
            Ticket ticket = parkingService.enterVehicle(request);
            return ResponseEntity.ok(TicketDto.getTicketDto(ticket));
        } catch (Exception e) {
            log.error("Check-in failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(TicketDto.getFailedTicketDto(e.getMessage()));
        }
    }

    @PutMapping(path = "/checkout/{ticketId}")
    public ResponseEntity<TicketDto> checkOut(@PathVariable Long ticketId) {
        log.info("Check-out request received for ticket {}", ticketId);
        try {
            Ticket ticket = parkingService.exitVehicle(ticketId);
            return ResponseEntity.ok(TicketDto.getTicketDto(ticket));
        } catch (Exception e) {
            log.error("Check-out failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(TicketDto.getFailedTicketDto(e.getMessage()));
        }
    }

    @PostMapping(path = "/addSpot")
    public ResponseEntity<?> addNewParkingSpot(@RequestBody SpotRequest request) {
        log.info("Add new parking spot request: type {}", request.getSpotType());
        try {
            parkingService.addNewSpot(request);
            return ResponseEntity.ok("Spot is successfully created");
        } catch (Exception e) {
            log.error("Add spot failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
