package com.temkarstudios.parkinglot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    
    Optional<ParkingSpot> findFirstByVehicleIsNullAndType(ParkingSpotType type);
}
