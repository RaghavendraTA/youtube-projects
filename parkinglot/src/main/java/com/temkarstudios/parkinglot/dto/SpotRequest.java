package com.temkarstudios.parkinglot.dto;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;

public class SpotRequest {

    private Long spotId;
    private float price;
    private float peakPrice;
    private ParkingSpotType spotType;

    public SpotRequest() {}

    public SpotRequest(Long spotId, float price, float peakPrice, ParkingSpotType spotType) {
        this.spotId = spotId;
        this.price = price;
        this.peakPrice = peakPrice;
        this.spotType = spotType;
    }

    public Long getSpotId() {
        return spotId;
    }

    public float getPrice() {
        return price;
    }

    public float getPeakPrice() {
        return peakPrice;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setPeakPrice(float peakPrice) {
        this.peakPrice = peakPrice;
    }

    public ParkingSpotType getSpotType() {
        return spotType;
    }

    public void setSpotType(ParkingSpotType spotType) {
        this.spotType = spotType;
    }
}
