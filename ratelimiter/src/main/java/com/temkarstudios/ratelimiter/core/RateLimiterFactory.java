package com.temkarstudios.ratelimiter.core;

import com.temkarstudios.ratelimiter.implementations.FixedWindowRateLimiter;
import com.temkarstudios.ratelimiter.implementations.LeakyBucketRateLimiter;
import com.temkarstudios.ratelimiter.implementations.SlidingWindowRateLimiter;
import com.temkarstudios.ratelimiter.implementations.TokenBucketRateLimiter;

public class RateLimiterFactory {
    public static RateLimiter createRateLimiter(RateLimitersEnum type, int maxRequests, long intervalMillis) {
        return switch (type) {
            case RateLimitersEnum.FIXED_WINDOW -> new FixedWindowRateLimiter(maxRequests, intervalMillis);
            case RateLimitersEnum.SLIDING_WINDOW -> new SlidingWindowRateLimiter(maxRequests, intervalMillis);
            case RateLimitersEnum.TOKEN_BUCKET -> new TokenBucketRateLimiter(maxRequests, intervalMillis);
            case RateLimitersEnum.LEAKY_BUCKET -> new LeakyBucketRateLimiter(maxRequests, intervalMillis);
            default -> throw new IllegalArgumentException("Unsupported type.");
        };
    }
}
