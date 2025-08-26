package com.nexsplit.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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

    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
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

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
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