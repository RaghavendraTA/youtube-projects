package com.temkarstudios.parkinglot.dto;

public class Request {

    private String licensePlate;
    private String vehicleType;

    public Request() {
    }

    public Request(String licensePlate, String vehicleType) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() {
        return this.licensePlate;
    }

    public String getVehicleType() {
        return this.vehicleType;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
