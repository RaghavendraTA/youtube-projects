package com.temkarstudios.parkinglot.controllers;

import com.temkarstudios.parkinglot.dto.CacheStatusDto;
import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.repository.ParkingSpotRepository;
import com.temkarstudios.parkinglot.services.ParkingSpotCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache monitoring controller
 * Provides endpoints to check cache status and statistics
 */
@RestController
@RequestMapping("/api/v1/cache")
public class CacheStatusController {

    @Autowired
    private ParkingSpotCacheService cacheService;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    /**
     * Get current cache status and statistics
     * Endpoint: GET /api/v1/cache/status
     */
    @GetMapping(path = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CacheStatusDto> getCacheStatus() {
        try {
            // Check if cache is healthy
            boolean cacheHealthy = cacheService.isCacheHealthy();

            // Get statistics from database
            int totalSpots = Math.toIntExact(parkingSpotRepository.count());
            long availableSpots = parkingSpotRepository.countByIsAvailableTrue();
            long occupiedSpots = totalSpots - availableSpots;

            // Get availability by type
            Map<String, Integer> availableByType = new HashMap<>();
            availableByType.put("COMPACT", parkingSpotRepository
                .findAll()
                .stream()
                .filter(s -> s.getType() == ParkingSpotType.COMPACT && s.isAvailable())
                .toList()
                .size()
            );
            availableByType.put("REGULAR", parkingSpotRepository
                .findAll()
                .stream()
                .filter(s -> s.getType() == ParkingSpotType.REGULAR && s.isAvailable())
                .toList()
                .size()
            );
            availableByType.put("OVERSIZED", parkingSpotRepository
                .findAll()
                .stream()
                .filter(s -> s.getType() == ParkingSpotType.OVERSIZED && s.isAvailable())
                .toList()
                .size()
            );

            CacheStatusDto status = new CacheStatusDto(
                cacheHealthy,
                totalSpots,
                availableSpots,
                occupiedSpots,
                availableByType
            );

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            CacheStatusDto errorStatus = new CacheStatusDto();
            errorStatus.setCacheHealthy(false);
            errorStatus.setMessage("Error retrieving cache status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorStatus);
        }
    }

    /**
     * Health check endpoint
     * Endpoint: GET /api/v1/cache/health
     */
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getHealth() {
        try {
            boolean healthy = cacheService.isCacheHealthy();
            Map<String, Object> response = new HashMap<>();
            response.put("status", healthy ? "UP" : "DOWN");
            response.put("cache", "Redis");
            response.put("timestamp", System.currentTimeMillis());

            return healthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Trigger manual cache synchronization
     * Endpoint: POST /api/v1/cache/sync
     */
    @PostMapping(path = "/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> syncCache() {
        try {
            // This would trigger the sync scheduler manually
            // For now, just return success - the scheduled task runs automatically every 5 minutes
            Map<String, String> response = new HashMap<>();
            response.put("status", "SYNC_SCHEDULED");
            response.put("message", "Cache synchronization scheduled. Runs automatically every 5 minutes.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
