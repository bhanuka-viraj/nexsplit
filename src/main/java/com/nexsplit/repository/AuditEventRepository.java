package com.nexsplit.repository;

import com.nexsplit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit event operations
 * Provides methods for storing and querying audit events
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {

    /**
     * Find audit events by user ID
     */
    List<AuditEvent> findByUserIdOrderByTimestampDesc(String userId);

    /**
     * Find audit events by event type
     */
    List<AuditEvent> findByEventTypeOrderByTimestampDesc(String eventType);

    /**
     * Find audit events by event category
     */
    List<AuditEvent> findByEventCategoryOrderByTimestampDesc(String eventCategory);

    /**
     * Find audit events by severity level
     */
    List<AuditEvent> findBySeverityLevelOrderByTimestampDesc(String severityLevel);

    /**
     * Find audit events by user ID and event type
     */
    List<AuditEvent> findByUserIdAndEventTypeOrderByTimestampDesc(String userId, String eventType);

    /**
     * Find audit events by user ID and event category
     */
    List<AuditEvent> findByUserIdAndEventCategoryOrderByTimestampDesc(String userId, String eventCategory);

    /**
     * Find audit events within a time range
     */
    List<AuditEvent> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find audit events by user ID within a time range
     */
    List<AuditEvent> findByUserIdAndTimestampBetweenOrderByTimestampDesc(String userId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * Find audit events by IP address
     */
    List<AuditEvent> findByIpAddressOrderByTimestampDesc(String ipAddress);

    /**
     * Count audit events by user ID
     */
    long countByUserId(String userId);

    /**
     * Count audit events by event type
     */
    long countByEventType(String eventType);

    /**
     * Count audit events by severity level
     */
    long countBySeverityLevel(String severityLevel);

    /**
     * Find recent audit events (last N days)
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.timestamp >= :startDate ORDER BY ae.timestamp DESC")
    List<AuditEvent> findRecentEvents(@Param("startDate") LocalDateTime startDate);

    /**
     * Find security events by user ID
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.userId = :userId AND ae.eventCategory = 'SECURITY' ORDER BY ae.timestamp DESC")
    List<AuditEvent> findSecurityEventsByUserId(@Param("userId") String userId);

    /**
     * Find authentication events by user ID
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.userId = :userId AND ae.eventCategory = 'AUTHENTICATION' ORDER BY ae.timestamp DESC")
    List<AuditEvent> findAuthenticationEventsByUserId(@Param("userId") String userId);

    /**
     * Find user action events by user ID
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.userId = :userId AND ae.eventCategory = 'USER_ACTION' ORDER BY ae.timestamp DESC")
    List<AuditEvent> findUserActionEventsByUserId(@Param("userId") String userId);

    /**
     * Find system events
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.eventCategory = 'SYSTEM' ORDER BY ae.timestamp DESC")
    List<AuditEvent> findSystemEvents();

    /**
     * Find high severity events
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.severityLevel IN ('HIGH', 'CRITICAL') ORDER BY ae.timestamp DESC")
    List<AuditEvent> findHighSeverityEvents();

    /**
     * Find events by IP address within time range
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.ipAddress = :ipAddress AND ae.timestamp BETWEEN :startTime AND :endTime ORDER BY ae.timestamp DESC")
    List<AuditEvent> findByIpAddressAndTimestampBetween(@Param("ipAddress") String ipAddress,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
