package com.temkarstudios.ratelimiter.implementations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.temkarstudios.ratelimiter.core.RateLimiter;

public class SlidingWindowRateLimiter extends RateLimiter {
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts;
    private final ConcurrentHashMap<String, Long> windowStartTimes;
    private final ConcurrentHashMap<String, Long> lastAccessTime;
    protected final long timeWindowMillis;

    public SlidingWindowRateLimiter(int maxRequests, long timeWindowMillis) {
        super(maxRequests, timeWindowMillis);
        this.timeWindowMillis = timeWindowMillis;
        this.requestCounts = new ConcurrentHashMap<>();
        this.windowStartTimes = new ConcurrentHashMap<>();
        this.lastAccessTime = new ConcurrentHashMap<>();
    }

    @Override
    public boolean allowRequests(String key) {
        long currentTime = System.currentTimeMillis();
        lastAccessTime.put(key, currentTime);
        
        // Get or create window start time and request count
        long windowStart = windowStartTimes.computeIfAbsent(key, k -> currentTime);
        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        
        // Check if we're still within the current window
        if (currentTime - windowStart > timeWindowMillis) {
            // Window has expired, reset it
            windowStartTimes.put(key, currentTime);
            count.set(0);
        }
        
        // Check if we can allow this request
        int currentCount = count.get();
        return currentCount < maxRequests && count.compareAndSet(currentCount, currentCount + 1);
    }

    @Override
    protected void processEntries() {
        long currentTime = System.currentTimeMillis();
        
        lastAccessTime.forEach((key, lastAccess) -> {
            // Remove entries not accessed in the last minute
            if (currentTime - lastAccess > CLEANUP_INTERVAL_MS) {
                requestCounts.remove(key);
                windowStartTimes.remove(key);
                lastAccessTime.remove(key);
            }
        });
    }
    
}
