package com.nexsplit.service;

import com.nexsplit.dto.RateLimitInfo;

/**
 * Service interface for rate limiting operations
 */
public interface RateLimitService {

    /**
     * Check if request is allowed for the given client
     * 
     * @param clientId Unique identifier for the client (email, IP, etc.)
     * @return true if request is allowed, false if rate limit exceeded
     */
    boolean isAllowed(String clientId);

    /**
     * Get rate limit information for the given client
     * 
     * @param clientId Unique identifier for the client
     * @return RateLimitInfo object containing current limits and usage
     */
    RateLimitInfo getRateLimitInfo(String clientId);

    /**
     * Get rate limit for specific endpoint and client
     * 
     * @param clientId Unique identifier for the client
     * @param endpoint API endpoint path
     * @return true if request is allowed, false if rate limit exceeded
     */
    boolean isAllowed(String clientId, String endpoint);

    /**
     * Clean up expired rate limit entries to prevent memory leaks
     */
    void cleanupExpiredEntries();
}
