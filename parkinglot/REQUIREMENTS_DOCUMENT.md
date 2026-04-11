# Requirements Document: Intelligent Parking Lot Management System

## Overview

The system is designed to manage a parking lot with various spot sizes and vehicle types. It handles vehicle check-ins, check-outs, fare calculation based on dynamic strategies, and the administration of
parking spots. The system must support high-performance lookups and asynchronous data consistency.

## Domain Entities & Rules

### Vehicles

- Vehicle Types: The system must support different categories of vehicles:
    - Motorcycle (Small)
    - Car (Medium)
    - Truck (Large)
- Each vehicle is uniquely identified by its License Plate.

### Parking Spots

- Spot Types: Spots are categorized to match vehicle sizes:
    - Compact: For Motorcycles.
    - Regular: For Cars.
    - Oversized: For Trucks.
- Pricing: Each spot must maintain two pricing tiers:
    - Base Price: Standard hourly/unit rate.
    - Peak Price: Elevated rate applied during peak periods.
- State: A spot must track whether it is currently available or occupied.

### Tickets

- A ticket is generated upon vehicle entry and represents the session.
- It must track:
    - The associated Vehicle.
    - The assigned Parking Spot.
    - Entry Timestamp.
    - Exit Timestamp (recorded at checkout).
    - Active Status (True if the vehicle is still parked).
    - Final Price (calculated upon checkout).

## Functional Requirements

### Vehicle Check-In (Entry)

- Input: Vehicle details (License Plate, Vehicle Type).
- Process:
  a. Validate the vehicle type.
  b. Find the first available parking spot that matches the vehicle's size requirement.
  c. Mark the spot as occupied.
  d. Generate a unique ticket for the session.
- Output: A ticket containing the entry details.

### Vehicle Check-Out (Exit)

- Input: Ticket ID.
- Process:
  a. Retrieve the active ticket.
  b. Record the current time as the exit timestamp.
  c. Calculate the total fare using a dynamic fare strategy.
  d. Mark the associated parking spot as available.
  e. Mark the ticket as inactive.
- Output: The finalized ticket including the total cost.

### Fare Calculation

- The system must support a pluggable Fare Strategy pattern to determine costs (30mins price is the flat price).
- Base Strategy: Calculates fare based on the spot's base price and duration (rate * minutes_parked).
- Peak Strategy: Calculates fare based on the spot's peak price for specific time windows or conditions (Peak_rate * minutes_parked).
- A Factory should be used to determine which strategy to apply at runtime based on the context.

### Spot Management

- Administrative capability to add new parking spots to the system.
- Input: Spot Type, Base Price, and Peak Price.

## Non-Functional Requirements

### Performance & Scalability

- Caching: To ensure low latency for check-in/check-out operations, the system should utilize a distributed cache (e.g., Redis) to store spot availability and active sessions.
- Asynchronous Persistence: Updates to the primary database should be handled asynchronously to prevent blocking the user-facing API.
- Initialization: On system startup, the cache must be initialized from the primary database to ensure consistency.

### API Design

- The system should expose a RESTful API with the following endpoints:
    - PUT /api/v1/checkin: Process vehicle entry.
    - PUT /api/v1/checkout/{ticketId}: Process vehicle exit and payment.
    - POST /api/v1/addSpot: Create a new parking spot.

Technical Requirements Specification: Intelligent Parking Lot System

## System Architecture & Design Patterns

The system must be implemented using a layered architecture (Controller $\rightarrow$ Service $\rightarrow$ Manager $\rightarrow$ Repository) and adhere to the following patterns:

- Strategy Pattern: Use a FareStrategy interface to decouple the fare calculation logic from the checkout process. Implement multiple strategies (e.g., BaseFareStrategy, PeakFareStrategy) that can be swapped
  at runtime.
- Factory Pattern: Implement a FareCalculationFactory to encapsulate the logic that decides which FareStrategy instance to provide based on the current time or system state.
- Builder Pattern: Use builders for complex domain models (Ticket, Vehicle, ParkingSpot) to ensure immutability and flexible object construction.
- Observer/Event-Driven: Implement a listener mechanism (e.g., CacheInitializationListener) to synchronize the primary database with the cache during system bootstrap.

## Data Model & Schema Requirements

### Entities

- Vehicle:
    - licensePlate (String, Primary Key/Unique)
    - type (Enum: CAR, TRUCK, MOTORCYCLE)
    - size (Enum: SMALL, MEDIUM, LARGE) $\rightarrow$ Derived from type.
- ParkingSpot:
    - id (Long, Primary Key)
    - type (Enum: COMPACT, REGULAR, OVERSIZED)
    - isAvailable (Boolean)
    - price (Float - Base Rate)
    - peakPrice (Float - Peak Rate)
    - vehicle (One-to-One relationship with Vehicle)
- Ticket:
    - id (Long, Primary Key, Sequence Generated)
    - vehicle (Many-to-One relationship)
    - parkingSpot (Many-to-One relationship)
    - entryTime / exitTime (DateTime)
    - isActive (Boolean)
    - finalPrice (Float)

### Mapping Logic

The system must enforce a strict mapping between VehicleSize and ParkingSpotType:
- SMALL $\rightarrow$ COMPACT
- MEDIUM $\rightarrow$ REGULAR
- LARGE $\rightarrow$ OVERSIZED

## Technical Functional Workflow

### Entry Flow (Check-In)

1. Request: Accept a DTO containing licensePlate and vehicleType.
2. Validation: Resolve the VehicleSize based on the VehicleType.
3. Allocation: Query the persistence layer for the first available ParkingSpot where type matches the vehicle's size requirement.
4. Atomicity: Mark the spot as unavailable and create a Ticket record in a single transaction/atomic operation.

### Exit Flow (Check-Out)

1. Retrieval: Fetch the active Ticket by ID.
2. Calculation:
   - Invoke the FareCalculationFactory to get the appropriate IFareStrategy.
   - Pass the Ticket object to the strategy to calculate the finalPrice based on the duration between entryTime and current time.
3. Cleanup: Update the ParkingSpot to isAvailable = true and the Ticket to isActive = false.

## Infrastructure & Performance Specifications

### Caching Layer (Distributed)

- State Management: Use a distributed cache (e.g., Redis) to store the "Available Spots" list to avoid expensive database scans on every check-in.
- Consistency: Implement a Write-Behind (Asynchronous) pattern where the cache is updated immediately for the user, and a background service (AsyncDatabaseUpdateService) persists the changes to the RDBMS.

### API Specifications

- Protocol: REST over HTTP.
- Format: JSON.
- Error Handling: Implement a global exception handling mechanism that returns 400 Bad Request with a descriptive error message in a TicketDto (or similar) response wrapper when business rules are violated
  (e.g., "No spots available").

### Complexity Constraints

- Time Complexity: Check-in and Check-out operations should aim for $O(1)$ or $O(\log N)$ by leveraging indexed lookups and caching.
- Space Complexity: The system should handle a variable number of parking spots without linear degradation in performance.