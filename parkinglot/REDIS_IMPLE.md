# Redis implementation for Parkinglot
Read through cache
> Note: ParkingSpots are defined once and updated in db before the system warms up. But there is also an api to add new spot only if the building construction team makes a new space. which means that api almost never gets invoked.

### Cache design

**Vehicle Avilable Spot Check**
Key : VehicleType
Value : [SpotId1, SpotId2, ...]

**Occupied Logic**
Key: VehicleLicensePlate
Value: { Spot, CarType, EntryTime }

### Case: When vehicle enter the parking lot
- Check if Vehicle has spot avilable using Redis key: carType, 
    - If redis says `not avilable` then fallback to db to re-confirm.
- Once vehicle Enters: 
    - generate a ticket, 
    - Update the redis (Avilable spot and Occupied keys), 
    - issue the ticket (Return the api response).
    - update db (vehicle, parkingSpot, ticket) in @Async mode

### Case: When vehicle exits the parking lot
- Check redis to get 
    - vehicle type, 
    - entry time, 
    - spot type 
    - compute the fare based on strategy
    - Update the redis by removing the cache key VehicleLicensePlate
    - Update avilable spot array in redis with spotId
    - Return the ticket.
    - Then update the (vehicle, parkingSpot, ticket) in db.
