# Redis Setup Guide for Parking Lot System

## Quick Start - Local Redis Setup

### Option 1: Docker (Recommended)
```bash
# Pull Redis image
docker pull redis:latest

# Run Redis container
docker run -d -p 6379:6379 --name parking-lot-redis redis:latest

# Verify Redis is running
docker ps

# Stop Redis
docker stop parking-lot-redis

# Resume Redis
docker start parking-lot-redis

# Remove container (if needed)
docker rm parking-lot-redis
```

### Option 2: Windows (Direct Installation)
1. Download Redis for Windows from: https://github.com/microsoftarchive/redis/releases
2. Extract to a directory (e.g., `C:\redis`)
3. Open Command Prompt in that directory
4. Run: `redis-server.exe`
5. In another Command Prompt, verify: `redis-cli ping` (should return "PONG")

### Option 3: WSL2 (Windows Subsystem for Linux)
```bash
# In WSL2 terminal
sudo apt-get update
sudo apt-get install redis-server
sudo service redis-server start

# Verify
redis-cli ping
```

### Option 4: macOS (Homebrew)
```bash
brew install redis
brew services start redis

# Verify
redis-cli ping
```

### Option 5: Linux (Ubuntu/Debian)
```bash
sudo apt-get install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server

# Verify
redis-cli ping
```

## Running the Parking Lot Application

### 1. Ensure Prerequisites
- PostgreSQL running on localhost:5432 with database `parkinglot`
- Redis running on localhost:6379 (or configured in application.properties)
- Java 21 installed

### 2. Build the Project
```bash
cd c:\projects\youtube-projects\parkinglot
mvn clean package -DskipTests
```

### 3. Run the Application
```bash
# Using Maven
mvn spring-boot:run

# Or using JAR
java -jar target/parkinglot-0.0.1-SNAPSHOT.jar
```

### 4. Verify Cache Initialization
Check application logs for:
```
Initializing Redis cache with available parking spots...
Initialized X spots for CAR
Initialized X spots for TRUCK
Initialized X spots for MOTORCYCLE
Redis cache initialization completed successfully!
```

## Testing Redis Functionality

### Using redis-cli

#### Connect to Redis
```bash
redis-cli
```

#### Check Available Spots Cache
```bash
# View all keys
KEYS *

# Check available spots for each vehicle type
SMEMBERS available_spots:CAR
SMEMBERS available_spots:TRUCK
SMEMBERS available_spots:MOTORCYCLE

# Count available spots
SCARD available_spots:CAR
```

#### Check Occupied Vehicles
```bash
# View occupied vehicle
GET occupied_vehicle:ABC123XYZ

# Check all occupied vehicles
KEYS occupied_vehicle:*
```

#### Monitor Real-time Operations
```bash
# Watch all Redis commands in real-time
MONITOR

# In another terminal, make API calls to check-in/check-out
```

#### Clear Cache (Testing)
```bash
# Clear all cache
FLUSHDB

# Restart application to reinitialize
```

## Testing with API Endpoints

### Prerequisites
- Application running on localhost:8080
- Parking spots added to database
- Redis cache initialized

### 1. Check-in Vehicle
```bash
curl -X PUT http://localhost:8080/api/v1/checkin \
  -H "Content-Type: application/json" \
  -d '{"licensePlate":"ABC123XYZ", "vehicleType":"CAR"}'
```

**Expected Response:**
```json
{
  "id": 1,
  "licensePlate": "ABC123XYZ",
  "vehicleType": "CAR",
  "entryTime": "2024-01-15T10:30:00",
  "spotId": 5,
  "status": "ACTIVE"
}
```

**Redis Effects:**
- Spot 5 removed from `available_spots:CAR`
- `occupied_vehicle:ABC123XYZ` created with entry details

### 2. Check-out Vehicle
```bash
curl -X PUT http://localhost:8080/api/v1/checkout/1
```

**Expected Response:**
```json
{
  "id": 1,
  "licensePlate": "ABC123XYZ",
  "vehicleType": "CAR",
  "exitTime": "2024-01-15T11:45:00",
  "finalPrice": 25.50,
  "status": "COMPLETED"
}
```

**Redis Effects:**
- `occupied_vehicle:ABC123XYZ` removed
- Spot 5 added back to `available_spots:CAR`

### 3. Add New Parking Spot
```bash
curl -X POST http://localhost:8080/api/v1/addSpot \
  -H "Content-Type: application/json" \
  -d '{
    "spotId": 100,
    "spotType": "REGULAR",
    "price": 20.0,
    "peakPrice": 30.0
  }'
```

**Redis Effects:**
- Spot 100 added to `available_spots:CAR` (REGULAR maps to CAR)

## Troubleshooting

### Issue: "Connection refused" when connecting to Redis
**Solution:**
- Verify Redis is running: `redis-cli ping`
- Check Redis port: default is 6379
- Verify firewall rules allow localhost:6379

### Issue: "Cannot connect to application database"
**Solution:**
- Verify PostgreSQL is running
- Check connection string in application.properties
- Verify database `parkinglot` exists

### Issue: Cache not initializing on startup
**Solution:**
- Check application logs for errors
- Verify parking spots exist in database
- Manually clear Redis: `FLUSHDB` and restart

### Issue: API returns 500 error
**Solution:**
- Check application logs for stack trace
- Verify all dependencies are running
- Try with Redis disabled (will use DB, slower)

## Performance Verification

### Check Cache Hit Rate
Monitor using redis-cli `MONITOR`:
- First request: Cache miss, takes 100-300ms
- Subsequent requests: Cache hit, takes 50-100ms

### Measure Response Times
```bash
# Linux/macOS
time curl -X PUT http://localhost:8080/api/v1/checkin \
  -H "Content-Type: application/json" \
  -d '{"licensePlate":"ABC123XYZ", "vehicleType":"CAR"}'

# Windows PowerShell
Measure-Command {
  curl -X PUT http://localhost:8080/api/v1/checkin `
    -H "Content-Type: application/json" `
    -d '{"licensePlate":"ABC123XYZ", "vehicleType":"CAR"}'
}
```

## Production Considerations

1. **Redis Persistence**: Configure RDB/AOF backups
2. **Replication**: Setup master-slave for HA
3. **Cluster**: Use Redis Cluster for scaling
4. **Security**: Set password and firewall rules
5. **Monitoring**: Setup metrics (Prometheus/Grafana)
6. **Backup**: Regular backup of Redis data

## Additional Resources

- [Redis Documentation](https://redis.io/documentation)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Redis Commands](https://redis.io/commands)
- [Lettuce Client](https://lettuce.io/)
