package com.nexsplit.service;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for sending emails asynchronously
 */
public interface EmailService {

    /**
     * Send a simple text email asynchronously
     * 
     * @param to      recipient email address
     * @param subject email subject
     * @param text    email body text
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendSimpleEmail(String to, String subject, String text);

    /**
     * Send password reset email asynchronously
     * 
     * @param to         recipient email address
     * @param resetToken password reset token
     * @param username   user's username
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendPasswordResetEmail(String to, String resetToken, String username);

    /**
     * Send welcome email to new user asynchronously
     * 
     * @param to       recipient email address
     * @param username user's username
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendWelcomeEmail(String to, String username);

    /**
     * Send email verification email asynchronously
     * 
     * @param to                recipient email address
     * @param verificationToken email verification token
     * @param username          user's username
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendEmailVerification(String to, String verificationToken, String username);

    /**
     * Send test preview email showing all templates asynchronously
     * 
     * @param to       recipient email address
     * @param username user's username
     * @return CompletableFuture that completes when email is sent
     */
    CompletableFuture<Void> sendTestPreviewEmail(String to, String username);
}
