# Redis Cache Implementation for Parking Lot System

## Overview
This document describes the Redis cache implementation added to the parking lot system. The implementation provides fast parking availability checks using Redis while maintaining consistency with the PostgreSQL database.

## Architecture

### Key Components

1. **RedisConfig** (`config/RedisConfig.java`)
   - Configures Redis connection and serialization
   - Sets up RedisTemplate for JSON serialization
   - Provides separate templates for generic objects and ParkingSpot entities

2. **ParkingSpotCacheService** (`services/ParkingSpotCacheService.java`)
   - Manages all Redis cache operations
   - Handles available spots sets and spot details
   - Provides cache health checks and refresh operations

3. **AsyncDatabaseUpdateService** (`services/AsyncDatabaseUpdateService.java`)
   - Handles asynchronous database updates
   - Runs in background thread pool to not block API responses
   - Updates both database and cache for consistency

4. **CacheConsistencyScheduler** (`services/CacheConsistencyScheduler.java`)
   - Periodic tasks to sync cache with database
   - Cache health monitoring
   - Cache statistics logging

5. **ParkingSpotManager** (modified)
   - Updated to use cache for parking spot lookups
   - Cache-first approach with database fallback

6. **ParkingServiceImpl** (modified)
   - Returns responses immediately
   - Delegates database updates to async service

## Data Flow

### Check-In Flow (Fast Response Path)
```
User Request (/api/v1/checkin)
    ↓
ParkingServiceImpl.enterVehicle()
    ↓
ParkingSpotManager.findEmptySpotForVehicle()
    ├─ ParkingSpotCacheService.getAvailableSpotIdForType() [Redis - FAST]
    └─ DB Fallback if cache miss
    ↓
Update Redis Cache (Remove from available set) [IMMEDIATE]
    ↓
Return Ticket Response [IMMEDIATE]
    ↓
AsyncDatabaseUpdateService.updateParkingSpotOccupancy() [BACKGROUND]
    └─ Runs in async thread pool
```

### Check-Out Flow (Fast Response Path)
```
User Request (/api/v1/checkout/{ticketId})
    ↓
ParkingServiceImpl.exitVehicle()
    ↓
Update Redis Cache (Add back to available set) [IMMEDIATE]
    ↓
Calculate Pricing
    ↓
Return Ticket with Pricing [IMMEDIATE]
    ↓
AsyncDatabaseUpdateService.updateTicketExit() [BACKGROUND]
    └─ Runs in async thread pool
```

## Redis Cache Structure

### 1. Available Spots Sets
```
Key: parking:available:COMPACT
Type: Redis Set
Values: [spotId1, spotId2, spotId3, ...]
Purpose: Quick lookup of available parking spots by type

Key: parking:available:REGULAR
Type: Redis Set
Values: [spotId4, spotId5, ...]

Key: parking:available:OVERSIZED
Type: Redis Set
Values: [spotId6, spotId7, ...]
```

### 2. Spot Details Hash
```
Key: parking:spot:{spotId}
Type: Redis String (JSON)
Value: {
  "id": 1,
  "isAvailable": true,
  "type": "COMPACT",
  "price": 50.0,
  "peakPrice": 75.0,
  "vehicle": null
}
Purpose: Store detailed information about parking spots
```

## Configuration

### Redis Connection Configuration
File: `application.properties`
```properties
# Redis host and port
spring.redis.host=localhost
spring.redis.port=6379

# Connection pool settings
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-idle=8
```

### Async Processing Configuration
```properties
# Thread pool for async operations
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=100
```

### Cache Sync Intervals
```properties
# Sync database to cache every 5 minutes
parking.cache.sync.interval=300000

# Check cache health every 1 minute
parking.cache.health.check.interval=60000

# Log cache statistics every 10 minutes
parking.cache.stats.interval=600000
```

## Consistency Guarantees

### 1. Immediate Cache Updates
- Cache is updated immediately when parking spots are occupied/vacated
- Users see consistent state during concurrent operations

### 2. Async Database Updates
- Database updates happen in background async tasks
- API responses return immediately for better performance
- Updates are queued and processed in order

### 3. Periodic Synchronization
- Every 5 minutes, full database sync to cache is performed
- Detects and corrects any discrepancies
- Health checks monitor Redis connectivity

### 4. Cache Invalidation
- Specific spots are invalidated after database update
- Triggers rebuild from authoritative database state
- Prevents stale cache entries

## Performance Benefits

1. **Fast Availability Checks**
   - Redis lookup: ~1ms
   - Database query: ~50-100ms
   - 50-100x faster availability checks

2. **Non-blocking API Responses**
   - Response returns immediately
   - Database updates in background
   - Users experience faster responses

3. **Reduced Database Load**
   - Cache reduces database queries
   - Async updates prevent DB bottlenecks
   - Connection pool used efficiently

## Monitoring

### Health Checks
```
[Cache Health] Redis cache is healthy
```

### Sync Logs
```
[Cache Sync] Starting database to cache synchronization...
[Cache Sync] Database to cache synchronization completed. Synced 100 spots
```

### Statistics
```
[Cache Stats] Total Parking Spots: 100
[Cache Stats] Available Spots: 45
[Cache Stats] Occupied Spots: 55
```

## Error Handling

### Cache Failures
- If Redis is unavailable, system falls back to database
- No service interruption if Redis fails
- Graceful degradation with reduced performance

### Database Failures
- Async updates queue and retry
- Cache remains valid for immediate responses
- Manual sync can trigger consistency checks

## Best Practices

### 1. Monitor Cache Health
- Check logs for health check failures
- Ensure Redis server is running
- Monitor memory usage on Redis

### 2. Adjust Cache Intervals
- For high-traffic systems: reduce sync interval to 60000ms
- For low-traffic systems: increase to 600000ms
- Balance between consistency and resource usage

### 3. Scaling Considerations
- Use Redis Cluster for distributed cache
- Increase async thread pool for high concurrency
- Monitor queue sizes for bottlenecks

### 4. Maintenance
- Periodic full sync ensures eventual consistency
- Health checks catch connectivity issues
- Statistics help optimize pool sizes

## Troubleshooting

### Redis Connection Errors
```
Error: Unable to connect to Redis
Solution: 
1. Check Redis server is running: redis-cli ping
2. Verify host/port in application.properties
3. Check firewall rules
```

### Cache Inconsistencies
```
Issue: Cache and database out of sync
Solution:
1. Manual sync occurs every 5 minutes
2. Restart application to reload cache
3. Check AsyncDatabaseUpdateService logs
```

### Performance Degradation
```
Issue: Slow parking availability checks
Solution:
1. Verify Redis is responding: check health logs
2. Increase async thread pool size
3. Check Redis memory usage (may be evicting keys)
```

## Dependencies Added

```xml
<!-- Already included in pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## Future Enhancements

1. **Redis Cluster Support**
   - Support for distributed Redis deployments
   - Increased availability and scalability

2. **Cache Warming**
   - Pre-load cache on application startup
   - Faster response times immediately

3. **Real-time Notifications**
   - Redis pub/sub for availability updates
   - WebSocket integration for real-time UI updates

4. **Advanced Analytics**
   - Peak hour predictions
   - Usage pattern analysis
   - Revenue optimization

## Conclusion

The Redis cache implementation provides:
- ✅ Fast parking availability checks
- ✅ Non-blocking API responses
- ✅ Automatic consistency synchronization
- ✅ Graceful fallback to database
- ✅ High-performance parking lot system
