package com.nexsplit.service;

import com.nexsplit.dto.auth.OAuth2TokenRequest;
import com.nexsplit.dto.auth.AuthResponse;
import com.nexsplit.dto.auth.GoogleUserInfo;

/**
 * Service for OAuth2 operations
 * Handles Google token validation and user processing
 */
public interface OAuth2Service {

    /**
     * Verify Google OAuth2 token and return JWT tokens
     * Used by mobile apps and web apps that handle OAuth2 on the frontend
     * 
     * @param request   OAuth2 token exchange request
     * @param ipAddress Client IP address for security tracking
     * @param userAgent Client user agent for security tracking
     * @return AuthResponse with JWT tokens
     */
    AuthResponse verifyOAuth2Token(OAuth2TokenRequest request, String ipAddress, String userAgent);

    /**
     * Validate Google access token with Google's servers
     * 
     * @param googleToken Google access token to validate
     * @return Google user information if token is valid
     * @throws RuntimeException if token is invalid
     */
    GoogleUserInfo validateGoogleToken(String googleToken);
}
