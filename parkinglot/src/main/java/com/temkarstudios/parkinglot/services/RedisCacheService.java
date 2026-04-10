package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.dto.OccupiedVehicleDto;
import com.temkarstudios.parkinglot.enums.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String AVAILABLE_SPOTS_PREFIX = "available_spots:";
    private static final String OCCUPIED_VEHICLE_PREFIX = "occupied_vehicle:";

    /**
     * Get available spot IDs for a specific vehicle type
     * Key: available_spots:{vehicleType}
     * Value: [spotId1, spotId2, ...]
     */
    public List<Long> getAvailableSpotsForVehicleType(VehicleType vehicleType) {
        try {
            Set<Object> spots = redisTemplate.opsForSet().members(AVAILABLE_SPOTS_PREFIX + vehicleType.name());
            if (spots != null && !spots.isEmpty()) {
                return new ArrayList<>(spots.stream()
                        .map(spot -> Long.valueOf(spot.toString()))
                        .toList());
            }
        } catch (Exception e) {
            // If Redis fails, return empty list to fallback to DB
            System.err.println("Error fetching available spots from Redis: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Add a spot ID to available spots for a vehicle type
     */
    public void addAvailableSpot(VehicleType vehicleType, Long spotId) {
        try {
            redisTemplate.opsForSet().add(AVAILABLE_SPOTS_PREFIX + vehicleType.name(), spotId);
        } catch (Exception e) {
            System.err.println("Error adding available spot to Redis: " + e.getMessage());
        }
    }

    /**
     * Remove a spot ID from available spots for a vehicle type
     */
    public void removeAvailableSpot(VehicleType vehicleType, Long spotId) {
        try {
            redisTemplate.opsForSet().remove(AVAILABLE_SPOTS_PREFIX + vehicleType.name(), spotId);
        } catch (Exception e) {
            System.err.println("Error removing available spot from Redis: " + e.getMessage());
        }
    }

    /**
     * Get occupied vehicle info by license plate
     * Key: occupied_vehicle:{licensePlate}
     * Value: { spotId, carType, entryTime }
     */
    public OccupiedVehicleDto getOccupiedVehicleInfo(String licensePlate) {
        try {
            Object occupiedInfo = redisTemplate.opsForValue().get(OCCUPIED_VEHICLE_PREFIX + licensePlate);
            if (occupiedInfo != null) {
                return (OccupiedVehicleDto) occupiedInfo;
            }
        } catch (Exception e) {
            System.err.println("Error fetching occupied vehicle info from Redis: " + e.getMessage());
        }
        return null;
    }

    /**
     * Add occupied vehicle info to Redis
     */
    public void addOccupiedVehicle(String licensePlate, OccupiedVehicleDto occupiedInfo) {
        try {
            redisTemplate.opsForValue().set(OCCUPIED_VEHICLE_PREFIX + licensePlate, occupiedInfo);
        } catch (Exception e) {
            System.err.println("Error adding occupied vehicle to Redis: " + e.getMessage());
        }
    }

    /**
     * Remove occupied vehicle info from Redis
     */
    public void removeOccupiedVehicle(String licensePlate) {
        try {
            redisTemplate.delete(OCCUPIED_VEHICLE_PREFIX + licensePlate);
        } catch (Exception e) {
            System.err.println("Error removing occupied vehicle from Redis: " + e.getMessage());
        }
    }

    /**
     * Initialize available spots for all vehicle types
     * This should be called on application startup to load all available spots from DB
     */
    public void initializeAvailableSpots(VehicleType vehicleType, List<Long> spotIds) {
        try {
            for (Long spotId : spotIds) {
                addAvailableSpot(vehicleType, spotId);
            }
        } catch (Exception e) {
            System.err.println("Error initializing available spots in Redis: " + e.getMessage());
        }
    }

    /**
     * Check if spot is available in cache (using presence in available spots set)
     */
    public boolean isSpotAvailableInCache(VehicleType vehicleType, Long spotId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet()
                    .isMember(AVAILABLE_SPOTS_PREFIX + vehicleType.name(), spotId));
        } catch (Exception e) {
            System.err.println("Error checking spot availability in Redis: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clear all cache (for testing or reset purposes)
     */
    public void clearAllCache() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            System.err.println("Error clearing Redis cache: " + e.getMessage());
        }
    }
}
