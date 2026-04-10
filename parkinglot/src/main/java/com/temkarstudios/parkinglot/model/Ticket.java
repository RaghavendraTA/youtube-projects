package com.temkarstudios.parkinglot.model;


import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "license_plate_no", referencedColumnName = "license_plate")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "parking_spot_no", referencedColumnName = "id")
    private ParkingSpot parkingSpot;

    private Date entryTime;
    private Date exitTime;

    private boolean isActive;
    private float finalPrice;

    public Ticket() {
    }

    public Ticket(Vehicle vehicle, ParkingSpot parkingSpot, Date entryTime) {
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.entryTime = entryTime;
        this.isActive = true;
    }

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public Date getExitTime() {
        return exitTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getFinalPrice() {
        return finalPrice;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }

    public void setExitTime(Date exitTime) {
        this.exitTime = exitTime;
    }

    public void setIsActive(boolean status) {
        this.isActive = status;
    }

    public void setFinalPrice(float price) {
        this.finalPrice = price;
    }

    // Builder code
    public static TicketBuilder builder() {
        return new TicketBuilder();
    }

    public static class TicketBuilder {

        private Vehicle vehicle;
        private ParkingSpot parkingSpot;
        private Date entryTime;

        public TicketBuilder setVehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public TicketBuilder setParkingSpot(ParkingSpot parkingSpot) {
            this.parkingSpot = parkingSpot;
            return this;
        }

        public TicketBuilder setEntryTime(Date entryTime) {
            this.entryTime = entryTime;
            return this;
        }

        public Ticket build() {
            return new Ticket(vehicle, parkingSpot, entryTime);
        }
    }
}
