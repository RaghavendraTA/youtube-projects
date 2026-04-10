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

@RestController
@RequestMapping("/api/v1")
public class ParkingController {

    private final ParkingService parkingService;

    @Autowired
    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }
    
    @PutMapping(path = "/checkin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TicketDto> checkIn(@RequestBody Request request) {
        try {
            Ticket ticket = parkingService.enterVehicle(request);
            return ResponseEntity.ok(TicketDto.getTicketDto(ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(TicketDto.getFailedTicketDto(e.getMessage()));
        }
    }

    @PutMapping(path = "/checkout/{ticketId}")
    public ResponseEntity<TicketDto> checkOut(@PathVariable Long ticketId) {
        try {
            Ticket ticket = parkingService.exitVehicle(ticketId);
            return ResponseEntity.ok(TicketDto.getTicketDto(ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(TicketDto.getFailedTicketDto(e.getMessage()));
        }
    }

    @PostMapping(path = "/addSpot")
    public ResponseEntity<?> addNewParkingSpot(@RequestBody SpotRequest request) {
        try {
            parkingService.addNewSpot(request);
            return ResponseEntity.ok("Spot is successfully created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
