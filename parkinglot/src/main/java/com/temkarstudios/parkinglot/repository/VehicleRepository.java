package com.temkarstudios.parkinglot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.temkarstudios.parkinglot.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    
}
