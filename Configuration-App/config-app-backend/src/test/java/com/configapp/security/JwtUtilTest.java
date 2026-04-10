package com.configapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateToken() {
        String userId = UUID.randomUUID().toString();
        String token = jwtUtil.generateToken(userId);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testExtractUserId() {
        String userId = UUID.randomUUID().toString();
        String token = jwtUtil.generateToken(userId);

        String extractedUserId = jwtUtil.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void testIsTokenValid() {
        String userId = UUID.randomUUID().toString();
        String token = jwtUtil.generateToken(userId);

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void testIsInvalidTokenInvalid() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }

    @Test
    void testGenerateRefreshToken() {
        String userId = UUID.randomUUID().toString();
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
        assertTrue(jwtUtil.isTokenValid(refreshToken));
    }

    @Test
    void testTokenExpiration() {
        String userId = UUID.randomUUID().toString();
        Long expirationTime = jwtUtil.getExpirationTime();

        assertNotNull(expirationTime);
        assertTrue(expirationTime > 0);
    }
}
