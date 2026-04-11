package com.temkarstudios.parkinglot;

import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.dto.SpotRequest;
import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.enums.VehicleType;
import com.temkarstudios.parkinglot.interfaces.ParkingService;
import com.temkarstudios.parkinglot.services.RedisCacheService;
import com.temkarstudios.parkinglot.manager.ParkingSpotManager;
import com.temkarstudios.parkinglot.manager.VehicleManager;
import com.temkarstudios.parkinglot.model.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedisTestConfig.class)
@DisplayName("ParkingLot Service Integration Tests")
class ParkingLotServiceIntegrationTest {

    @Autowired
    private ParkingService parkingService; // real bean

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private ParkingSpotManager parkingSpotManager;

    @Autowired
    private VehicleManager vehicleManager;

    @Autowired
    private com.temkarstudios.parkinglot.repository.ParkingSpotRepository repository;

    @Test
    @DisplayName("Should successfully park a vehicle and return a ticket")
    void testParkVehicle() throws Exception {
        // Arrange: clear repository and create a spot
        repository.deleteAll();
        SpotRequest spotRequest = new SpotRequest(10000L, 5f, 10f, ParkingSpotType.REGULAR);
        parkingSpotManager.createNewSpot(spotRequest);

        // Act: park a vehicle
        Request request = new Request();
        request.setLicensePlate("ABC-123");
        request.setVehicleType(VehicleType.CAR.name());
        Ticket ticket = parkingService.enterVehicle(request);

        // Assert
        assertNotNull(ticket, "Ticket should not be null");
        assertEquals("ABC-123", ticket.getVehicle().getLicensePlate());
        assertNotNull(ticket.getParkingSpot(), "Parking spot should be assigned");
        assertFalse(redisCacheService.getAvailableSpotsForVehicleType(VehicleType.CAR).isEmpty(),
                "There should be available spots after parking");
    }

    @Test
    @DisplayName("Should throw exception when no available spot is found")
    void testParkVehicle_NoSpotAvailable() {
        Request request = new Request();
        request.setLicensePlate("DEF-456");
        request.setVehicleType(VehicleType.CAR.name());

        repository.deleteAll(); // ensure empty

        Exception e = assertThrows(Exception.class, () -> parkingService.enterVehicle(request));
        assertEquals("No Parking Spot found", e.getMessage());
    }

    @Test
    @DisplayName("Should calculate exit fee correctly")
    void testExitVehicle() throws Exception {
        // Arrange: park a vehicle first
        repository.deleteAll();
        SpotRequest spotRequest = new SpotRequest(100000L, 5f, 10f, ParkingSpotType.REGULAR);
        parkingSpotManager.createNewSpot(spotRequest);

        Request request = new Request();
        request.setLicensePlate("GHI-789");
        request.setVehicleType(VehicleType.CAR.name());
        Ticket parked = parkingService.enterVehicle(request);

        // Act: exit vehicle
        Ticket exited = parkingService.exitVehicle(parked.getId());

        // Assert
        assertNotNull(exited.getExitTime(), "Exit time should be set");
        assertTrue(exited.getFinalPrice() >= 0, "Final price should be non‑negative");
        assertTrue(redisCacheService.getAvailableSpotsForVehicleType(VehicleType.CAR)
                .contains(exited.getParkingSpot().getId()),
                "Spot should be returned to available pool");
    }
}