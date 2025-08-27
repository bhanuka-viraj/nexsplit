package com.nexsplit.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for structured logging optimized for Kibana integration
 * Provides JSON-formatted logs with consistent structure and correlation IDs
 */
@Component
@Slf4j
public class StructuredLoggingUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Log a business event with structured data
     * 
     * @param eventType      The type of business event
     * @param userId         The user ID (masked)
     * @param action         The action performed
     * @param result         The result of the action
     * @param additionalData Additional context data
     */
    public static void logBusinessEvent(String eventType, String userId, String action, String result,
            Map<String, Object> additionalData) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("correlationId", getCorrelationId());
        logData.put("eventType", eventType);
        logData.put("userId", LoggingUtil.maskUserId(userId));
        logData.put("action", action);
        logData.put("result", result);
        logData.put("level", "INFO");

        if (additionalData != null) {
            logData.putAll(additionalData);
        }

        log.info("BUSINESS_EVENT: {}", toJson(logData));
    }

    /**
     * Log a security event with structured data
     * 
     * @param eventType The type of security event
     * @param userId    The user ID (masked)
     * @param ipAddress The client IP address
     * @param userAgent The user agent string
     * @param severity  The severity level (LOW, MEDIUM, HIGH, CRITICAL)
     * @param details   Additional security details
     */
    public static void logSecurityEvent(String eventType, String userId, String ipAddress, String userAgent,
            String severity, Map<String, Object> details) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("correlationId", getCorrelationId());
        logData.put("eventType", eventType);
        logData.put("category", "SECURITY");
        logData.put("userId", LoggingUtil.maskUserId(userId));
        logData.put("ipAddress", ipAddress);
        logData.put("userAgent", userAgent);
        logData.put("severity", severity);
        logData.put("level", "WARN");

        if (details != null) {
            logData.putAll(details);
        }

        log.warn("SECURITY_EVENT: {}", toJson(logData));
    }

    /**
     * Log a performance event with structured data
     * 
     * @param methodName     The method name
     * @param durationMs     The execution duration in milliseconds
     * @param status         The execution status (SUCCESS, FAILURE, TIMEOUT)
     * @param additionalData Additional performance data
     */
    public static void logPerformanceEvent(String methodName, long durationMs, String status,
            Map<String, Object> additionalData) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("correlationId", getCorrelationId());
        logData.put("eventType", "PERFORMANCE");
        logData.put("category", "PERFORMANCE");
        logData.put("methodName", methodName);
        logData.put("durationMs", durationMs);
        logData.put("status", status);
        logData.put("level", durationMs > 1000 ? "WARN" : "DEBUG");

        if (additionalData != null) {
            logData.putAll(additionalData);
        }

        if (durationMs > 1000) {
            log.warn("PERFORMANCE_EVENT: {}", toJson(logData));
        } else {
            log.debug("PERFORMANCE_EVENT: {}", toJson(logData));
        }
    }

    /**
     * Log an error event with structured data
     * 
     * @param errorType    The type of error
     * @param errorMessage The error message
     * @param stackTrace   The stack trace (truncated)
     * @param context      Additional error context
     */
    public static void logErrorEvent(String errorType, String errorMessage, String stackTrace,
            Map<String, Object> context) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("correlationId", getCorrelationId());
        logData.put("eventType", "ERROR");
        logData.put("category", "ERROR");
        logData.put("errorType", errorType);
        logData.put("errorMessage", errorMessage);
        logData.put("stackTrace", truncateStackTrace(stackTrace));
        logData.put("level", "ERROR");

        if (context != null) {
            logData.putAll(context);
        }

        log.error("ERROR_EVENT: {}", toJson(logData));
    }

    /**
     * Log an HTTP request event
     * 
     * @param method     The HTTP method
     * @param uri        The request URI
     * @param statusCode The response status code
     * @param durationMs The request duration
     * @param userId     The user ID (masked)
     * @param ipAddress  The client IP address
     */
    public static void logHttpEvent(String method, String uri, int statusCode, long durationMs, String userId,
            String ipAddress) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("correlationId", getCorrelationId());
        logData.put("eventType", "HTTP_REQUEST");
        logData.put("category", "HTTP");
        logData.put("method", method);
        logData.put("uri", uri);
        logData.put("statusCode", statusCode);
        logData.put("durationMs", durationMs);
        logData.put("userId", LoggingUtil.maskUserId(userId));
        logData.put("ipAddress", ipAddress);
        logData.put("level", statusCode >= 400 ? "WARN" : "INFO");

        if (statusCode >= 400) {
            log.warn("HTTP_EVENT: {}", toJson(logData));
        } else {
            log.info("HTTP_EVENT: {}", toJson(logData));
        }
    }

    /**
     * Get current correlation ID for request tracing
     */
    private static String getCorrelationId() {
        // Get correlation ID from MDC (set by CorrelationIdFilter)
        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.trim().isEmpty()) {
            // Fallback to generating new one if not in MDC
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    /**
     * Convert map to JSON string
     */
    private static String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize log data\"}";
        }
    }

    /**
     * Truncate stack trace for logging
     */
    private static String truncateStackTrace(String stackTrace) {
        if (stackTrace == null || stackTrace.length() <= 500) {
            return stackTrace;
        }
        return stackTrace.substring(0, 500) + "... [truncated]";
    }
}
