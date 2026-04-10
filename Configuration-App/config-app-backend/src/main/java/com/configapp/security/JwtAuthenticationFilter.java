package com.configapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT validation for public endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.matches("^/api/v1/admin/(login|register|refresh)$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String userId = null;

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                logger.info("Extracted token from Authorization header");
                
                try {
                    userId = jwtUtil.extractUserId(token);
                    logger.info("Extracted userId from token: " + userId);
                } catch (Exception e) {
                    logger.error("Failed to extract userId from token", e);
                    filterChain.doFilter(request, response);
                    return;
                }
            } else {
                logger.debug("No valid Authorization header found: " + (authHeader != null ? "header exists but doesn't start with Bearer" : "header is null"));
                filterChain.doFilter(request, response);
                return;
            }

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    boolean isValid = jwtUtil.isTokenValid(token);
                    logger.info("Token validation result: " + isValid);
                    
                    if (isValid) {
                        try {
                            logger.info("Token is valid, loading user details for userId: " + userId);
                            UserDetails userDetails = userDetailsService.loadUserById(userId);
                            
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            logger.info("Authentication set successfully for userId: " + userId);
                        } catch (Exception e) {
                            logger.error("Failed to load user details or set authentication for userId: " + userId, e);
                        }
                    } else {
                        logger.warn("Token validation failed for userId: " + userId);
                    }
                } catch (Exception e) {
                    logger.error("Exception during token validation", e);
                }
            } else {
                if (userId == null) {
                    logger.warn("UserId is null, unable to authenticate");
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in JWT authentication filter", e);
        }

        filterChain.doFilter(request, response);
    }
}
