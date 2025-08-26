package com.nexsplit.service.impl;

import com.nexsplit.dto.RateLimitInfo;
import com.nexsplit.service.RateLimitService;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of rate limiting service
 * Uses ConcurrentHashMap for thread-safe operations
 */
@Service
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    // Store rate limits in memory using thread-safe map
    private final Map<String, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();

    // Default rate limits
    private static final int DEFAULT_REQUESTS = 100;
    private static final int DEFAULT_WINDOW = 60; // seconds

    // Endpoint-specific rate limits
    private static final Map<String, Integer> ENDPOINT_LIMITS = Map.of(
            "/api/v1/auth/login", 10, // 10 login attempts per minute
            "/api/v1/auth/register", 5, // 5 registrations per minute
            "/api/v1/auth/verify-email", 10, // 10 email verifications per minute
            "/api/v1/auth/reset-password", 5, // 5 password resets per minute
            "/api/v1/expenses", 200, // 200 expense operations per minute
            "/api/v1/nex", 100, // 100 nex operations per minute
            "/api/v1/events/stream", 100 // 100 SSE connections per minute
    );

    @Override
    public boolean isAllowed(String clientId) {
        return isAllowed(clientId, null);
    }

    @Override
    public boolean isAllowed(String clientId, String endpoint) {
        String key = createKey(clientId, endpoint);
        RateLimitInfo info = rateLimits.get(key);

        if (info == null) {
            int limit = getLimitForEndpoint(endpoint);
            info = new RateLimitInfo(limit, DEFAULT_WINDOW);
            rateLimits.put(key, info);
        }

        boolean allowed = info.isAllowed();

        // Log rate limit events for monitoring
        if (!allowed) {
            StructuredLoggingUtil.logSecurityEvent(
                    "RATE_LIMIT_EXCEEDED",
                    clientId,
                    "unknown",
                    "unknown",
                    "MEDIUM",
                    Map.of(
                            "endpoint", endpoint != null ? endpoint : "default",
                            "limit", info.getMaxRequests(),
                            "window", DEFAULT_WINDOW,
                            "remaining", info.getRemaining()));
        }

        return allowed;
    }

    @Override
    public RateLimitInfo getRateLimitInfo(String clientId) {
        return rateLimits.get(clientId);
    }

    @Override
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupExpiredEntries() {
        int beforeSize = rateLimits.size();

        rateLimits.entrySet().removeIf(entry -> entry.getValue().isWindowExpired());

        int afterSize = rateLimits.size();
        int removed = beforeSize - afterSize;

        if (removed > 0) {
            log.debug("Cleaned up {} expired rate limit entries. Current size: {}",
                    removed, afterSize);

            StructuredLoggingUtil.logBusinessEvent(
                    "RATE_LIMIT_CLEANUP",
                    "system",
                    "cleanup_expired_entries",
                    "SUCCESS",
                    Map.of(
                            "entriesRemoved", removed,
                            "currentSize", afterSize));
        }
    }

    /**
     * Create a unique key for rate limiting
     */
    private String createKey(String clientId, String endpoint) {
        if (endpoint == null) {
            return clientId;
        }
        return clientId + ":" + endpoint;
    }

    /**
     * Get rate limit for specific endpoint
     */
    private int getLimitForEndpoint(String endpoint) {
        if (endpoint == null) {
            return DEFAULT_REQUESTS;
        }

        // Find matching endpoint pattern
        for (Map.Entry<String, Integer> entry : ENDPOINT_LIMITS.entrySet()) {
            if (endpoint.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return DEFAULT_REQUESTS;
    }

    /**
     * Get current memory usage statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
                "totalEntries", rateLimits.size(),
                "memoryUsage", estimateMemoryUsage(),
                "endpointLimits", ENDPOINT_LIMITS,
                "defaultLimit", DEFAULT_REQUESTS,
                "defaultWindow", DEFAULT_WINDOW);
    }

    /**
     * Estimate memory usage in bytes
     */
    private long estimateMemoryUsage() {
        // Rough estimation: each entry ~200 bytes
        return rateLimits.size() * 200L;
    }
}
