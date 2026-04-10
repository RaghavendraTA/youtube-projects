package com.temkarstudios.parkinglot.manager;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.enums.VehicleSize;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Vehicle;
import com.temkarstudios.parkinglot.repository.ParkingSpotRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParkingSpotManager {

    @Autowired
    private ParkingSpotRepository repository;

    public Optional<ParkingSpot> findEmptySpotForVehicle(Vehicle vehicle) {
        return this.repository.findFirstByVehicleIsNullAndType(convertSizeToType(vehicle.getSize()));
    }

    public void occupy(Vehicle vehicle, ParkingSpot spot) {
        spot.setLicensePlate(vehicle);
        spot.setSpotAvailability(false);
        this.repository.saveAndFlush(spot);
    }

    public void vacate(Vehicle vehicle, ParkingSpot spot) {
        spot.setLicensePlate(null);
        spot.setSpotAvailability(true);
        this.repository.saveAndFlush(spot);
    }

    private ParkingSpotType convertSizeToType(VehicleSize size) {
        return switch(size) {
            case SMALL -> ParkingSpotType.COMPACT;
            case MEDIUM -> ParkingSpotType.REGULAR;
            case LARGE -> ParkingSpotType.OVERSIZED;
        };
    }
}
