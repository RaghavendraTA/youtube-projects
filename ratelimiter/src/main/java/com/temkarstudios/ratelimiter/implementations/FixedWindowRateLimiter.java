package com.temkarstudios.ratelimiter.implementations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.temkarstudios.ratelimiter.core.RateLimiter;

public class FixedWindowRateLimiter extends RateLimiter {
    private final ConcurrentHashMap<String, Long> windowStartTimes;
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts;
    private final ConcurrentHashMap<String, Long> lastAccessTime;

    public FixedWindowRateLimiter(int maxRequests, long refillIntervalMillis) {
        super(maxRequests, refillIntervalMillis);
        this.windowStartTimes = new ConcurrentHashMap<>();
        this.requestCounts = new ConcurrentHashMap<>();
        this.lastAccessTime = new ConcurrentHashMap<>();
    }

    @Override
    public boolean allowRequests(String key) {
        long now = System.currentTimeMillis();
        lastAccessTime.put(key, now);
        
        // Get or create window start time and request count
        long windowStart = windowStartTimes.computeIfAbsent(key, k -> now);
        AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
        
        // Check if we're still within the current window
        if (now - windowStart >= refillIntervalMillis) {
            // Window has expired, reset it
            windowStartTimes.put(key, now);
            count.set(0);
        }
        
        // Check if we can allow this request
        int currentCount = count.get();
        if (currentCount < maxRequests) {
            if (count.compareAndSet(currentCount, currentCount + 1)) {
                return true;
            }
            // Retry if CAS failed
            return allowRequests(key);
        }
        return false;
    }

    @Override
    protected void processEntries() {
        long currentTime = System.currentTimeMillis();
        
        lastAccessTime.forEach((key, lastAccess) -> {
            // Remove entries not accessed in the last minute
            if (currentTime - lastAccess > CLEANUP_INTERVAL_MS) {
                windowStartTimes.remove(key);
                requestCounts.remove(key);
                lastAccessTime.remove(key);
            }
        });
    }
}
