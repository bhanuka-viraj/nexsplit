package com.nexsplit.service;

import java.util.concurrent.CompletableFuture;

/**
 * Email service interface for sending various types of emails
 * All operations are asynchronous to avoid blocking the main thread
 */
public interface EmailService {

    /**
     * Send password reset email asynchronously
     * 
     * @param email      User's email address
     * @param resetToken The reset token to include in the email
     * @param username   User's username for personalization
     * @return CompletableFuture with the result of the email sending operation
     */
    CompletableFuture<String> sendPasswordResetEmailAsync(String email, int resetToken, String username);

    /**
     * Send welcome email asynchronously for new users
     * 
     * @param email    User's email address
     * @param fullName User's full name for personalization
     * @return CompletableFuture with the result of the email sending operation
     */
    CompletableFuture<String> sendWelcomeEmailAsync(String email, String fullName);

    /**
     * Send email confirmation asynchronously
     * 
     * @param email             User's email address
     * @param confirmationToken The confirmation token
     * @param username          User's username for personalization
     * @return CompletableFuture with the result of the email sending operation
     */
    CompletableFuture<String> sendEmailConfirmationAsync(String email, String confirmationToken, String username);

    /**
     * Send generic email asynchronously
     * 
     * @param to          Recipient email address
     * @param subject     Email subject
     * @param htmlContent HTML content of the email
     * @return CompletableFuture with the result of the email sending operation
     */
    CompletableFuture<String> sendEmailAsync(String to, String subject, String htmlContent);
}
