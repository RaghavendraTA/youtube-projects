package com.temkarstudios.ratelimiter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
public class BaseController {
    
    @GetMapping(path = "/test", produces = "application/json")
    public ResponseEntity<String> testEndpoint() {
        try {
            Thread.sleep(1000);
            return ResponseEntity.ok("{\"message\":\"API is allowed\"}");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Request interrupted\",\"details\":\"" + e.getMessage() + "\"}");
        }
    }
}
