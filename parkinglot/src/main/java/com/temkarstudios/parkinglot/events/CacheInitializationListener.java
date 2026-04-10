package com.temkarstudios.parkinglot.events;

import com.temkarstudios.parkinglot.enums.VehicleType;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.repository.ParkingSpotRepository;
import com.temkarstudios.parkinglot.services.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CacheInitializationListener {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    /**
     * Initialize Redis cache with all available parking spots from the database
     * This runs once when the application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeRedisCache() {
        System.out.println("Initializing Redis cache with available parking spots...");

        try {
            // Get all available spots from database
            List<ParkingSpot> availableSpots = parkingSpotRepository.findAll()
                    .stream()
                    .filter(ParkingSpot::isAvailable)
                    .toList();

            // Group spots by vehicle type
            Map<VehicleType, List<Long>> spotsByVehicleType = new HashMap<>();
            for (VehicleType vehicleType : VehicleType.values()) {
                spotsByVehicleType.put(vehicleType, new java.util.ArrayList<>());
            }

            // Populate the map
            for (ParkingSpot spot : availableSpots) {
                VehicleType vehicleType = spot.getType().toVehicleType();
                spotsByVehicleType.get(vehicleType).add(spot.getId());
            }

            // Initialize Redis cache for each vehicle type
            for (Map.Entry<VehicleType, List<Long>> entry : spotsByVehicleType.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    redisCacheService.initializeAvailableSpots(entry.getKey(), entry.getValue());
                    System.out.println("Initialized " + entry.getValue().size() + " spots for " + entry.getKey());
                }
            }

            System.out.println("Redis cache initialization completed successfully!");
        } catch (Exception e) {
            System.err.println("Error initializing Redis cache: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
