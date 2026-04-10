package com.temkarstudios.parkinglot.enums;

public enum VehicleType {
    CAR(VehicleSize.MEDIUM),
    TRUCK(VehicleSize.LARGE),
    MOTORCYCLE(VehicleSize.SMALL);

    private VehicleSize size;

    VehicleType(VehicleSize size) {
        this.size = size;
    }

    public VehicleSize getSize() {
        return size;
    }
}
