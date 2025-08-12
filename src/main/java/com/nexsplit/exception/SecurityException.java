package com.nexsplit.exception;

/**
 * Custom exception for security-related errors
 * Used for token theft, suspicious activity, etc.
 */
public class SecurityException extends RuntimeException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
