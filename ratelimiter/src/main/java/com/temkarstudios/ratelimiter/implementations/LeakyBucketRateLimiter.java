package com.temkarstudios.ratelimiter.implementations;

import java.util.concurrent.ConcurrentHashMap;

import com.temkarstudios.ratelimiter.core.RateLimiter;

public class LeakyBucketRateLimiter extends RateLimiter {
    private final int capacity;
    private final double leakRate; // requests per millisecond
    
    private final ConcurrentHashMap<String, Integer> waterLevels;
    private final ConcurrentHashMap<String, Long> lastLeakTimes;
    private final ConcurrentHashMap<String, Long> lastAccessTime;

    public LeakyBucketRateLimiter(int capacity, long leakRateMillis) {
        super(capacity, leakRateMillis);
        this.capacity = capacity;
        this.leakRate = 1.0 / leakRateMillis; // convert to requests per millisecond
        this.waterLevels = new ConcurrentHashMap<>();
        this.lastLeakTimes = new ConcurrentHashMap<>();
        this.lastAccessTime = new ConcurrentHashMap<>();
    }

    @Override
    public boolean allowRequests(String key) {
        long now = System.currentTimeMillis();
        
        // Initialize if needed
        waterLevels.computeIfAbsent(key, k -> 0);
        lastLeakTimes.computeIfAbsent(key, k -> now);
        lastAccessTime.put(key, now);
        
        // Synchronize on the key to ensure atomic operations for this specific key
        synchronized (key.intern()) {
            long lastLeak = lastLeakTimes.get(key);
            long timeElapsed = now - lastLeak;
            
            // Calculate how much water has leaked
            double leakedWater = timeElapsed * leakRate;
            int currentLevel = waterLevels.get(key);
            int newLevel = Math.max(0, (int) (currentLevel - leakedWater));
            
            // Check if we can add this request
            if (newLevel < capacity) {
                waterLevels.put(key, newLevel + 1);
                lastLeakTimes.put(key, now);
                return true;
            } else {
                waterLevels.put(key, newLevel);
                lastLeakTimes.put(key, now);
                return false;
            }
        }
    }

    @Override
    protected void processEntries() {
        long currentTime = System.currentTimeMillis();
        
        lastAccessTime.forEach((key, lastAccess) -> {
            // Remove entries not accessed in the last minute
            if (currentTime - lastAccess > CLEANUP_INTERVAL_MS) {
                waterLevels.remove(key);
                lastLeakTimes.remove(key);
                lastAccessTime.remove(key);
            }
        });
    }
}
