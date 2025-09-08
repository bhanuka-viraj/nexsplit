package com.nexsplit.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {
    private final SecretKey secretKey;
    private final int accessTokenExpirationMinutes;
    private final int refreshTokenExpirationDays;

    public JwtUtil(@Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.expiration-minutes:15}") int accessTokenExpirationMinutes,
            @Value("${jwt.refresh-token.expiration-days:7}") int refreshTokenExpirationDays) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long for HS256");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public String generateAccessToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public String generateAccessToken(String userId, String email, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate JWT-based refresh token with enhanced security claims
     * 
     * SECURITY FEATURES:
     * - Family ID for theft detection
     * - User ID for database tracking
     * - Token ID for unique identification
     * - User Agent for security monitoring
     * - Issued at and expiration for validation
     */
    public String generateRefreshToken(String userId, String email, String familyId, String userAgent) {
        String tokenId = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(tokenId) // Unique token identifier
                .subject(email) // User email
                .claim("userId", userId) // User ID for database operations
                .claim("familyId", familyId) // Family ID for theft detection
                .claim("userAgent", userAgent) // User agent for security monitoring
                .claim("type", "refresh") // Token type for validation
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS)))
                .signWith(secretKey)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Get email from token (for backward compatibility)
     * Note: Email is now stored as a claim, not as subject
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * Extract email from the current JWT token in the request context
     * This is useful for controllers that need the email but only have UserDetails
     */
    public String getEmailFromCurrentToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null) {
                log.warn("No request context available");
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("No valid Authorization header found");
                return null;
            }

            String token = authHeader.substring(7);
            return getEmailFromToken(token);
        } catch (Exception e) {
            log.error("Failed to extract email from current token: {}", e.getMessage());
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Extract user ID from refresh token
     */
    public String getUserIdFromRefreshToken(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    /**
     * Extract family ID from refresh token
     */
    public String getFamilyIdFromRefreshToken(String token) {
        return parseClaims(token).get("familyId", String.class);
    }

    /**
     * Extract token ID from refresh token
     */
    public String getTokenIdFromRefreshToken(String token) {
        return parseClaims(token).getId();
    }

    /**
     * Extract user agent from refresh token
     */
    public String getUserAgentFromRefreshToken(String token) {
        return parseClaims(token).get("userAgent", String.class);
    }

    /**
     * Validate refresh token and check if it's the correct type
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException
                | io.jsonwebtoken.ExpiredJwtException | IllegalArgumentException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}