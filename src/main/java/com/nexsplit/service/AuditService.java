package com.nexsplit.service;

/**
 * Audit service interface for logging business events and security incidents
 * All operations are asynchronous to avoid blocking the main thread
 */
public interface AuditService {

    /**
     * Log security event asynchronously
     * 
     * @param userId    User ID associated with the event
     * @param eventType Type of security event (e.g., LOGIN_FAILURE, TOKEN_THEFT)
     * @param details   Additional details about the event
     */
    void logSecurityEventAsync(String userId, String eventType, String details);

    /**
     * Log user action asynchronously
     * 
     * @param userId  User ID who performed the action
     * @param action  Type of action performed (e.g., USER_REGISTRATION,
     *                PROFILE_UPDATE)
     * @param details Additional details about the action
     */
    void logUserActionAsync(String userId, String action, String details);

    /**
     * Log system event asynchronously
     * 
     * @param eventType Type of system event (e.g., CLEANUP_COMPLETED, MAINTENANCE)
     * @param details   Additional details about the system event
     */
    void logSystemEventAsync(String eventType, String details);

    /**
     * Log authentication event asynchronously
     * 
     * @param userId    User ID associated with the authentication
     * @param eventType Type of authentication event (e.g., LOGIN_SUCCESS, LOGOUT)
     * @param ipAddress IP address of the user
     * @param userAgent User agent string
     * @param details   Additional details about the authentication
     */
    void logAuthenticationEventAsync(String userId, String eventType, String ipAddress, String userAgent,
            String details);
}
