package com.temkarstudios.parkinglot.model;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private boolean isAvailable;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "license_plate_no", referencedColumnName = "license_plate")
    private @Nullable Vehicle vehicle;

    @Enumerated(EnumType.ORDINAL)
    private ParkingSpotType type;

    private float price;

    public ParkingSpot() {
    }

    public Long getId() {
        return id;
    }

    public boolean isSpotAvailable() {
        return isAvailable;
    }

    public void setSpotAvailability(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setLicensePlate(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ParkingSpotType getType() {
        return type;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
