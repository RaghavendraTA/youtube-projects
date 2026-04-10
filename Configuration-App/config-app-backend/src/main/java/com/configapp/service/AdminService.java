package com.configapp.service;

import com.configapp.dto.AdminDto;
import com.configapp.dto.LoginRequest;
import com.configapp.dto.LoginResponse;
import com.configapp.dto.RegisterRequest;
import com.configapp.dto.UpdateAdminRequest;
import com.configapp.dto.RefreshTokenRequest;
import com.configapp.model.Admin;
import com.configapp.repository.AdminRepository;
import com.configapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Async
    public CompletableFuture<AdminDto> register(RegisterRequest request) {
        if (adminRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Admin admin = Admin.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .numberOfConfigurationsOwned(0L)
                .registeredOn(LocalDateTime.now())
                .active(true)
                .build();

        Admin savedAdmin = adminRepository.save(admin);
        return CompletableFuture.completedFuture(toAdminDto(savedAdmin));
    }

    @Async
    public CompletableFuture<LoginResponse> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(admin.getId());
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId());
        Long expiresIn = jwtUtil.getExpirationTime();

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(toAdminDto(admin))
                .build();

        return CompletableFuture.completedFuture(response);
    }

    @Async
    public CompletableFuture<LoginResponse> refresh(RefreshTokenRequest request) {
        String userId = jwtUtil.extractUserId(request.getRefreshToken());
        
        if (!jwtUtil.isTokenValid(request.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Admin admin = adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(admin.getId());
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId());
        Long expiresIn = jwtUtil.getExpirationTime();

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(toAdminDto(admin))
                .build();

        return CompletableFuture.completedFuture(response);
    }

    @Async
    public CompletableFuture<AdminDto> getAdminById(String id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        return CompletableFuture.completedFuture(toAdminDto(admin));
    }

    @Async
    public CompletableFuture<AdminDto> updateAdmin(String id, UpdateAdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (request.getUsername() != null) {
            if (adminRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }
            admin.setUsername(request.getUsername());
        }

        if (request.getActive() != null) {
            admin.setActive(request.getActive());
        }

        Admin updatedAdmin = adminRepository.save(admin);
        return CompletableFuture.completedFuture(toAdminDto(updatedAdmin));
    }

    private AdminDto toAdminDto(Admin admin) {
        return AdminDto.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .numberOfConfigurationsOwned(admin.getNumberOfConfigurationsOwned())
                .registeredOn(admin.getRegisteredOn())
                .active(admin.getActive())
                .build();
    }
}
