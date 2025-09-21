package com.nexsplit.service;

import com.nexsplit.config.SseConfig;
import com.nexsplit.dto.event.EventNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing Server-Sent Events (SSE) connections.
 * 
 * This service provides comprehensive SSE connection management including
 * connection tracking, heartbeat management, event broadcasting, and
 * connection cleanup. It ensures reliable real-time communication between
 * the server and clients.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseManager {

    private final SseConfig sseConfig;

    // Active SSE connections by user ID
    private final Map<String, List<SseEmitter>> activeConnections = new ConcurrentHashMap<>();

    // Connection statistics
    private final Map<String, ConnectionStats> connectionStats = new ConcurrentHashMap<>();

    // Scheduled executor for heartbeat and cleanup
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * Initialize the SSE manager.
     */
    public void initialize() {
        if (sseConfig.isEnableHeartbeat()) {
            startHeartbeat();
        }

        if (sseConfig.isEnableAutoCleanup()) {
            startCleanup();
        }

        log.info("SSE Manager initialized with config: {}", sseConfig);
    }

    /**
     * Add a new SSE connection for a user.
     * 
     * @param userId  The user ID
     * @param emitter The SSE emitter
     * @return true if connection was added successfully
     */
    public boolean addConnection(String userId, SseEmitter emitter) {
        if (isConnectionLimitReached(userId)) {
            log.warn("Connection limit reached for user: {}", userId);
            return false;
        }

        if (isTotalConnectionLimitReached()) {
            log.warn("Total connection limit reached");
            return false;
        }

        activeConnections.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        updateConnectionStats(userId, true);

        log.debug("Added SSE connection for user: {}, total connections: {}", userId, getTotalConnections());
        return true;
    }

    /**
     * Remove an SSE connection for a user.
     * 
     * @param userId  The user ID
     * @param emitter The SSE emitter
     */
    public void removeConnection(String userId, SseEmitter emitter) {
        List<SseEmitter> connections = activeConnections.get(userId);
        if (connections != null) {
            connections.remove(emitter);
            if (connections.isEmpty()) {
                activeConnections.remove(userId);
            }
        }

        updateConnectionStats(userId, false);
        log.debug("Removed SSE connection for user: {}, total connections: {}", userId, getTotalConnections());
    }

    /**
     * Broadcast an event to all connections for a user.
     * 
     * @param userId The user ID
     * @param event  The event to broadcast
     */
    public void broadcastToUser(String userId, EventNotification notification) {
        List<SseEmitter> connections = activeConnections.get(userId);
        if (connections != null && !connections.isEmpty()) {
            for (SseEmitter emitter : connections) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("event")
                            .data(notification));
                } catch (Exception e) {
                    log.warn("Error sending event to user {}: {}", userId, notification.getEventType(), e);
                    removeConnection(userId, emitter);
                }
            }
        }
    }

    /**
     * Broadcast an event to all active connections.
     * 
     * @param event The event to broadcast
     */
    public void broadcastToAll(EventNotification notification) {
        for (Map.Entry<String, List<SseEmitter>> entry : activeConnections.entrySet()) {
            String userId = entry.getKey();
            broadcastToUser(userId, notification);
        }
    }

    /**
     * Get the number of active connections for a user.
     * 
     * @param userId The user ID
     * @return Number of active connections
     */
    public int getConnectionCount(String userId) {
        List<SseEmitter> connections = activeConnections.get(userId);
        return connections != null ? connections.size() : 0;
    }

    /**
     * Get the total number of active connections.
     * 
     * @return Total number of active connections
     */
    public int getTotalConnections() {
        return activeConnections.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Get connection statistics for a user.
     * 
     * @param userId The user ID
     * @return Connection statistics
     */
    public ConnectionStats getConnectionStats(String userId) {
        return connectionStats.getOrDefault(userId, new ConnectionStats());
    }

    /**
     * Get overall connection statistics.
     * 
     * @return Overall connection statistics
     */
    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalConnections", getTotalConnections());
        stats.put("activeUsers", activeConnections.size());
        stats.put("maxConnectionsPerUser", sseConfig.getMaxConnectionsPerUser());
        stats.put("maxTotalConnections", sseConfig.getMaxTotalConnections());
        stats.put("connectionTimeout", sseConfig.getConnectionTimeout());
        stats.put("heartbeatInterval", sseConfig.getHeartbeatInterval());
        return stats;
    }

    /**
     * Check if connection limit is reached for a user.
     * 
     * @param userId The user ID
     * @return true if limit is reached
     */
    private boolean isConnectionLimitReached(String userId) {
        return getConnectionCount(userId) >= sseConfig.getMaxConnectionsPerUser();
    }

    /**
     * Check if total connection limit is reached.
     * 
     * @return true if limit is reached
     */
    private boolean isTotalConnectionLimitReached() {
        return getTotalConnections() >= sseConfig.getMaxTotalConnections();
    }

    /**
     * Update connection statistics for a user.
     * 
     * @param userId    The user ID
     * @param connected Whether the user is connecting or disconnecting
     */
    private void updateConnectionStats(String userId, boolean connected) {
        ConnectionStats stats = connectionStats.computeIfAbsent(userId, k -> new ConnectionStats());
        if (connected) {
            stats.incrementConnections();
        } else {
            stats.decrementConnections();
        }
        stats.setLastActivity(System.currentTimeMillis());
    }

    /**
     * Start heartbeat service.
     */
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("Error sending heartbeat", e);
            }
        }, sseConfig.getHeartbeatInterval(), sseConfig.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     * Start cleanup service.
     */
    private void startCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupStaleConnections();
            } catch (Exception e) {
                log.error("Error during connection cleanup", e);
            }
        }, sseConfig.getCleanupInterval(), sseConfig.getCleanupInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     * Send heartbeat to all active connections.
     */
    private void sendHeartbeat() {
        for (Map.Entry<String, List<SseEmitter>> entry : activeConnections.entrySet()) {
            String userId = entry.getKey();
            List<SseEmitter> connections = entry.getValue();

            for (SseEmitter emitter : connections) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("ping"));
                } catch (Exception e) {
                    log.debug("Error sending heartbeat to user {}: {}", userId, e.getMessage());
                    removeConnection(userId, emitter);
                }
            }
        }
    }

    /**
     * Clean up stale connections.
     */
    private void cleanupStaleConnections() {
        long currentTime = System.currentTimeMillis();
        long staleThreshold = currentTime - sseConfig.getConnectionTimeout();

        for (Map.Entry<String, List<SseEmitter>> entry : activeConnections.entrySet()) {
            String userId = entry.getKey();
            List<SseEmitter> connections = entry.getValue();

            connections.removeIf(emitter -> {
                try {
                    // Try to send a test message to check if connection is alive
                    emitter.send(SseEmitter.event().name("test").data("test"));
                    return false;
                } catch (Exception e) {
                    log.debug("Removing stale connection for user: {}", userId);
                    return true;
                }
            });

            if (connections.isEmpty()) {
                activeConnections.remove(userId);
            }
        }

        log.debug("Connection cleanup completed. Active connections: {}", getTotalConnections());
    }

    /**
     * Connection statistics data class.
     */
    public static class ConnectionStats {
        private int connectionCount = 0;
        private long lastActivity = System.currentTimeMillis();
        private long totalConnections = 0;
        private long totalDisconnections = 0;

        public void incrementConnections() {
            connectionCount++;
            totalConnections++;
            lastActivity = System.currentTimeMillis();
        }

        public void decrementConnections() {
            connectionCount = Math.max(0, connectionCount - 1);
            totalDisconnections++;
            lastActivity = System.currentTimeMillis();
        }

        // Getters and setters
        public int getConnectionCount() {
            return connectionCount;
        }

        public void setConnectionCount(int connectionCount) {
            this.connectionCount = connectionCount;
        }

        public long getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(long lastActivity) {
            this.lastActivity = lastActivity;
        }

        public long getTotalConnections() {
            return totalConnections;
        }

        public void setTotalConnections(long totalConnections) {
            this.totalConnections = totalConnections;
        }

        public long getTotalDisconnections() {
            return totalDisconnections;
        }

        public void setTotalDisconnections(long totalDisconnections) {
            this.totalDisconnections = totalDisconnections;
        }
    }
}
