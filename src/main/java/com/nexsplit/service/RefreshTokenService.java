package com.nexsplit.service;

import com.nexsplit.exception.SecurityException;
import com.nexsplit.model.RefreshToken;
import com.nexsplit.model.User;
import com.nexsplit.repository.RefreshTokenRepository;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Secure refresh token service with theft detection
 * Implements industrial-grade token rotation security with family tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token.expiration-days:7}")
    private int refreshTokenExpirationDays;

    @Value("${jwt.access-token.expiration-minutes:15}")
    private int accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token.max-family-size:10}")
    private int maxFamilySize;

    @Value("${jwt.refresh-token.max-concurrent-sessions:5}")
    private int maxConcurrentSessions;

    /**
     * Create a deterministic hash for refresh tokens
     * Uses SHA-256 for consistent hashing (unlike BCrypt which uses random salts)
     */
    private String createTokenHash(String tokenValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenValue.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generate a new refresh token for a user
     * Creates a new family for each login session
     */
    @Transactional
    public String generateRefreshToken(String userId, String ipAddress, String userAgent) {
        // Generate unique token
        String tokenValue = UUID.randomUUID().toString();
        String tokenHash = createTokenHash(tokenValue); // Use deterministic hash
        String familyId = UUID.randomUUID().toString(); // New family for each session

        log.debug("Generated refresh token for user: {}. Hash: {}", userId, tokenHash);

        // Create refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .tokenHash(tokenHash)
                .userId(userId)
                .familyId(familyId)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);

        // Return the actual token (not the hash)
        return tokenValue;
    }

    /**
     * Refresh access token with comprehensive theft detection
     * Tracks token families and detects any unauthorized access
     */
    @Transactional
    public RefreshTokenResponse refreshAccessToken(String refreshTokenValue, String ipAddress, String userAgent) {
        // Hash the provided token using deterministic method
        String tokenHash = createTokenHash(refreshTokenValue);

        log.debug("Looking for refresh token with hash: {}", tokenHash);

        // Find the refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.error("Refresh token not found in database. Hash: {}", tokenHash);
                    return new SecurityException("Invalid refresh token");
                });

        // Check if token is valid
        if (!refreshToken.isValid()) {
            if (refreshToken.getIsUsed()) {
                // Token reuse detected - possible theft
                handleTokenTheft(refreshToken);
            }
            throw new SecurityException("Invalid or expired refresh token");
        }

        // CRITICAL: Check for family compromise
        if (isFamilyCompromised(refreshToken)) {
            log.error("Token family compromise detected for user: {}, family: {}",
                    refreshToken.getUserId(), refreshToken.getFamilyId());
            handleTokenTheft(refreshToken);
            throw new SecurityException("Security violation detected - please re-authenticate");
        }

        // Check for suspicious activity
        if (isSuspiciousActivity(refreshToken, ipAddress, userAgent)) {
            log.warn("Suspicious refresh token activity detected for user: {}", refreshToken.getUserId());
            handleTokenTheft(refreshToken);
            throw new SecurityException("Suspicious activity detected");
        }

        // Check concurrent sessions limit
        if (exceedsConcurrentSessions(refreshToken.getUserId())) {
            log.warn("Too many concurrent sessions for user: {}", refreshToken.getUserId());
            handleTokenTheft(refreshToken);
            throw new SecurityException("Too many active sessions - please re-authenticate");
        }

        // Mark token as used
        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);

        // Get user email for access token generation
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new SecurityException("User not found"));

        // Generate new access token with user email
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), "USER");

        log.debug("Generated new access token for user: {} (email: {})", refreshToken.getUserId(), user.getEmail());

        // Generate new refresh token (token rotation) - SAME FAMILY
        String newRefreshToken = generateRefreshTokenInSameFamily(refreshToken.getUserId(),
                refreshToken.getFamilyId(), ipAddress, userAgent);

        log.info("Token refreshed successfully for user: {} (email: {})", refreshToken.getUserId(), user.getEmail());

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * Generate refresh token in the same family (for rotation)
     */
    private String generateRefreshTokenInSameFamily(String userId, String familyId, String ipAddress,
            String userAgent) {
        String tokenValue = UUID.randomUUID().toString();
        String tokenHash = createTokenHash(tokenValue); // Use deterministic hash

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .tokenHash(tokenHash)
                .userId(userId)
                .familyId(familyId) // Same family for rotation
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    /**
     * Check if token family is compromised
     * This is the KEY security check that prevents sequential theft
     * 
     * SECURITY MECHANISM: Token families group related refresh tokens together.
     * Each login session gets a unique family ID, and all token rotations
     * within that session stay in the same family.
     * 
     * THEFT DETECTION STRATEGY:
     * 1. MULTI-SOURCE DETECTION: If tokens in the same family are used from
     * different IP addresses or user agents, it indicates theft
     * 2. RAPID GENERATION DETECTION: If too many tokens are generated quickly,
     * it suggests abuse or automated attacks
     * 
     * PERFORMANCE OPTIMIZATION: Uses database-level aggregation instead of
     * Java streams for better performance with large families.
     * 
     * SCENARIO EXAMPLE:
     * - User logs in on Phone (IP: 192.168.1.100, UA: Mobile) → Family F1
     * - Thief steals token and uses it on Laptop (IP: 192.168.1.100, UA: Desktop)
     * - System detects 2 different sources in same family → FAMILY COMPROMISED
     * - All tokens in Family F1 are revoked immediately
     * 
     * @param currentToken The refresh token being validated
     * @return true if family is compromised, false otherwise
     */
    private boolean isFamilyCompromised(RefreshToken currentToken) {
        String familyId = currentToken.getFamilyId();
        String userId = currentToken.getUserId();

        // OPTIMIZED: Use database-level aggregation for multi-source detection
        // This is much more efficient than Java streams for large families
        if (refreshTokenRepository.hasMultipleSourcesInFamily(familyId)) {
            long uniqueSources = refreshTokenRepository.countUniqueSourcesInFamily(familyId);
            log.error("Family compromise detected: {} unique sources for family {}", uniqueSources, familyId);
            return true;
        }

        // OPTIMIZED: Use database-level counting for rapid generation detection
        // This is much more efficient than Java streams for large families
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);
        long recentTokens = refreshTokenRepository.countRecentTokensInFamily(familyId, cutoffTime);

        if (recentTokens > 3) { // More than 3 tokens in 5 minutes is suspicious
            log.warn("Rapid token generation detected: {} tokens in 5 minutes for family {}", recentTokens, familyId);
            return true;
        }

        return false;
    }

    /**
     * Check if user exceeds concurrent sessions limit
     */
    private boolean exceedsConcurrentSessions(String userId) {
        long activeSessions = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now()).size();
        return activeSessions > maxConcurrentSessions;
    }

    /**
     * Handle token theft by revoking entire family
     * 
     * SECURITY RESPONSE: When theft is detected, this method implements
     * a comprehensive security response to prevent further damage.
     * 
     * RESPONSE STRATEGY:
     * 1. FAMILY-WIDE REVOCATION: All tokens in the compromised family are revoked
     * - Prevents the thief from using any other tokens in the family
     * - Forces legitimate user to re-authenticate
     * 2. SECURITY INCIDENT LOGGING: Detailed logs for security analysis
     * - Records the theft incident with full context
     * - Enables pattern recognition and threat analysis
     * 3. IMMEDIATE ACTION: No delay in response to minimize damage
     * 
     * IMPACT ON LEGITIMATE USERS:
     * - User will be logged out from all devices using tokens from this family
     * - User must re-authenticate to continue using the application
     * - This is a security trade-off: inconvenience vs. protection
     * 
     * @param compromisedToken The token that was compromised
     */
    private void handleTokenTheft(RefreshToken compromisedToken) {
        log.error("Token theft detected for user: {}, family: {}",
                compromisedToken.getUserId(), compromisedToken.getFamilyId());

        // Revoke all tokens in the family
        refreshTokenRepository.revokeAllTokensInFamily(compromisedToken.getFamilyId());

        // Log security incident
        log.error("SECURITY INCIDENT: All tokens in family {} have been revoked due to theft detection",
                compromisedToken.getFamilyId());
    }

    /**
     * Check for suspicious activity
     * 
     * SECURITY MONITORING: This method analyzes token usage patterns to detect
     * potential security threats or abuse scenarios.
     * 
     * DETECTION CRITERIA:
     * 1. IP ADDRESS CHANGES: Monitors if tokens are used from different IP
     * addresses
     * - Legitimate: User moves between networks (home → office)
     * - Suspicious: Sudden changes to unknown IPs
     * 2. USER AGENT CHANGES: Tracks device/browser changes
     * - Legitimate: User switches devices (phone → laptop)
     * - Suspicious: Rapid changes or unknown user agents
     * 3. FAMILY SIZE LIMITS: Prevents token flooding attacks
     * - Normal: 1-3 tokens per family
     * - Suspicious: >10 tokens (potential abuse)
     * 
     * LOGGING STRATEGY: All suspicious activities are logged for security analysis,
     * but not all trigger immediate family revocation (to avoid false positives).
     * 
     * @param token            The refresh token being validated
     * @param currentIp        The current client IP address
     * @param currentUserAgent The current user agent string
     * @return true if suspicious activity detected, false otherwise
     */
    private boolean isSuspiciousActivity(RefreshToken token, String currentIp, String currentUserAgent) {
        // Check if IP address changed significantly
        if (token.getIpAddress() != null && currentIp != null) {
            if (!token.getIpAddress().equals(currentIp)) {
                // Could be legitimate (user moved) but worth logging
                log.warn("IP address changed for refresh token: {} -> {}",
                        token.getIpAddress(), currentIp);
            }
        }

        // Check if user agent changed
        if (token.getUserAgent() != null && currentUserAgent != null) {
            if (!token.getUserAgent().equals(currentUserAgent)) {
                log.warn("User agent changed for refresh token: {} -> {}",
                        token.getUserAgent(), currentUserAgent);
            }
        }

        // Check family size (too many active tokens might indicate abuse)
        long familySize = refreshTokenRepository.countActiveTokensInFamily(token.getFamilyId());
        if (familySize > maxFamilySize) {
            log.warn("Large token family detected: {} tokens for user: {}",
                    familySize, token.getUserId());
            return true;
        }

        return false;
    }

    /**
     * Revoke all tokens for a user (logout)
     */
    @Transactional
    public void revokeAllUserTokens(String userId) {
        List<RefreshToken> userTokens = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
        userTokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(userTokens);
        log.info("All tokens revoked for user: {}", userId);
    }

    /**
     * Scheduled cleanup of expired tokens
     * Runs every day at 2:00 AM
     * 
     * SECURITY CONSIDERATION: This method deletes expired tokens immediately.
     * However, this creates a security gap where theft of expired tokens cannot be
     * detected.
     * 
     * ALTERNATIVE APPROACH: Consider keeping expired tokens for 24 hours to detect
     * theft:
     * - Use: LocalDateTime.now().minusHours(24) instead of LocalDateTime.now()
     * - This allows detection of expired token abuse
     * - Trade-off: Slightly larger database size
     * 
     * @see #cleanupExpiredTokens() for manual cleanup
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2:00 AM
    @Transactional
    public void scheduledCleanupExpiredTokens() {
        try {
            log.info("Starting scheduled cleanup of expired refresh tokens...");
            cleanupExpiredTokens();
            log.info("Scheduled cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up expired tokens (manual or scheduled)
     * 
     * CURRENT IMPLEMENTATION: Deletes all expired tokens immediately
     * This means theft of expired tokens cannot be detected.
     * 
     * SECURITY GAP: If a thief steals an expired token and tries to use it:
     * 1. Token not found in database (already deleted)
     * 2. System returns "Invalid token"
     * 3. No theft detection possible
     * 4. Family compromise not detected
     * 
     * RECOMMENDED ENHANCEMENT: Keep expired tokens for 24 hours:
     * ```java
     * LocalDateTime securityCutoff = LocalDateTime.now().minusHours(24);
     * refreshTokenRepository.deleteExpiredTokens(securityCutoff);
     * ```
     * 
     * This would allow detection of expired token abuse while still maintaining
     * regular cleanup for database performance.
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }

    /**
     * Response class for refresh token operations
     */
    public static class RefreshTokenResponse {
        private final String accessToken;
        private final String refreshToken;

        public RefreshTokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
