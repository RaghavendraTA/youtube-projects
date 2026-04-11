# Rate Limiter Service – Requirements Specification

## 1. Overview

The service exposes an HTTP API (Spring MVC) and protects all incoming requests with a per‑client IP rate‑limiting interceptor.
Key points:

| Layer | Responsibility |
| :--- | :--- |
| Controller | Business logic (currently none; placeholder BaseController). |
| Middleware (HandlerInterceptor) | Intercepts every request, extracts the client IP, and consults a RateLimiter. |
| RateLimiter | Abstract base that schedules periodic clean‑up/refill. Concrete strategies are implemented in separate classes (TokenBucket, LeakyBucket, FixedWindow, SlidingWindow). |
| Factory | RateLimiterFactory builds a limiter instance based on an enum (RateLimitersEnum). |
| Configuration | WebConfig registers the interceptor. |

The default configuration creates a Token‑Bucket limiter that allows 2 requests per second per IP, with a daemon thread refilling tokens every second.

---

## 2. Functional Requirements

| # | Requirement | Notes |
| :--- | :--- | :--- |
| FR‑01 | Intercept every HTTP request before controller dispatch. | Implemented by RequestMiddleware (HandlerInterceptor). |
| FR‑02 | Identify the caller by IP address (`request.getRemoteAddr()`). | Used as the rate‑limit key. |
| FR‑03 | Enforce a maximum of N requests per T milliseconds per client. | Configurable via RateLimiterFactory. Default: maxRequests=2, intervalMillis=1000. |
| FR‑04 | Respond with 429 Too Many Requests if the limit is exceeded. | JSON body: `{"error":"Rate limit exceeded. Maximum requests exceeded. Please try again later."}` |
| FR‑05 | Support four algorithm variants: Token‑Bucket, Leaky‑Bucket, Fixed‑Window, Sliding‑Window. | Selection via RateLimitersEnum in factory. |
| FR‑06 | Provide clean‑up of idle keys after a configurable timeout (1 min). | Implemented in each concrete limiter’s processEntries(). |
| FR‑07 | All data structures must be thread‑safe and lock‑free where possible. | Use ConcurrentHashMap, AtomicLong/AtomicInteger. |
| FR‑08 | Rate‑limiting logic must be non‑blocking for the request thread. | DAO logic runs in the same thread; periodic clean‑up runs on a daemon thread. |
| FR‑09 | Expose a configuration endpoint (optional) to change limiter type or parameters at runtime. | Not present in current code but a future extension. |
| FR‑10 | Log when a request is rejected due to rate limiting. | Can be added via Response or interceptor. |

---

## 3. Non‑Functional Requirements

| # | Requirement | Notes |
| :--- | :--- | :--- |
| NFR‑01 | Performance – per‑request overhead < 1 ms under normal load. | O(1) hash map lookup + atomic operation. |
| NFR‑02 | Scalability – supports thousands of concurrent IPs. | Memory per IP ≈ 64 bytes; eviction removes idle entries. |
| NFR‑03 | Reliability – limiter state survives server restart (in‑memory). | In‑memory only; no persistence. |
| NFR‑04 | Thread‑safety – no race conditions in counters or token updates. | Use atomic compare‑and‑set and key‑interned locks for Leaky‑Bucket. |
| NFR‑05 | Configurability – maxRequests, intervalMillis, and limiter type are passed to factory at construction. | Allows unit tests with different parameters. |
| NFR‑06 | Extensibility – new algorithms can be added by extending RateLimiter. | Factory switch statement can be extended. |
| NFR‑07 | Observability – provide metrics (e.g., current token count per IP). | Not in current code, but recommended for Prometheus/Grafana. |

---

## 4. Algorithmic Strategies & Optimizations

| Algorithm | Core Idea | Complexity | Optimization Notes |
| :--- | :--- | :--- | :--- |
| Token‑Bucket | Refill tokens at a fixed rate; allow a burst up to maxRequests. | O(1) per request; O(n) per cleanup (n = active keys). | Tokens stored in AtomicLong; daemon thread refills entire bucket each interval for simplicity. |
| Leaky‑Bucket | Each request adds to a “water level”; leak occurs at a constant rate. | O(1) per request with synchronized key lock; cleanup O(n). | Uses synchronized (key.intern()) to avoid global lock; leak calculation uses double arithmetic. |
| Fixed‑Window | Count requests in discrete windows of intervalMillis. | O(1) per request; resets counter when window expires. | Stores window start times and counts in concurrent maps. |
| Sliding‑Window | Maintains a moving window; allows smoother rate limiting. | O(1) per request; resets count if window elapsed. | Keeps timeWindowMillis in each limiter; reuses atomic counters. |

All algorithms share:

- Periodic cleanup thread: runs every refillIntervalMillis to evict idle keys (last accessed > 1 min).
- Thread‑safety: all shared maps are ConcurrentHashMap. Leaky‑Bucket uses per‑key synchronization; others rely on atomic CAS.

---

## 5. Integration & Deployment Notes

### 1. Spring Boot Configuration
- RequestMiddleware is instantiated with default limiter settings.
- WebConfig registers the interceptor globally (`registry.addInterceptor(requestMiddleware)`).

### 2. Testing
- RatelimiterApplicationTests (located in `src/test/java/com/temkarstudios/ratelimiter`) verifies the limiter behaves correctly.
- Integration tests use EmbeddedRedisTestConfig for caching (not currently used by the limiter).

### 3. Extending or Reconfiguring
- To switch algorithms, modify the constructor in RequestMiddleware to call `RateLimiterFactory.createRateLimiter(...)` with a different `RateLimitersEnum`.
- To change maxRequests or intervalMillis, adjust the factory arguments accordingly.

### 4. Future Enhancements
- Persist limiter state to Redis or another distributed cache for horizontal scaling.
- Expose actuator endpoints for metrics and runtime configuration.
- Add per‑endpoint rate limits (extract endpoint path as key).

---

## 6. Constraints & Assumptions

- The service runs in a single JVM instance; no distributed coordination.
- Rate limiting is based solely on the client IP (`request.getRemoteAddr()`).
- The application has no external dependencies for the limiter (no Redis, no database).
- All timing values are in milliseconds.

---

## 7. Deliverables for an AI to Re‑implement

### 1. Java classes
- `RateLimiter` (abstract) with background refill thread and cleanup.
- Concrete subclasses (`TokenBucketRateLimiter`, `LeakyBucketRateLimiter`, `FixedWindowRateLimiter`, `SlidingWindowRateLimiter`).
- `RateLimitersEnum` enum.
- `RateLimiterFactory` with a static `createRateLimiter` method.

### 2. Spring MVC components
- `RequestMiddleware` implementing `HandlerInterceptor`.
- `WebConfig` registering the interceptor.

### 3. Thread‑safety & Performance
- Use `ConcurrentHashMap` for per‑IP state.
- Use `AtomicLong`/`AtomicInteger` for counters.
- Key‑interned locks only for Leaky‑Bucket to avoid contention.

### 4. Cleanup
- Evict entries idle for > 1 minute (`CLEANUP_INTERVAL_MS`).

### 5. HTTP Response
- 429 status with JSON error body when limit is exceeded.

### 6. Testing hooks
- Ensure unit tests can inject a custom limiter or alter parameters.