package com.nexsplit.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexsplit.dto.auth.AuthResponse;
import com.nexsplit.dto.auth.GoogleUserInfo;
import com.nexsplit.dto.auth.OAuth2TokenRequest;
import com.nexsplit.model.User;
import com.nexsplit.service.AuditService;
import com.nexsplit.service.OAuth2Service;
import com.nexsplit.util.JwtUtil;
import com.nexsplit.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Implementation of OAuth2Service
 * Handles Google OAuth2 token validation and user processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserServiceImpl userService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Override
    public AuthResponse verifyOAuth2Token(OAuth2TokenRequest request, String ipAddress, String userAgent) {
        log.info("Processing OAuth2 token verification");

        try {
            // 1. Validate Google token
            GoogleUserInfo googleUser = validateGoogleToken(request.getGoogleToken());
            log.info("Google token validated for user: {}", LoggingUtil.maskEmail(googleUser.getEmail()));

            // 2. Process OAuth2 user
            User user = userService.processOAuthUserByEmail(googleUser.getEmail(), googleUser.getName());
            log.info("OAuth2 user processed: {}", LoggingUtil.maskEmail(user.getEmail()));

            // 3. Generate JWT tokens
            String accessToken = userService.generateAccessToken(user);
            String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), userAgent);

            // 4. Log audit event asynchronously
            auditService.logAuthenticationEventAsync(
                    user.getId(),
                    "OAUTH_LOGIN_SUCCESS",
                    ipAddress,
                    userAgent,
                    "OAuth2 login successful");

            // 5. Create response
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .refreshToken(refreshToken)
                    .expiresIn(900L) // 15 minutes
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

            log.info("OAuth2 token verification successful for user: {}", LoggingUtil.maskEmail(user.getEmail()));
            return authResponse;

        } catch (Exception e) {
            log.error("OAuth2 token verification failed: {}", e.getMessage(), e);
            throw new RuntimeException("OAuth2 token verification failed", e);
        }
    }

    @Override
    public GoogleUserInfo validateGoogleToken(String googleToken) {
        try {
            // Call Google's userinfo endpoint to validate token
            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

            String response = restClient.get()
                    .uri(userInfoUrl + "?access_token=" + googleToken)
                    .retrieve()
                    .body(String.class);

            if (response == null) {
                throw new RuntimeException("Failed to get user info from Google");
            }

            JsonNode userInfo = objectMapper.readTree(response);

            // Check if we got an error response
            if (userInfo.has("error")) {
                throw new RuntimeException("Google token validation failed: " + userInfo.get("error").asText());
            }

            // Extract user information
            GoogleUserInfo googleUser = GoogleUserInfo.builder()
                    .email(userInfo.get("email").asText())
                    .name(userInfo.get("name").asText())
                    .givenName(userInfo.has("given_name") ? userInfo.get("given_name").asText() : null)
                    .familyName(userInfo.has("family_name") ? userInfo.get("family_name").asText() : null)
                    .picture(userInfo.has("picture") ? userInfo.get("picture").asText() : null)
                    .sub(userInfo.get("sub").asText())
                    .emailVerified(userInfo.get("email_verified").asBoolean())
                    .locale(userInfo.has("locale") ? userInfo.get("locale").asText() : null)
                    .hd(userInfo.has("hd") ? userInfo.get("hd").asText() : null)
                    .build();

            log.debug("Google token validated successfully for user: {}", LoggingUtil.maskEmail(googleUser.getEmail()));
            return googleUser;

        } catch (Exception e) {
            log.error("Google token validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid Google OAuth2 token", e);
        }
    }
}
