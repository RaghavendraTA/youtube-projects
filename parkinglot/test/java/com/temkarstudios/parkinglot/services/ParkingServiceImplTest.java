package com.temkarstudios.parkinglot.services;

import com.temkarstudios.parkinglot.dto.Request;
import com.temkarstudios.parkinglot.interfaces.ParkingService;
import com.temkarstudios.parkinglot.manager.ParkingSpotManager;
import com.temkarstudios.parkinglot.manager.TicketManager;
import com.temkarstudios.parkinglot.manager.VehicleManager;
import com.temkarstudios.parkinglot.model.ParkingSpot;
import com.temkarstudios.parkinglot.model.Ticket;
import com.temkarstudios.parkinglot.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceImplTest {

    @Mock
    private ParkingSpotManager mockParkingSpotManager;

    @Mock
    private VehicleManager mockVehicleManager;

    @Mock
    private TicketManager mockTicketManager;

    @InjectMocks
    private ParkingServiceImpl parkingService;

    // Mock dependencies for setup
    private Vehicle mockVehicle;
    private ParkingSpot mockParkingSpot;
    private Ticket mockTicket;
    private Request mockRequest;

    @BeforeEach
    void setUp() {
        // Setup common mocks for each test
        mockVehicle = mock(Vehicle.class);
        mockParkingSpot = mock(ParkingSpot.class);
        mockTicket = mock(Ticket.class);
        mockRequest = mock(Request.class);
        
        // Default behavior setup (can be overridden in specific tests)
        when(mockVehicleManager.addVehicleEntry(any(Request.class))).thenReturn(mockVehicle);
        when(mockParkingSpotManager.findEmptySpotForVehicle(any(Vehicle.class))).thenReturn(Optional.of(mockParkingSpot));
        when(mockTicketManager.generateTicket(any(Vehicle.class), any(ParkingSpot.class))).thenReturn(mockTicket);
        
        // Setup default ticket/spot states for success path
        when(mockParkingSpot.getLicensePlate(any())).thenReturn(mockVehicle);
        when(mockParkingSpot.getSpotAvailability()).thenReturn(false);
        when(mockTicket.getVehicle()).thenReturn(mockVehicle);
        when(mockTicket.getParkingSpot()).thenReturn(mockParkingSpot);
    }

    // --- Test Cases for enterVehicle ---

    @Test
    void enterVehicle_shouldSuccessfullyCheckInVehicle() throws Exception {
        // Arrange: Setup for success path
        when(mockParkingSpotManager.findEmptySpotForVehicle(mockVehicle)).thenReturn(Optional.of(mockParkingSpot));
        
        // Act
        Ticket result = parkingService.enterVehicle(mockRequest);
        
        // Assert
        verify(mockParkingSpotManager).occupy(mockVehicle, mockParkingSpot);
        verify(mockTicketManager).generateTicket(mockVehicle, mockParkingSpot);
        assertEquals(mockTicket, result, "Should return the generated ticket.");
    }

    @Test
    void enterVehicle_shouldThrowExceptionIfNoSpotAvailable() throws Exception {
        // Arrange: Simulate no spots available
        when(mockParkingSpotManager.findEmptySpotForVehicle(mockVehicle)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            parkingService.enterVehicle(mockRequest);
        });
        assertEquals("No Parking Spot found", exception.getMessage());
        
        // Verify no further actions were taken
        verify(mockParkingSpotManager, never()).occupy(any(), any());
        verify(mockTicketManager, never()).generateTicket(any(), any());
    }

    @Test
    void enterVehicle_shouldRejectIfAlreadyParked() throws Exception {
        // Arrange: Simulate vehicle already parked (This tests the new validation logic)
        when(mockTicketManager.isVehicleAlreadyParked(anyString())).thenReturn(true);
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            parkingService.enterVehicle(mockRequest);
        });
        
        String expectedMessage = "Vehicle with license plate " + "ABC1234" + " is already parked and has an active ticket.";
        assertTrue(exception.getMessage().contains("already parked"));
        
        // Verify no state changes occurred
        verify(mockParkingSpotManager, never()).occupy(any(), any());
        verify(mockTicketManager, never()).generateTicket(any(), any());
    }

    // --- Test Cases for exitVehicle ---

    @Test
    void exitVehicle_shouldSuccessfullyCheckOutVehicle() throws Exception {
        // Arrange: Setup for success path
        Long ticketId = 1L;
        when(mockTicketManager.getTicketById(ticketId)).thenReturn(Optional.of(mockTicket));
        
        // Mocking time difference calculation for predictable pricing (e.g., 1 hour, 1 minute)
        // This requires mocking Date/Time, which is complex. For simplicity, we verify the call sequence.
        
        // Act
        Ticket result = parkingService.exitVehicle(ticketId);
        
        // Assert
        verify(mockParkingSpotManager).vacate(any(Vehicle.class), any(ParkingSpot.class));
        verify(mockTicketManager).updatePrice(eq(mockTicket), anyFloat());
        assertEquals(mockTicket, result, "Should return the updated ticket.");
    }

    @Test
    void exitVehicle_shouldThrowExceptionIfTicketNotFound() throws Exception {
        // Arrange: Simulate ticket not found
        when(mockTicketManager.getTicketById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            parkingService.exitVehicle(999L);
        });
        assertEquals("No ticket found", exception.getMessage());
        
        // Verify no state changes occurred
        verify(mockParkingSpotManager, never()).vacate(any(), any());
        verify(mockTicketManager, never()).updatePrice(any(), anyFloat());
    }
}