package com.configapp.service;

import com.configapp.dto.LoginRequest;
import com.configapp.dto.RegisterRequest;
import com.configapp.model.Admin;
import com.configapp.repository.AdminRepository;
import com.configapp.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();
    }

    @Test
    void testRegisterNewAdmin() throws ExecutionException, InterruptedException {
        RegisterRequest request = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();

        var result = adminService.register(request).get();

        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
        assertEquals(0L, result.getNumberOfConfigurationsOwned());
        assertTrue(result.getActive());
    }

    @Test
    void testRegisterDuplicateUsername() {
        RegisterRequest request = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();

        assertDoesNotThrow(() -> adminService.register(request).get());

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("test@example.com")
                .password("password456")
                .build();

        assertThrows(Exception.class, () -> adminService.register(duplicateRequest).get());
    }

    @Test
    void testLoginSuccess() throws ExecutionException, InterruptedException {
        // Setup
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();
        adminService.register(registerRequest).get();

        // Test login
        LoginRequest loginRequest = LoginRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();

        var result = adminService.login(loginRequest).get();

        assertNotNull(result.getToken());
        assertNotNull(result.getRefreshToken());
        assertNotNull(result.getExpiresIn());
        assertEquals("test@example.com", result.getUser().getUsername());
    }

    @Test
    void testLoginInvalidCredentials() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();

        assertDoesNotThrow(() -> adminService.register(registerRequest).get());

        LoginRequest loginRequest = LoginRequest.builder()
                .username("test@example.com")
                .password("wrongpassword")
                .build();

        assertThrows(Exception.class, () -> adminService.login(loginRequest).get());
    }

    @Test
    void testGetAdminById() throws ExecutionException, InterruptedException {
        RegisterRequest request = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();

        var registered = adminService.register(request).get();
        var retrieved = adminService.getAdminById(registered.getId()).get();

        assertEquals(registered.getId(), retrieved.getId());
        assertEquals(registered.getUsername(), retrieved.getUsername());
    }

    @Test
    void testGetAdminNotFound() {
        assertThrows(Exception.class, () -> 
            adminService.getAdminById("nonexistent-id").get()
        );
    }
}
