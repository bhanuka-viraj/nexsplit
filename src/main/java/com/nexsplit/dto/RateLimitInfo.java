package com.nexsplit.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Stores rate limiting information for a specific client
 * Manages request count, time windows, and limit validation
 */
@Getter
public class RateLimitInfo {
    private int requestCount;
    private LocalDateTime windowStart;
    private final int maxRequests;
    private final int windowSeconds;

    public RateLimitInfo(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.windowStart = LocalDateTime.now();
        this.requestCount = 0;
    }

    /**
     * Check if request is allowed and increment count if so
     */
    public boolean isAllowed() {
        // Check if window expired
        if (isWindowExpired()) {
            resetWindow();
        }

        // Check if under limit
        if (requestCount < maxRequests) {
            requestCount++;
            return true;
        }

        return false;
    }

    /**
     * Check if current time window has expired
     */
    public boolean isWindowExpired() {
        return LocalDateTime.now().isAfter(
                windowStart.plusSeconds(windowSeconds));
    }

    /**
     * Reset the time window and request count
     */
    private void resetWindow() {
        this.windowStart = LocalDateTime.now();
        this.requestCount = 0;
    }

    /**
     * Get remaining requests allowed in current window
     */
    public int getRemaining() {
        return Math.max(0, maxRequests - requestCount);
    }

    /**
     * Get Unix timestamp when rate limit resets
     */
    public long getResetTime() {
        return windowStart.plusSeconds(windowSeconds)
                .atZone(ZoneId.systemDefault())
                .toEpochSecond();
    }

    /**
     * Get seconds until rate limit resets
     */
    public long getSecondsUntilReset() {
        LocalDateTime resetTime = windowStart.plusSeconds(windowSeconds);
        return java.time.Duration.between(LocalDateTime.now(), resetTime).getSeconds();
    }
}
