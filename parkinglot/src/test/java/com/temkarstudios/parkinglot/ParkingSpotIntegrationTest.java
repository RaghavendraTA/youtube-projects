package com.temkarstudios.parkinglot;

import com.temkarstudios.parkinglot.enums.ParkingSpotType;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("ParkingSpot Integration Tests")
class ParkingSpotIntegrationTest {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clear parking_spot table
        jdbcTemplate.execute("DELETE FROM ticket");

        // Clear parking_spot table
        jdbcTemplate.execute("DELETE FROM parking_spot");

        // Clear parking_spot table
        jdbcTemplate.execute("DELETE FROM vehicle");

        // Reset sequence (H2 database)
        jdbcTemplate.execute("ALTER SEQUENCE ticket_seq RESTART WITH 1");
        
        // Reset sequence (H2 database)
        jdbcTemplate.execute("ALTER SEQUENCE parking_spot_seq RESTART WITH 1");
        
        // Insert 10 different parking spot records
        insertTestParkingSpots();
    }

    private void insertTestParkingSpots() {
        ParkingSpotType[] spotTypes = {
            ParkingSpotType.COMPACT,
            ParkingSpotType.REGULAR,
            ParkingSpotType.OVERSIZED,
            ParkingSpotType.COMPACT,
            ParkingSpotType.REGULAR,
            ParkingSpotType.OVERSIZED,
            ParkingSpotType.COMPACT,
            ParkingSpotType.REGULAR,
            ParkingSpotType.OVERSIZED,
            ParkingSpotType.COMPACT
        };

        float[] prices = {5.0f, 7.5f, 10.0f, 5.0f, 7.5f, 10.0f, 5.0f, 7.5f, 10.0f, 5.0f};

        for (int i = 0; i < 10; i++) {
            ParkingSpot spot = new ParkingSpot();
            spot.setSpotAvailability(true);
            spot.setPrice(prices[i]);
            
            // Use reflection to set the type since there's no setter
            try {
                var field = ParkingSpot.class.getDeclaredField("type");
                field.setAccessible(true);
                field.set(spot, spotTypes[i]);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            parkingSpotRepository.save(spot);
        }
    }

    @Test
    @DisplayName("Should have 10 parking spots after initialization")
    void testParkingSpotCountAfterInitialization() {
        List<ParkingSpot> spots = parkingSpotRepository.findAll();
        assertEquals(10, spots.size(), "Should have 10 parking spots");
    }

    @Test
    @DisplayName("Should have correct distribution of parking spot types")
    void testParkingSpotTypeDistribution() {
        List<ParkingSpot> spots = parkingSpotRepository.findAll();
        
        long compactCount = spots.stream()
            .filter(s -> s.getType() == ParkingSpotType.COMPACT)
            .count();
        
        long regularCount = spots.stream()
            .filter(s -> s.getType() == ParkingSpotType.REGULAR)
            .count();
        
        long oversizedCount = spots.stream()
            .filter(s -> s.getType() == ParkingSpotType.OVERSIZED)
            .count();

        assertEquals(4, compactCount, "Should have 4 COMPACT spots");
        assertEquals(3, regularCount, "Should have 3 REGULAR spots");
        assertEquals(3, oversizedCount, "Should have 3 OVERSIZED spots");
    }

    @Test
    @DisplayName("Should have correct price for each spot type")
    void testParkingSpotPrices() {
        List<ParkingSpot> spots = parkingSpotRepository.findAll();
        
        spots.forEach(spot -> {
            switch (spot.getType()) {
                case COMPACT:
                    assertEquals(5.0f, spot.getPrice(), "COMPACT spots should be 5.0");
                    break;
                case REGULAR:
                    assertEquals(7.5f, spot.getPrice(), "REGULAR spots should be 7.5");
                    break;
                case OVERSIZED:
                    assertEquals(10.0f, spot.getPrice(), "OVERSIZED spots should be 10.0");
                    break;
            }
        });
    }

    @Test
    @DisplayName("Should have all spots available")
    void testAllSpotsAreAvailable() {
        List<ParkingSpot> spots = parkingSpotRepository.findAll();
        
        boolean allAvailable = spots.stream()
            .allMatch(ParkingSpot::isAvailable);
        
        assertTrue(allAvailable, "All spots should be available");
    }

    @Test
    @DisplayName("Should reset data on each test")
    void testDataResetBetweenTests() {
        // This test verifies that BeforeEach resets the data correctly
        List<ParkingSpot> spots = parkingSpotRepository.findAll();
        assertEquals(10, spots.size(), "Should always have exactly 10 spots after setup");
    }
}
