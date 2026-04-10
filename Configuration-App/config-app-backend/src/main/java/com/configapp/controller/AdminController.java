package com.configapp.controller;

import com.configapp.dto.AdminDto;
import com.configapp.dto.LoginRequest;
import com.configapp.dto.LoginResponse;
import com.configapp.dto.RegisterRequest;
import com.configapp.dto.RefreshTokenRequest;
import com.configapp.dto.UpdateAdminRequest;
import com.configapp.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<AdminDto>> register(@Valid @RequestBody RegisterRequest request) {
        return adminService.register(request)
                .thenApply(dto -> ResponseEntity.status(HttpStatus.OK).body(dto));
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return adminService.login(request)
                .thenApply(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @PostMapping("/refresh")
    public CompletableFuture<ResponseEntity<LoginResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        return adminService.refresh(request)
                .thenApply(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<AdminDto>> getAdmin(@PathVariable String id) {
        return adminService.getAdminById(id)
                .thenApply(dto -> ResponseEntity.status(HttpStatus.OK).body(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminDto> updateAdmin(
            @PathVariable String id,
            @RequestBody UpdateAdminRequest request,
            Authentication authentication) {
        
        // Verify user is updating their own profile or is authorized
        if (!authentication.getName().equals(id)) {
            throw new IllegalArgumentException("Cannot update other user's profile");
        }

        try {
            AdminDto dto = adminService.updateAdmin(id, request).get();
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error updating admin", e);
        }
    }
}
