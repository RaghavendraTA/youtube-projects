package com.temkarstudios.ratelimiter.core;

public abstract class RateLimiter {
    protected final int maxRequests;
    private final Thread refillThread;
    public final long refillIntervalMillis;
    protected static final long CLEANUP_INTERVAL_MS = 60000; // 1 minute

    public RateLimiter(int maxRequests, long refillIntervalMillis) {
        this.maxRequests = maxRequests;
        this.refillIntervalMillis = refillIntervalMillis;
        this.refillThread = new Thread(this::refillTokens);
        this.refillThread.setDaemon(true);
        this.refillThread.start();
    }

    private void refillTokens() {
        while (true) {
            try {
                Thread.sleep(refillIntervalMillis);
                processEntries();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * This method should be implemented by subclasses to determine if a request with the given key is allowed based on the rate limiting logic.
     * @param key
     * @return true if the request is allowed, false if it exceeds the rate limit
     */
    public abstract boolean allowRequests(String key);
    
    /**
     * It should be implemented by subclasses and This method is called periodically to process the entries in the rate limiter.<br/>
     * it should handle refilling and eviction logic
     */
    protected abstract void processEntries();
}
