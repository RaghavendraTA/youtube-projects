package com.temkarstudios.parkinglot.model;

import com.temkarstudios.parkinglot.enums.VehicleSize;
import com.temkarstudios.parkinglot.enums.VehicleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class Vehicle {

    @Id
    @Column(name = "license_plate")
    private String licensePlate;

    @Enumerated(EnumType.ORDINAL)
    private VehicleSize size;

    @Enumerated(EnumType.ORDINAL)
    private VehicleType type;

    public Vehicle() {
    }

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.size = type.getSize();
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleSize getSize() {
        return size;
    }

    public VehicleType getType() {
        return type;
    }

    public static VehicleBuilder builder() {
        return new VehicleBuilder();
    }

    public static class VehicleBuilder {

        private String licensePlate;
        private VehicleType type;

        public VehicleBuilder setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
            return this;
        }

        public VehicleBuilder setVehicleType(String vehicleType) {
            this.type = VehicleType.valueOf(vehicleType);
            return this;
        }

        public Vehicle build() {
            return new Vehicle(licensePlate, type);
        }
    }
}