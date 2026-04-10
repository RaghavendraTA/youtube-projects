package com.temkarstudios.parkinglot.manager;

import com.temkarstudios.parkinglot.dto.SpotRequest;
import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.enums.VehicleSize;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Vehicle;
import com.temkarstudios.parkinglot.repository.ParkingSpotRepository;
import com.temkarstudios.parkinglot.services.AsyncDatabaseUpdateService;
import com.temkarstudios.parkinglot.services.ParkingSpotCacheService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParkingSpotManager {

    @Autowired
    private ParkingSpotRepository repository;

    @Autowired
    private ParkingSpotCacheService cacheService;

    @Autowired
    private AsyncDatabaseUpdateService asyncDatabaseUpdateService;

    /**
     * Find an empty spot for a vehicle
     * First checks Redis cache for available spots, falls back to database if cache miss
     */
    public Optional<ParkingSpot> findEmptySpotForVehicle(Vehicle vehicle) {
        ParkingSpotType requiredType = convertSizeToType(vehicle.getSize());
        
        // Try to get from Redis cache first for faster response
        Optional<Long> cachedSpotId = cacheService.getAvailableSpotIdForType(requiredType);
        if (cachedSpotId.isPresent()) {
            // Try to get spot details from cache
            Optional<ParkingSpot> cachedSpot = cacheService.getCachedParkingSpot(cachedSpotId.get());
            if (cachedSpot.isPresent() && cachedSpot.get().isAvailable()) {
                return cachedSpot;
            }
        }
        
        // Fall back to database if cache miss or invalid
        Optional<ParkingSpot> dbSpot = this.repository.findFirstByVehicleIsNullAndType(requiredType);
        
        // Cache the result for future requests
        if (dbSpot.isPresent()) {
            cacheService.cacheParkingSpot(dbSpot.get());
            cacheService.addAvailableSpot(dbSpot.get().getId(), requiredType);
        }
        
        return dbSpot;
    }

    /**
     * Occupy a parking spot
     * Updates Redis cache immediately for quick response
     * Updates database asynchronously to not block the API response
     */
    public void occupy(Vehicle vehicle, ParkingSpot spot) {
        // Update Redis cache immediately
        cacheService.removeAvailableSpot(spot.getId(), spot.getType());
        
        // Update database asynchronously
        asyncDatabaseUpdateService.updateParkingSpotOccupancy(spot.getId(), vehicle, true);
    }

    /**
     * Vacate a parking spot
     * Updates Redis cache immediately for quick response
     * Updates database asynchronously to not block the API response
     */
    public void vacate(Vehicle vehicle, ParkingSpot spot) {
        // Update Redis cache immediately
        cacheService.addAvailableSpot(spot.getId(), spot.getType());
        
        // Update database asynchronously
        asyncDatabaseUpdateService.updateParkingSpotOccupancy(spot.getId(), null, false);
    }

    private ParkingSpotType convertSizeToType(VehicleSize size) {
        return switch(size) {
            case SMALL -> ParkingSpotType.COMPACT;
            case MEDIUM -> ParkingSpotType.REGULAR;
            case LARGE -> ParkingSpotType.OVERSIZED;
        };
    }

    public ParkingSpot createNewSpot(SpotRequest request) {
        ParkingSpot spot = ParkingSpot.builder()
                .setId(request.getSpotId())
                .setParkingSpotType(request.getSpotType())
                .setPrice(request.getPrice())
                .setPeakPrice(request.getPeakPrice())
                .build();
        ParkingSpot savedSpot = this.repository.saveAndFlush(spot);
        
        // Cache the newly created spot
        cacheService.cacheParkingSpot(savedSpot);
        cacheService.addAvailableSpot(savedSpot.getId(), savedSpot.getType());
        
        return savedSpot;
    }
}
