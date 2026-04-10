package com.temkarstudios.parkinglot.enums;

public enum ParkingSpotType {
    COMPACT,
    REGULAR,
    OVERSIZED;

    public VehicleType toVehicleType() {
        return switch(this) {
            case COMPACT -> VehicleType.MOTORCYCLE;
            case REGULAR -> VehicleType.CAR;
            case OVERSIZED -> VehicleType.TRUCK;
        };
    }
}
