package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ParkingSpotCacheService {

    private static final String AVAILABLE_SPOTS_PREFIX = "parking:available:";
    private static final String SPOT_DETAILS_PREFIX = "parking:spot:";
    private static final String CACHE_EXPIRY_HOURS = "24";
    private static final long CACHE_EXPIRY = 24 * 60 * 60; // 24 hours in seconds

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisTemplate<String, ParkingSpot> parkingSpotRedisTemplate;

    /**
     * Get an available parking spot for a specific type from Redis cache
     */
    public Optional<Long> getAvailableSpotIdForType(ParkingSpotType spotType) {
        try {
            String key = AVAILABLE_SPOTS_PREFIX + spotType.name();
            Set<Object> availableSpots = redisTemplate.opsForSet().members(key);
            
            if (availableSpots != null && !availableSpots.isEmpty()) {
                Long spotId = (Long) availableSpots.stream().findFirst().orElse(null);
                return Optional.ofNullable(spotId);
            }
            return Optional.empty();
        } catch (Exception e) {
            // Log error and return empty - fall back to DB
            System.err.println("Error getting available spot from cache: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Add a parking spot to the available spots set in Redis
     */
    public void addAvailableSpot(Long spotId, ParkingSpotType spotType) {
        try {
            String key = AVAILABLE_SPOTS_PREFIX + spotType.name();
            redisTemplate.opsForSet().add(key, spotId);
            redisTemplate.expire(key, CACHE_EXPIRY, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Error adding spot to cache: " + e.getMessage());
        }
    }

    /**
     * Remove a parking spot from the available spots set in Redis
     */
    public void removeAvailableSpot(Long spotId, ParkingSpotType spotType) {
        try {
            String key = AVAILABLE_SPOTS_PREFIX + spotType.name();
            redisTemplate.opsForSet().remove(key, spotId);
        } catch (Exception e) {
            System.err.println("Error removing spot from cache: " + e.getMessage());
        }
    }

    /**
     * Cache parking spot details
     */
    public void cacheParkingSpot(ParkingSpot spot) {
        try {
            String key = SPOT_DETAILS_PREFIX + spot.getId();
            parkingSpotRedisTemplate.opsForValue().set(key, spot, CACHE_EXPIRY, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Error caching parking spot: " + e.getMessage());
        }
    }

    /**
     * Get cached parking spot details
     */
    public Optional<ParkingSpot> getCachedParkingSpot(Long spotId) {
        try {
            String key = SPOT_DETAILS_PREFIX + spotId;
            ParkingSpot spot = parkingSpotRedisTemplate.opsForValue().get(key);
            return Optional.ofNullable(spot);
        } catch (Exception e) {
            System.err.println("Error retrieving parking spot from cache: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Invalidate specific parking spot cache
     */
    public void invalidateParkingSpotCache(Long spotId) {
        try {
            String key = SPOT_DETAILS_PREFIX + spotId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            System.err.println("Error invalidating parking spot cache: " + e.getMessage());
        }
    }

    /**
     * Refresh all available spots for a type (full cache rebuild)
     */
    public void refreshAvailableSpotsForType(ParkingSpotType spotType, Set<Long> availableSpotIds) {
        try {
            String key = AVAILABLE_SPOTS_PREFIX + spotType.name();
            redisTemplate.delete(key);
            
            for (Long spotId : availableSpotIds) {
                redisTemplate.opsForSet().add(key, spotId);
            }
            redisTemplate.expire(key, CACHE_EXPIRY, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Error refreshing available spots cache: " + e.getMessage());
        }
    }

    /**
     * Clear all cache for a specific spot type
     */
    public void clearAvailableSpotsCache(ParkingSpotType spotType) {
        try {
            String key = AVAILABLE_SPOTS_PREFIX + spotType.name();
            redisTemplate.delete(key);
        } catch (Exception e) {
            System.err.println("Error clearing available spots cache: " + e.getMessage());
        }
    }

    /**
     * Check if cache is available (for monitoring)
     */
    public boolean isCacheHealthy() {
        try {
            redisTemplate.opsForValue().set("health:check", "OK", 1, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            System.err.println("Cache health check failed: " + e.getMessage());
            return false;
        }
    }
}
