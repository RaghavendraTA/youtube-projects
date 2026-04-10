# Parking Lot Management System - Requirements Document

## 1. Project Overview

### Project Name
**Parking Lot Management System** (parkinglot)

### Project Description
A Spring Boot RESTful web application designed to manage parking lot operations including vehicle check-ins, check-outs, pricing calculations, and parking spot management. The system supports dynamic pricing based on peak versus off-peak hours and handles multiple vehicle types with appropriately sized parking spots.

### Technical Stack
- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21
- **Database**: PostgreSQL
- **Cache**: Redis (Spring Data Redis)
- **ORM**: JPA/Hibernate
- **API Documentation**: Swagger/OpenAPI 3.0
- **Build Tool**: Maven
- **GroupId**: com.raghavendrata
- **ArtifactId**: parkinglot
- **Version**: 1.0.0

---

## 2. API Endpoints

### 2.1 Check-In Endpoint
**Endpoint**: `PUT /api/v1/checkin`
- **Description**: Registers a vehicle entry into the parking lot and generates a parking ticket
- **Request Body**:
  ```json
  {
    "licensePlate": "string",
    "vehicleType": "string (CAR | TRUCK | MOTORCYCLE)"
  }
  ```
- **Response** (Success - 200 OK):
  ```json
  {
    "ticketId": "long",
    "vehicleNo": "string",
    "parkingSpotNo": "long",
    "entryTime": "timestamp",
    "exitTime": null,
    "price": null,
    "message": "",
    "hasError": false
  }
  ```
- **Response** (Error - 400 Bad Request):
  ```json
  {
    "message": "Failed to generate a ticket: [error reason]",
    "hasError": true
  }
  ```
- **Error Scenarios**:
  - Vehicle already parked with active ticket
  - No available parking spot for the vehicle size

### 2.2 Check-Out Endpoint
**Endpoint**: `PUT /api/v1/checkout/{ticketId}`
- **Description**: Registers vehicle exit, calculates parking fare, and closes the parking ticket
- **Path Parameter**: `ticketId` (Long) - The unique ticket identifier
- **Response** (Success - 200 OK):
  ```json
  {
    "ticketId": "long",
    "vehicleNo": "string",
    "parkingSpotNo": "long",
    "entryTime": "timestamp",
    "exitTime": "timestamp",
    "price": "float (calculated fare)",
    "message": "",
    "hasError": false
  }
  ```
- **Response** (Error - 400 Bad Request):
  ```json
  {
    "message": "Failed to generate a ticket: No ticket found",
    "hasError": true
  }
  ```
- **Error Scenarios**:
  - Ticket ID not found in system

### 2.3 Add Parking Spot Endpoint
**Endpoint**: `POST /api/v1/addSpot`
- **Description**: Creates a new parking spot in the system with associated pricing
- **Request Body**:
  ```json
  {
    "spotId": "long",
    "price": "float",
    "peakPrice": "float",
    "spotType": "string (COMPACT | REGULAR | OVERSIZED)"
  }
  ```
- **Response** (Success - 200 OK):
  ```json
  {
    "message": "Spot is successfully created"
  }
  ```
- **Response** (Error - 400 Bad Request):
  ```json
  {
    "message": "[error reason]"
  }
  ```

### 2.4 Swagger/OpenAPI Documentation
**Endpoint**: `http://localhost:8080/swagger-ui.html`
- Interactive API documentation and testing interface
- Generated from `/v3/api-docs`

---

## 3. Main Logic & Calculations

### 3.1 Check-In Flow (enterVehicle)
1. **Validation**: Check if vehicle is already parked with an active ticket
   - If yes, throw custom exception: "Vehicle is already parked and has an active ticket"
2. **Vehicle Management**: Add/retrieve vehicle entry with:
   - License plate (unique identifier)
   - Vehicle type (CAR, TRUCK, or MOTORCYCLE)
   - Automatic vehicle size assignment based on type
3. **Spot Finding**: Find an available parking spot matching the vehicle size
   - Vehicle size mapping:
     - MOTORCYCLE → SMALL (COMPACT spots)
     - CAR → MEDIUM (REGULAR spots)
     - TRUCK → LARGE (OVERSIZED spots)
4. **Spot Occupation**: Mark the selected spot as occupied
5. **Ticket Generation**: Create parking ticket with:
   - Vehicle reference
   - Parking spot reference
   - Entry timestamp (current date/time)
   - Active status = true
   - Exit time = null
   - Final price = pending

### 3.2 Check-Out Flow (exitVehicle)
1. **Ticket Retrieval**: Find ticket by ID
2. **Spot Vacation**: Mark parking spot as available and remove vehicle assignment
3. **Exit Registration**: Set exit timestamp to current date/time
4. **Price Calculation**: Compute parking fare using strategy pattern
5. **Ticket Update**: Finalize ticket with calculated price and mark as inactive

### 3.3 Pricing Calculation Logic

#### Strategy Pattern Implementation
Two fare calculation strategies are implemented:

**Base Fare Strategy** (Off-Peak Hours):
```
Duration Calculation:
  - Time difference = exitTime - entryTime
  - Total minutes = time difference / (1000 * 60)
  - Hours = floor(total minutes / 60)
  - Remaining minutes = total minutes % 60

Fare Formula:
  - Base charge = hours × 2 × spotPrice
  - Additional charge = (remaining minutes > 30) ? 2 × spotPrice : 1 × spotPrice
  - Total fare = base charge + additional charge
```

**Peak Fare Strategy** (Peak Hours):
- Same calculation as base strategy but uses `peakPrice` instead of regular `price`
- Applied during peak hours

#### Peak Hour Detection
Peak hours are defined as:
- **Morning Peak**: 07:00 - 10:00 (7 AM to 10 AM)
- **Evening Peak**: 16:00 - 19:00 (4 PM to 7 PM)

The system automatically selects the appropriate strategy based on current hour.

#### Pricing Example
For a vehicle parking in a REGULAR spot (price=$5, peakPrice=$8):
- **Off-Peak Scenario**: Parked for 2 hours 45 minutes
  - Base charge = 2 × 2 × $5 = $20
  - Remaining minutes = 45 > 30, so additional = 2 × $5 = $10
  - Total = $30
- **Peak Scenario**: Same duration
  - Base charge = 2 × 2 × $8 = $32
  - Additional = 2 × $8 = $16
  - Total = $48

---

## 4. Services & Components

### 4.1 ParkingService Interface
**Location**: `interfaces/ParkingService.java`

**Methods**:
1. **enterVehicle(Request request)** → Ticket
   - Manages complete check-in workflow
   - Throws Exception for validation failures

2. **exitVehicle(Long ticketId)** → Ticket
   - Manages complete check-out workflow with pricing
   - Throws Exception for ticket not found

3. **addNewSpot(SpotRequest request)** → void
   - Creates new parking spot
   - Throws Exception for creation failures

### 4.2 Manager Components

#### VehicleManager
**Location**: `manager/VehicleManager.java`
- **Responsibility**: Vehicle lifecycle management
- **Methods**:
  - `addVehicleEntry(Request request)`: Creates or retrieves vehicle with automatic JPA operations

#### ParkingSpotManager
**Location**: `manager/ParkingSpotManager.java`
- **Responsibility**: Parking spot lifecycle and assignment
- **Key Methods**:
  - `findEmptySpotForVehicle(Vehicle vehicle)`: Finds available spot matching vehicle size
  - `occupy(Vehicle vehicle, ParkingSpot spot)`: Assigns vehicle to spot and marks as unavailable
  - `vacate(Vehicle vehicle, ParkingSpot spot)`: Releases spot and marks as available
  - `createNewSpot(SpotRequest request)`: Creates new parking spot
- **Size-to-Spot Mapping**:
  - SMALL → COMPACT
  - MEDIUM → REGULAR
  - LARGE → OVERSIZED

#### TicketManager
**Location**: `manager/TicketManager.java`
- **Responsibility**: Ticket lifecycle and business logic
- **Key Methods**:
  - `generateTicket(Vehicle vehicle, ParkingSpot spot)`: Creates new ticket at check-in
  - `updatePrice(Ticket ticket, float price)`: Updates ticket with calculated fare
  - `isVehicleAlreadyParked(String licensePlate)`: Validates no active ticket exists
  - `getTicketById(Long id)`: Retrieves ticket for check-out

#### FairCalculationFactory
**Location**: `manager/FairCalculationFactory.java`
- **Responsibility**: Strategy pattern factory for fare calculation
- **Method**: `getFareCalculator(boolean isPeakHour)` → IFareStrategy
  - Returns appropriate fare strategy based on peak hour flag

### 4.3 Fare Strategy Implementations

#### IFareStrategy Interface
**Location**: `interfaces/IFareStrategy.java`
- **Method**: `CalculateFare(Ticket ticket)` → float
  - Calculates parking fare based on entry and exit times

#### BaseFareStrategy
**Location**: `manager/BaseFareStrategy.java`
- Used for off-peak pricing
- Implements base fare calculation logic

#### PeakFareStrategy
**Location**: `manager/PeakFareStrategy.java`
- Used for peak hour pricing
- Implements same calculation with peak pricing rates

### 4.4 ParkingServiceImpl
**Location**: `services/ParkingServiceImpl.java`
- **Responsibility**: Orchestrates services and managers for business workflows
- **Key Logic**:
  - Check-in coordination with validation and spot assignment
  - Check-out coordination with pricing calculation
  - Peak hour detection for strategy selection (7-10 AM and 4-7 PM)

---

## 5. Database Configuration

### 5.1 Database System
- **Type**: PostgreSQL (SQL)
- **JDBC URL**: `jdbc:postgresql://localhost:5432/parkinglot`
- **Default Credentials**:
  - Username: `postgres`
  - Password: `postgres`
- **Database Name**: `parkinglot`

### 5.2 JPA/Hibernate Configuration
- **DDL Auto**: `update` (automatically creates/updates schema)
- **SQL Formatting**: Enabled for readability
- **SQL Display**: Enabled (`show-sql=true`)
- **Dialect**: PostgreSQL

---

## 6. Database Entities & Schema

### 6.1 Vehicle Entity
**Table Name**: `vehicle`
**Primary Key**: `license_plate` (String)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| license_plate | VARCHAR | PK | Vehicle's license plate (unique identifier) |
| size | INTEGER | NOT NULL | Vehicle size (ENUM: 0=SMALL, 1=MEDIUM, 2=LARGE) |
| type | INTEGER | NOT NULL | Vehicle type (ENUM: 0=CAR, 1=TRUCK, 2=MOTORCYCLE) |

**Relationships**:
- One-to-One with ParkingSpot (via foreign key)
- One-to-Many with Ticket

### 6.2 ParkingSpot Entity
**Table Name**: `parking_spot`
**Primary Key**: `id` (Long - Auto-generated)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO | Unique spot identifier |
| is_available | BOOLEAN | NOT NULL | Availability status |
| license_plate_no | VARCHAR | FK | Reference to Vehicle (nullable) |
| type | INTEGER | NOT NULL | Spot type (ENUM: 0=COMPACT, 1=REGULAR, 2=OVERSIZED) |
| price | FLOAT | NOT NULL | Off-peak hourly rate |
| peak_price | FLOAT | NOT NULL | Peak hour rate |

**Relationships**:
- One-to-One with Vehicle (optional)
- One-to-Many with Ticket

**Indexes/Queries**:
- Custom query: `findFirstByVehicleIsNullAndType` - Find available spot by type

### 6.3 Ticket Entity
**Table Name**: `ticket`
**Primary Key**: `id` (Long - Auto-generated with SEQUENCE)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, SEQUENCE | Unique ticket identifier |
| license_plate_no | VARCHAR | FK | Reference to Vehicle |
| parking_spot_no | BIGINT | FK | Reference to ParkingSpot |
| entry_time | TIMESTAMP | NOT NULL | Vehicle entry timestamp |
| exit_time | TIMESTAMP | Nullable | Vehicle exit timestamp |
| is_active | BOOLEAN | NOT NULL | Active status (false on checkout) |
| final_price | FLOAT | Nullable | Calculated parking fare |

**Relationships**:
- Many-to-One with Vehicle
- Many-to-One with ParkingSpot

**Indexes/Queries**:
- Custom query: `findFirstByVehicleLicensePlateAndIsActiveTrueAndExitTimeIsNull` - Find active ticket for vehicle

### 6.4 Entity Relationship Diagram
```
Vehicle (1) -----> (∞) Ticket
  ↓
  (1)
  ↓
ParkingSpot (1) -----> (∞) Ticket

OneToOne: Vehicle ←→ ParkingSpot (optional)
ManyToOne: Ticket → Vehicle
ManyToOne: Ticket → ParkingSpot
```

---

## 7. Data Transfer Objects (DTOs)

### 7.1 Request DTO
**Location**: `dto/Request.java`
**Purpose**: Check-in request payload

**Fields**:
- `licensePlate: String` - Vehicle license plate
- `vehicleType: String` - Vehicle type (CAR, TRUCK, MOTORCYCLE)

### 7.2 SpotRequest DTO
**Location**: `dto/SpotRequest.java`
**Purpose**: Add parking spot request payload

**Fields**:
- `spotId: Long` - Parking spot identifier
- `price: float` - Off-peak hourly rate
- `peakPrice: float` - Peak hour rate
- `spotType: ParkingSpotType` - Spot type (COMPACT, REGULAR, OVERSIZED)

### 7.3 TicketDto DTO
**Location**: `dto/TicketDto.java`
**Purpose**: Ticket response payload

**Fields**:
- `TicketId: Long` - Ticket identifier
- `vehicleNo: String` - Vehicle license plate
- `parkingSpotNo: Long` - Parking spot ID
- `entryTime: Date` - Check-in timestamp
- `exitTime: Date` - Check-out timestamp (nullable during active parking)
- `price: Float` - Calculated fare
- `message: String` - Status/error message
- `hasError: boolean` - Error flag

---

## 8. Enumerations

### 8.1 VehicleType
**Location**: `enums/VehicleType.java`

| Value | Size | Description |
|-------|------|-------------|
| CAR | MEDIUM | Standard passenger vehicle |
| TRUCK | LARGE | Large commercial vehicle |
| MOTORCYCLE | SMALL | Two-wheeled vehicle |

### 8.2 VehicleSize
**Location**: `enums/VehicleSize.java`

| Value | Description |
|-------|-------------|
| SMALL | Motorcycles and compact vehicles |
| MEDIUM | Standard cars |
| LARGE | Trucks and large vehicles |

### 8.3 ParkingSpotType
**Location**: `enums/ParkingSpotType.java`

| Value | Compatible Size | Description |
|-------|-----------------|-------------|
| COMPACT | SMALL | Small spot for motorcycles |
| REGULAR | MEDIUM | Standard spot for cars |
| OVERSIZED | LARGE | Large spot for trucks |

---

## 9. Repository Layer

### 9.1 VehicleRepository
**Location**: `repository/VehicleRepository.java`
- Extends: `JpaRepository<Vehicle, String>`
- **Generic Methods**:
  - `save()`, `saveAndFlush()`, `findById()`, `findAll()`, `delete()`, etc.

### 9.2 ParkingSpotRepository
**Location**: `repository/ParkingSpotRepository.java`
- Extends: `JpaRepository<ParkingSpot, Long>`
- **Custom Method**:
  - `Optional<ParkingSpot> findFirstByVehicleIsNullAndType(ParkingSpotType type)` - Retrieves first available spot of specified type

### 9.3 TicketRepository
**Location**: `repository/TicketRepository.java`
- Extends: `JpaRepository<Ticket, Long>`
- **Custom Method**:
  - `Optional<Ticket> findFirstByVehicleLicensePlateAndIsActiveTrueAndExitTimeIsNull(String licensePlate)` - Retrieves active ticket for vehicle

---

## 10. Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────────┐
│     Controller Layer (REST)          │
│    - ParkingController              │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│     Service Layer                    │
│    - ParkingServiceImpl              │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│     Manager Layer (Business Logic)   │
│    - VehicleManager                 │
│    - ParkingSpotManager             │
│    - TicketManager                  │
│    - FairCalculationFactory         │
│    - IFareStrategy implementations  │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│     Repository Layer (Data Access)   │
│    - VehicleRepository              │
│    - ParkingSpotRepository          │
│    - TicketRepository               │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│     Database Layer                   │
│    - PostgreSQL                     │
└─────────────────────────────────────┘
```

### Design Patterns Used
- **Strategy Pattern**: Fare calculation strategies (BaseFareStrategy, PeakFareStrategy)
- **Factory Pattern**: FareCalculationFactory for strategy selection
- **Builder Pattern**: Entity builders for Vehicle, ParkingSpot, Ticket
- **Repository Pattern**: Data access abstraction through repositories
- **Dependency Injection**: Spring managed components

---

## 11. Key Features & Workflows

### 11.1 Vehicle Check-In Process
1. User provides license plate and vehicle type
2. System validates no active ticket exists
3. Vehicle is added/retrieved in database
4. System finds available spot matching vehicle size
5. Spot is marked as occupied
6. Ticket is generated and returned

### 11.2 Vehicle Check-Out Process
1. User provides ticket ID
2. System retrieves ticket and validates existence
3. Parking spot is vacated and marked available
4. Current time is recorded as exit time
5. Parking duration is calculated
6. Appropriate pricing strategy is selected based on current hour
7. Fare is calculated based on duration and rates
8. Ticket is finalized with calculated price
9. Final ticket is returned to user

### 11.3 Dynamic Pricing
- Off-peak pricing applies outside 7-10 AM and 4-7 PM
- Peak pricing applies during identified peak hours
- Different rates for same duration in peak vs. off-peak
- Supports micro-pricing with 30-minute increments

### 11.4 Parking Spot Management
- Three spot types accommodating different vehicle sizes
- Configurable pricing for each spot
- Automatic spot matching based on vehicle size
- Manual spot creation via API

---

## 12. Future Considerations

### Potential Enhancements
- Redis caching for spot availability and pricing
- Real-time occupancy dashboard
- Monthly/subscription parking options
- Multiple parking lot support
- Integration with payment gateways
- Email notifications for expiry
- Admin dashboard for analytics
- Vehicle maintenance history tracking
- Mobile app integration

---

**Document Version**: 1.0  
**Last Updated**: April 10, 2026  
**Project Status**: Active Development
