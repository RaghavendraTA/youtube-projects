package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scheduler service for periodic Redis cache consistency with database
 * Ensures that cache and database remain in sync
 */
@Service
public class CacheConsistencyScheduler {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private ParkingSpotCacheService cacheService;

    @Autowired
    private AsyncDatabaseUpdateService asyncDatabaseUpdateService;

    /**
     * Periodic task to sync all parking spots from database to Redis cache
     * Runs every 5 minutes to ensure cache consistency
     */
    @Scheduled(fixedRateString = "${parking.cache.sync.interval:300000}") // 5 minutes default
    public void syncDatabaseToCache() {
        try {
            System.out.println("[Cache Sync] Starting database to cache synchronization...");
            
            List<ParkingSpot> allSpots = parkingSpotRepository.findAll();
            
            if (allSpots.isEmpty()) {
                System.out.println("[Cache Sync] No parking spots found in database");
                return;
            }
            
            // Batch update cache with all spots
            asyncDatabaseUpdateService.batchSyncSpotsToCache(allSpots);
            
            // Rebuild available spots cache for each type
            for (ParkingSpotType spotType : ParkingSpotType.values()) {
                Set<Long> availableSpotIds = new HashSet<>();
                
                for (ParkingSpot spot : allSpots) {
                    if (spot.getType() == spotType && spot.isAvailable()) {
                        availableSpotIds.add(spot.getId());
                    }
                }
                
                cacheService.refreshAvailableSpotsForType(spotType, availableSpotIds);
            }
            
            System.out.println("[Cache Sync] Database to cache synchronization completed. Synced " + allSpots.size() + " spots");
            
        } catch (Exception e) {
            System.err.println("[Cache Sync] Error during database to cache synchronization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check cache health periodically
     * Runs every 1 minute
     */
    @Scheduled(fixedRateString = "${parking.cache.health.check.interval:60000}") // 1 minute default
    public void checkCacheHealth() {
        try {
            boolean healthy = cacheService.isCacheHealthy();
            if (!healthy) {
                System.err.println("[Cache Health] WARNING: Redis cache is not responding properly");
            } else {
                System.out.println("[Cache Health] Redis cache is healthy");
            }
        } catch (Exception e) {
            System.err.println("[Cache Health] Error checking cache health: " + e.getMessage());
        }
    }

    /**
     * Log cache statistics periodically
     * Runs every 10 minutes
     */
    @Scheduled(fixedRateString = "${parking.cache.stats.interval:600000}") // 10 minutes default
    public void logCacheStatistics() {
        try {
            int totalSpots = Math.toIntExact(parkingSpotRepository.count());
            long availableSpots = parkingSpotRepository.countByIsAvailableTrue();
            
            System.out.println("[Cache Stats] Total Parking Spots: " + totalSpots);
            System.out.println("[Cache Stats] Available Spots: " + availableSpots);
            System.out.println("[Cache Stats] Occupied Spots: " + (totalSpots - availableSpots));
            
        } catch (Exception e) {
            System.err.println("[Cache Stats] Error logging cache statistics: " + e.getMessage());
        }
    }
}
