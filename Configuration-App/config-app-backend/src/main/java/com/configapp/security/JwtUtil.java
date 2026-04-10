package com.configapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:your-super-secret-key-change-this-in-production-environment}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")  // 1 hour default
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")  // 7 days default
    private Long refreshTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, userId, jwtExpiration);
    }

    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userId, refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
          long nowMillis = System.currentTimeMillis();
          Date now = new Date(nowMillis);
          Date expirationDate = new Date(nowMillis + expiration);

          claims.put("sub", subject); // Set subject in claims explicitly

          return Jwts.builder()
                  .setClaims(claims)
                  .setIssuedAt(now)
                  .setExpiration(expirationDate)
                  .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                  .compact();
      }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getExpirationTime() {
        return jwtExpiration;
    }
}
