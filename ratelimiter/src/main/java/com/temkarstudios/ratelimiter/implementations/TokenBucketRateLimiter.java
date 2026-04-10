package com.temkarstudios.ratelimiter.implementations;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

import com.temkarstudios.ratelimiter.core.RateLimiter;

public class TokenBucketRateLimiter extends RateLimiter {
    private final ConcurrentHashMap<String, AtomicLong> tokenBuckets;
    private final ConcurrentHashMap<String, Long> lastAccessTime;

    public TokenBucketRateLimiter(int maxRequests, long refillIntervalMillis) {
        super(maxRequests, refillIntervalMillis);
        this.tokenBuckets = new ConcurrentHashMap<>();
        this.lastAccessTime = new ConcurrentHashMap<>();
    }

    @Override
    public boolean allowRequests(String key) {
        AtomicLong tokens = tokenBuckets.computeIfAbsent(key, k -> new AtomicLong(maxRequests));
        lastAccessTime.put(key, System.currentTimeMillis());
        long currentTokens = tokens.get();
        return currentTokens >= 1 && tokens.compareAndSet(currentTokens, currentTokens - 1);
    }

    @Override
    protected void processEntries() {
        long currentTime = System.currentTimeMillis();
        tokenBuckets.forEach((key, tokens) -> {
            long lastAccess = lastAccessTime.getOrDefault(key, currentTime);

            // Remove key if not accessed in the last minute
            if (currentTime - lastAccess > CLEANUP_INTERVAL_MS) {
                tokenBuckets.remove(key);
                lastAccessTime.remove(key);
            } else {
                // Refill tokens for active keys
                tokens.set(maxRequests);
            }
        });
    }
}
