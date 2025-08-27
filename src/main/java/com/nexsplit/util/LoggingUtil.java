package com.nexsplit.util;

/**
 * Utility class for logging-related helper methods
 * Provides consistent logging patterns and security utilities
 */
public class LoggingUtil {

    /**
     * Mask email address or user ID for security in logs
     * Example: user@example.com -> u***@example.com
     * Example: 123e4567-e89b-12d3-a456-426614174000 -> 123e***-426614174000
     * 
     * @param identifier The email address or user ID to mask
     * @return Masked identifier safe for logging
     */
    public static String maskEmail(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return "***";
        }

        // Check if it's an email address (contains @)
        int atIndex = identifier.indexOf('@');
        if (atIndex > 0) {
            // It's an email address
            if (atIndex <= 1) {
                return "***" + identifier.substring(atIndex);
            }
            return identifier.charAt(0) + "***" + identifier.substring(atIndex);
        } else {
            // It's a user ID (UUID or other identifier)
            if (identifier.length() <= 8) {
                return "***";
            }
            // Show first 4 and last 4 characters
            return identifier.substring(0, 4) + "***" + identifier.substring(identifier.length() - 4);
        }
    }

    /**
     * Mask sensitive data like tokens, passwords, etc.
     * 
     * @param data The data to mask
     * @return Masked data safe for logging
     */
    public static String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return "***";
        }
        if (data.length() <= 8) {
            return "***";
        }
        return data.substring(0, 4) + "***" + data.substring(data.length() - 4);
    }

    /**
     * Mask user ID for security in logs
     * Example: 123e4567-e89b-12d3-a456-426614174000 -> 123e***-426614174000
     * 
     * @param userId The user ID to mask
     * @return Masked user ID safe for logging
     */
    public static String maskUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return "***";
        }
        if (userId.length() <= 8) {
            return "***";
        }
        // Show first 4 and last 4 characters
        return userId.substring(0, 4) + "***" + userId.substring(userId.length() - 4);
    }

    /**
     * Check if data contains sensitive information
     * 
     * @param data The data to check
     * @return true if data contains sensitive information
     */
    public static boolean isSensitiveData(String data) {
        if (data == null)
            return false;

        String lowerData = data.toLowerCase();
        return lowerData.contains("password") ||
                lowerData.contains("token") ||
                lowerData.contains("secret") ||
                lowerData.contains("auth") ||
                lowerData.contains("bearer") ||
                lowerData.contains("jwt") ||
                lowerData.contains("key");
    }
}
