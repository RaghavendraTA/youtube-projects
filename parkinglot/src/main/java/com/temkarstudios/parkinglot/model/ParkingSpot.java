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

    private float peakPrice;

    public ParkingSpot() {
    }

    public ParkingSpot(Long id, ParkingSpotType type, float price, float peakPrice) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.peakPrice = peakPrice;
        this.isAvailable = false;
        this.vehicle = null;
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

    public float getPeakPrice() { return peakPrice; }

    public void setPeakPrice(float peakPrice) {
        this.peakPrice = peakPrice;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private boolean isAvailable;
        private Vehicle vehicle;
        private ParkingSpotType type;
        private float price;
        private float peakPrice;

        Builder() {
            this.isAvailable = false;
            this.vehicle = null;
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setParkingSpotType(ParkingSpotType type) {
            this.type = type;
            return this;
        }

        public Builder setPrice(float price) {
            this.price = price;
            return this;
        }

        public Builder setPeakPrice(float peakPrice) {
            this.peakPrice = peakPrice;
            return this;
        }

        public ParkingSpot build() {
            return new ParkingSpot(id, type, price, peakPrice);
        }
    }
}
