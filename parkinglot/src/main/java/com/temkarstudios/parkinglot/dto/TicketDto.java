package com.temkarstudios.parkinglot.dto;

import com.temkarstudios.parkinglot.model.Ticket;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketDto {

    private Long TicketId;
    private String vehicleNo;
    private Long parkingSpotNo;
    private Date entryTime;
    private Date exitTime;
    private Float price;

    private String message;
    private boolean hasError;

    public TicketDto(Ticket ticket) {
        this.TicketId = ticket.getId();
        this.vehicleNo = ticket.getVehicle().getLicensePlate();
        this.parkingSpotNo = ticket.getParkingSpot().getId();
        this.entryTime = ticket.getEntryTime();
        this.exitTime = ticket.getExitTime();
        this.price = ticket.getFinalPrice();

        this.hasError = false;
        this.message = "";
    }

    public TicketDto(String message) {
        this.hasError = true;
        this.message = "Failed to generate a ticket: " + message;
    }

    public static TicketDto getTicketDto(Ticket ticket) {
        return new TicketDto(ticket);
    }

    public static TicketDto getFailedTicketDto(String message) {
        return new TicketDto(message);
    }
}
