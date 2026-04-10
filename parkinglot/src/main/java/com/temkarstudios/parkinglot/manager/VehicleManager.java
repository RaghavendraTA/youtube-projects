package com.temkarstudios.parkinglot.manager;

import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.model.Vehicle;
import com.temkarstudios.parkinglot.repository.VehicleRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleManager {

    @Autowired
    private VehicleRepository repository;

    public Vehicle addVehicleEntry(Request request) {
        Optional<Vehicle> vehicle = repository.findById(request.getLicensePlate());
        if (!vehicle.isEmpty()) {
            return vehicle.get();
        }

        Vehicle newVehicle = Vehicle.builder()
                .setLicensePlate(request.getLicensePlate())
                .setVehicleType(request.getVehicleType())
                .build();

        return repository.saveAndFlush(newVehicle);
    }
}
