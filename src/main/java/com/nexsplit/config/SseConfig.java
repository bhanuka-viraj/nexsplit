package com.nexsplit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Server-Sent Events (SSE).
 * 
 * This class provides configuration properties for SSE connections including
 * timeout settings, heartbeat intervals, and connection limits.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "nexsplit.sse")
@Data
public class SseConfig {

    /**
     * SSE connection timeout in milliseconds (default: 30 seconds).
     */
    private long connectionTimeout = 30000;

    /**
     * Heartbeat interval in milliseconds (default: 10 seconds).
     */
    private long heartbeatInterval = 10000;

    /**
     * Maximum number of connections per user (default: 5).
     */
    private int maxConnectionsPerUser = 5;

    /**
     * Maximum number of total connections (default: 1000).
     */
    private int maxTotalConnections = 1000;

    /**
     * Enable heartbeat messages to keep connections alive.
     */
    private boolean enableHeartbeat = true;

    /**
     * Enable connection statistics logging.
     */
    private boolean enableStatistics = true;

    /**
     * Connection cleanup interval in milliseconds (default: 60 seconds).
     */
    private long cleanupInterval = 60000;

    /**
     * Enable automatic connection cleanup.
     */
    private boolean enableAutoCleanup = true;

    /**
     * Buffer size for event queuing (default: 100).
     */
    private int eventBufferSize = 100;

    /**
     * Enable event buffering for offline users.
     */
    private boolean enableEventBuffering = true;

    /**
     * Maximum number of buffered events per user (default: 50).
     */
    private int maxBufferedEventsPerUser = 50;

    /**
     * Event retention time in milliseconds (default: 5 minutes).
     */
    private long eventRetentionTime = 300000;

    /**
     * Enable event compression.
     */
    private boolean enableCompression = true;

    /**
     * Enable connection rate limiting.
     */
    private boolean enableRateLimiting = true;

    /**
     * Rate limit: connections per minute per IP (default: 10).
     */
    private int rateLimitPerMinute = 10;

    /**
     * Rate limit: connections per hour per IP (default: 100).
     */
    private int rateLimitPerHour = 100;
}
