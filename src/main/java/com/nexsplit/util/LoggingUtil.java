package com.nexsplit.util;

/**
 * Utility class for logging-related helper methods
 * Provides consistent logging patterns and security utilities
 */
public class LoggingUtil {

    /**
     * Mask email address for security in logs
     * Example: user@example.com -> u***@example.com
     * 
     * @param email The email address to mask
     * @return Masked email address safe for logging
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
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
