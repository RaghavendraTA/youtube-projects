package com.temkarstudios.parkinglot.dto;

import com.temkarstudios.parkinglot.enums.VehicleType;
import java.io.Serializable;
import java.util.Date;

public class OccupiedVehicleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spotId;
    private VehicleType carType;
    private Date entryTime;

    public OccupiedVehicleDto() {
    }

    public OccupiedVehicleDto(Long spotId, VehicleType carType, Date entryTime) {
        this.spotId = spotId;
        this.carType = carType;
        this.entryTime = entryTime;
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public VehicleType getCarType() {
        return carType;
    }

    public void setCarType(VehicleType carType) {
        this.carType = carType;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }

    @Override
    public String toString() {
        return "OccupiedVehicleDto{" +
                "spotId=" + spotId +
                ", carType=" + carType +
                ", entryTime=" + entryTime +
                '}';
    }
}
