# Implementation Plan: Intelligent Parking Lot Management System

## Context

The goal is to implement a complete Intelligent Parking Lot Management System based on the provided REQUIREMENTS_DOCUMENT.md and REDIS_IMPLE.md. The system manages vehicle check-ins, check-outs, and
parking spot administration, utilizing a layered architecture, various design patterns (Strategy, Factory, Builder, Observer), and a distributed cache (Redis) for high performance and asynchronous
persistence.

## Proposed Architecture

### Layered Architecture: Controller $\rightarrow$ Service $\rightarrow$ Manager $\rightarrow$ Repository

----

#### Domain Models & Entities

- Vehicle: (licensePlate, type, size) - Builder pattern.
- ParkingSpot: (id, type, isAvailable, price, peakPrice, vehicle) - Builder pattern.
- Ticket: (id, vehicle, parkingSpot, entryTime, exitTime, isActive, finalPrice) - Builder pattern.
- VehicleType (Enum): MOTORCYCLE, CAR, TRUCK.
- VehicleSize (Enum): SMALL, MEDIUM, LARGE.
- ParkingSpotType (Enum): COMPACT, REGULAR, OVERSIZED.

#### Core Logic & Design Patterns

- Fare Calculation (Strategy & Factory):
  - IFareStrategy interface.
  - BaseFareStrategy: rate * minutes_parked.
  - PeakFareStrategy: peak_rate * minutes_parked.
  - FareCalculationFactory: Decides which strategy to use based on time/state.
- Caching Layer (Redis):
  - Available Spots: Key: VehicleType $\rightarrow$ Value: List<SpotId>.
  - Occupied Logic: Key: VehicleLicensePlate $\rightarrow$ Value: {Spot, CarType, EntryTime}.
- Asynchronous Persistence:
  - AsyncDatabaseUpdateService: Handles DB updates in @Async mode to prevent blocking the API.
- System Initialization (Observer/Event):
  - CacheInitializationListener: Warms up the Redis cache from the database on startup.

#### API Endpoints

- PUT /api/v1/checkin: Process vehicle entry.
- PUT /api/v1/checkout/{ticketId}: Process vehicle exit and payment.
- POST /api/v1/addSpot: Create a new parking spot.

## Implementation Steps

1. Project Setup: Initialize project structure and dependencies (Spring Boot, Redis, JPA).
2. Domain Layer: Implement Enums, Entities, and Builders.
3. Repository Layer: Implement JPA repositories for Vehicle, ParkingSpot, and Ticket.
4. Strategy Layer: Implement IFareStrategy, BaseFareStrategy, PeakFareStrategy, and FareCalculationFactory.
5. Caching Layer: Implement Redis logic for spot availability and session tracking.
6. Service/Manager Layer:
   - ParkingManager: Orchestrates the core business logic for check-in/check-out.
   - AsyncDatabaseUpdateService: Handles non-blocking DB persistence.
7. Controller Layer: Implement REST endpoints and global exception handling.
8. Infrastructure: Implement CacheInitializationListener for system bootstrap.
9. Testing: Verify flows (Check-in $\rightarrow$ Cache Update $\rightarrow$ Async DB Update $\rightarrow$ Check-out $\rightarrow$ Fare Calc).

## Verification Plan

- Unit Tests: Fare strategies, Factory logic, Builder validation.
- Integration Tests: Redis cache hit/miss, Async DB update verification.
- End-to-End Tests:
  - Successful check-in for different vehicle types.
  - Handling "No spots available" (400 Bad Request).
  - Correct fare calculation using different strategies during checkout.
  - Administrative addition of new spots.