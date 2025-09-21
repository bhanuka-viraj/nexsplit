package com.nexsplit.service.impl;

import com.nexsplit.model.AuditEvent;
import com.nexsplit.repository.AuditEventRepository;
import com.nexsplit.service.AuditService;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Audit service implementation with async operations
 * Uses virtual threads for better scalability and performance
 * Provides comprehensive audit logging for compliance and security
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    /**
     * Generate a thread-safe unique ID for audit events
     * Uses timestamp + random component to prevent collisions
     */
    private String generateUniqueId() {
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return timestamp + "-" + random + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Save audit event with direct persistence
     * Uses repository save() for async compatibility
     */
    private void saveAuditEvent(AuditEvent auditEvent) {
        auditEventRepository.save(auditEvent);
    }

    /**
     * Log security event asynchronously
     * 
     * BENEFITS:
     * - Non-blocking security monitoring
     * - Database audit trail for compliance
     * - Comprehensive security tracking
     * - Fallback to structured logging
     */
    @Async("asyncExecutor")
    @Override
    public void logSecurityEventAsync(String userId, String eventType, String details) {
        try {
            log.debug("Logging security event for user: {}, type: {}", userId, eventType);

            // Create audit event entity with thread-safe ID generation
            AuditEvent auditEvent = AuditEvent.builder()
                    .id(generateUniqueId())
                    .userId(userId)
                    .eventType(eventType)
                    .eventCategory("SECURITY")
                    .eventDetails(details)
                    .severityLevel("HIGH")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Save to database with direct persistence
            saveAuditEvent(auditEvent);

            // Also log to structured logging for monitoring
            StructuredLoggingUtil.logBusinessEvent(
                    "AUDIT_EVENT",
                    userId,
                    "SECURITY_AUDIT",
                    "SUCCESS",
                    java.util.Map.of(
                            "eventType", eventType,
                            "details", details,
                            "auditId", auditEvent.getId(),
                            "thread", Thread.currentThread().getName()));

            log.debug("Security event logged successfully for user: {}", userId);

        } catch (Exception e) {
            log.error("Failed to log security event for user: {}", userId, e);

            // Fallback to structured logging if audit fails
            StructuredLoggingUtil.logErrorEvent(
                    "AUDIT_FAILURE",
                    "Failed to log security event",
                    e.getMessage(),
                    java.util.Map.of("userId", userId, "eventType", eventType));
        }
    }

    /**
     * Log user action asynchronously
     * 
     * BENEFITS:
     * - Non-blocking user action tracking
     * - Business audit trail
     * - User behavior analytics
     * - Compliance requirements
     */
    @Async("asyncExecutor")
    @Override
    public void logUserActionAsync(String userId, String action, String details) {
        try {
            log.debug("Logging user action for user: {}, action: {}", userId, action);

            // Create audit event entity with thread-safe ID generation
            AuditEvent auditEvent = AuditEvent.builder()
                    .id(generateUniqueId())
                    .userId(userId)
                    .eventType(action)
                    .eventCategory("USER_ACTION")
                    .eventDetails(details)
                    .severityLevel("MEDIUM")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Save to database with direct persistence
            saveAuditEvent(auditEvent);

            // Also log to structured logging for monitoring
            StructuredLoggingUtil.logBusinessEvent(
                    "AUDIT_EVENT",
                    userId,
                    "USER_ACTION_AUDIT",
                    "SUCCESS",
                    java.util.Map.of(
                            "action", action,
                            "details", details,
                            "auditId", auditEvent.getId(),
                            "thread", Thread.currentThread().getName()));

            log.debug("User action logged successfully for user: {}", userId);

        } catch (Exception e) {
            log.error("Failed to log user action for user: {}", userId, e);

            // Fallback to structured logging if audit fails
            StructuredLoggingUtil.logErrorEvent(
                    "AUDIT_FAILURE",
                    "Failed to log user action",
                    e.getMessage(),
                    java.util.Map.of("userId", userId, "action", action));
        }
    }

    /**
     * Log system event asynchronously
     * 
     * BENEFITS:
     * - Non-blocking system monitoring
     * - System health tracking
     * - Maintenance logging
     * - Performance monitoring
     */
    @Async("asyncExecutor")
    @Override
    public void logSystemEventAsync(String eventType, String details) {
        try {
            log.debug("Logging system event: {}", eventType);

            // Create audit event entity with thread-safe ID generation
            AuditEvent auditEvent = AuditEvent.builder()
                    .id(generateUniqueId())
                    .userId(null) // System events don't have a specific user (NULL is allowed by FK constraint)
                    .eventType(eventType)
                    .eventCategory("SYSTEM")
                    .eventDetails(details)
                    .severityLevel("LOW")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Save to database with direct persistence
            saveAuditEvent(auditEvent);

            // Also log to structured logging for monitoring
            StructuredLoggingUtil.logBusinessEvent(
                    "AUDIT_EVENT",
                    "SYSTEM",
                    "SYSTEM_AUDIT",
                    "SUCCESS",
                    java.util.Map.of(
                            "eventType", eventType,
                            "details", details,
                            "auditId", auditEvent.getId(),
                            "thread", Thread.currentThread().getName()));

            log.debug("System event logged successfully: {}", eventType);

        } catch (Exception e) {
            log.error("Failed to log system event: {}", eventType, e);

            // Fallback to structured logging if audit fails
            StructuredLoggingUtil.logErrorEvent(
                    "AUDIT_FAILURE",
                    "Failed to log system event",
                    e.getMessage(),
                    java.util.Map.of("eventType", eventType));
        }
    }

    /**
     * Log authentication event asynchronously
     * 
     * BENEFITS:
     * - Non-blocking authentication tracking
     * - Security monitoring
     * - Login pattern analysis
     * - Fraud detection support
     */
    @Async("asyncExecutor")
    @Override
    public void logAuthenticationEventAsync(String userId, String eventType, String ipAddress, String userAgent,
            String details) {
        try {
            log.debug("Logging authentication event for user: {}, type: {}", userId, eventType);

            // Create audit event entity with thread-safe ID generation
            AuditEvent auditEvent = AuditEvent.builder()
                    .id(generateUniqueId())
                    .userId(userId)
                    .eventType(eventType)
                    .eventCategory("AUTHENTICATION")
                    .eventDetails(details)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .severityLevel("MEDIUM")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Save to database with direct persistence
            saveAuditEvent(auditEvent);

            // Also log to structured logging for monitoring
            StructuredLoggingUtil.logBusinessEvent(
                    "AUDIT_EVENT",
                    userId,
                    "AUTHENTICATION_AUDIT",
                    "SUCCESS",
                    java.util.Map.of(
                            "eventType", eventType,
                            "ipAddress", ipAddress,
                            "userAgent", userAgent,
                            "details", details,
                            "auditId", auditEvent.getId(),
                            "thread", Thread.currentThread().getName()));

            log.debug("Authentication event logged successfully for user: {}", userId);

        } catch (Exception e) {
            log.error("Failed to log authentication event for user: {}", userId, e);

            // Fallback to structured logging if audit fails
            StructuredLoggingUtil.logErrorEvent(
                    "AUDIT_FAILURE",
                    "Failed to log authentication event",
                    e.getMessage(),
                    java.util.Map.of("userId", userId, "eventType", eventType));
        }
    }
}
