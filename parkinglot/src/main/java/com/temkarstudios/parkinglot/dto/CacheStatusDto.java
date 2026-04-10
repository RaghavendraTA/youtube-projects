package com.temkarstudios.parkinglot.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for returning cache statistics and status
 */
public class CacheStatusDto {
    private boolean cacheHealthy;
    private int totalSpots;
    private long availableSpots;
    private long occupiedSpots;
    private Map<String, Integer> availableByType;
    private LocalDateTime lastSyncTime;
    private String message;

    public CacheStatusDto() {
        this.lastSyncTime = LocalDateTime.now();
    }

    public CacheStatusDto(boolean cacheHealthy, int totalSpots, long availableSpots, 
                         long occupiedSpots, Map<String, Integer> availableByType) {
        this.cacheHealthy = cacheHealthy;
        this.totalSpots = totalSpots;
        this.availableSpots = availableSpots;
        this.occupiedSpots = occupiedSpots;
        this.availableByType = availableByType;
        this.lastSyncTime = LocalDateTime.now();
        this.message = "Cache status retrieved successfully";
    }

    // Getters and Setters
    public boolean isCacheHealthy() {
        return cacheHealthy;
    }

    public void setCacheHealthy(boolean cacheHealthy) {
        this.cacheHealthy = cacheHealthy;
    }

    public int getTotalSpots() {
        return totalSpots;
    }

    public void setTotalSpots(int totalSpots) {
        this.totalSpots = totalSpots;
    }

    public long getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(long availableSpots) {
        this.availableSpots = availableSpots;
    }

    public long getOccupiedSpots() {
        return occupiedSpots;
    }

    public void setOccupiedSpots(long occupiedSpots) {
        this.occupiedSpots = occupiedSpots;
    }

    public Map<String, Integer> getAvailableByType() {
        return availableByType;
    }

    public void setAvailableByType(Map<String, Integer> availableByType) {
        this.availableByType = availableByType;
    }

    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
