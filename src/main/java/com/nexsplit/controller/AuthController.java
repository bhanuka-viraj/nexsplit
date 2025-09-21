package com.nexsplit.controller;

import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.auth.AuthResponse;
import com.nexsplit.dto.auth.LoginRequest;
import com.nexsplit.dto.auth.OAuth2TokenRequest;
import com.nexsplit.dto.auth.PasswordResetDto;
import com.nexsplit.dto.auth.PasswordResetRequestDto;
import com.nexsplit.dto.auth.RefreshTokenRequest;
import com.nexsplit.dto.auth.RefreshTokenResponse;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.dto.response.RegistrationResponse;
import com.nexsplit.dto.response.PasswordResetResponse;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.exception.SecurityException;
import com.nexsplit.exception.UserNotFoundException;
import com.nexsplit.model.User;
import com.nexsplit.service.AuditService;
import com.nexsplit.service.EmailService;
import com.nexsplit.service.OAuth2Service;
import com.nexsplit.service.UserService;
import com.nexsplit.service.impl.RefreshTokenServiceImpl;
import com.nexsplit.service.impl.UserServiceImpl;
import com.nexsplit.config.ApiConfig;
import com.nexsplit.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nexsplit.util.LoggingUtil;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
@Slf4j
public class AuthController {

        private final UserService userService;
        private final RefreshTokenServiceImpl refreshTokenServiceImpl;
        private final JwtUtil jwtUtil;
        private final AuditService auditService;
        private final OAuth2Service oauth2Service;
        private final EmailService emailService;

        public AuthController(UserServiceImpl userService, RefreshTokenServiceImpl refreshTokenServiceImpl,
                        JwtUtil jwtUtil, AuditService auditService, OAuth2Service oauth2Service,
                        EmailService emailService) {
                this.userService = userService;
                this.refreshTokenServiceImpl = refreshTokenServiceImpl;
                this.jwtUtil = jwtUtil;
                this.auditService = auditService;
                this.oauth2Service = oauth2Service;
                this.emailService = emailService;
        }

        @PostMapping("/oauth2/verify")
        @Operation(summary = "OAuth2 Token Exchange", description = "Verify Google OAuth2 token and return JWT tokens. Used by mobile apps and web apps that handle OAuth2 on the frontend.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "OAuth2 token exchange request", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = OAuth2TokenRequest.class), examples = @ExampleObject(name = "OAuth2 Token Exchange", value = """
                        {
                          "googleToken": "ya29.a0AfB_byC..."
                        }
                        """))), responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OAuth2 token verification successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid Google OAuth2 token")
        })
        public ResponseEntity<AuthResponse> verifyOAuth2Token(@Valid @RequestBody OAuth2TokenRequest request,
                        HttpServletRequest httpRequest, HttpServletResponse response) {
                try {
                        // Get client IP address for security tracking
                        String ipAddress = getClientIpAddress(httpRequest);
                        String userAgent = httpRequest.getHeader("User-Agent");

                        // Verify OAuth2 token and get JWT tokens
                        AuthResponse authResponse = oauth2Service.verifyOAuth2Token(request, ipAddress, userAgent);

                        // Set refresh token as a secure cookie
                        ResponseCookie refreshCookie = ResponseCookie
                                        .from("refreshToken", authResponse.getRefreshToken())
                                        .httpOnly(true)
                                        .secure(true)
                                        .path(ApiConfig.API_BASE_PATH)
                                        .maxAge(Duration.ofDays(7))
                                        .sameSite("Strict")
                                        .build();

                        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                        log.info("OAuth2 token verification successful");
                        return ResponseEntity.ok(authResponse);

                } catch (Exception e) {
                        log.error("OAuth2 token verification failed: {}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(AuthResponse.builder()
                                                        .error("OAuth2 token verification failed")
                                                        .build());
                }
        }

        @PostMapping("/register")
        @Operation(summary = "Register New User", description = "Register a new user account with email and password authentication. Email verification is required before login.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration data", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class), examples = @ExampleObject(name = "User Registration", value = """
                        {
                          "email": "john.doe@example.com",
                          "password": "StrongPass123!",
                          "firstName": "John",
                          "lastName": "Doe",
                          "username": "johndoe",
                          "contactNumber": "+1234567890"
                        }
                        """))), responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully. Check email for verification code."),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data or user already exists")
        })
        public ResponseEntity<ApiResponse<RegistrationResponse>> register(@Valid @RequestBody UserDto userDto,
                        HttpServletRequest request) {
                User user = userService.registerUser(userDto);

                // Get client information for security tracking
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                // Log audit event asynchronously
                auditService.logAuthenticationEventAsync(user.getId(), "USER_REGISTRATION_SUCCESS", ipAddress,
                                userAgent, "User registration successful - email verification pending");

                // Log business event for Elasticsearch
                StructuredLoggingUtil.logBusinessEvent(
                                "USER_REGISTRATION",
                                user.getEmail(),
                                "CREATE_ACCOUNT",
                                "SUCCESS",
                                Map.of(
                                                "source", "WEB",
                                                "ipAddress", ipAddress,
                                                "userAgent", userAgent,
                                                "userType", "REGULAR",
                                                "emailVerified", false));

                log.info("User registered successfully: {} - Email verification required",
                                LoggingUtil.maskEmail(user.getEmail()));

                RegistrationResponse response = RegistrationResponse.builder()
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .emailVerified(false)
                                .nextStep("Verify your email to complete registration")
                                .build();

                return ResponseEntity.ok(ApiResponse.success(response,
                                "Registration successful! Please check your email for verification code."));
        }

        @PostMapping("/login")
        @Operation(summary = "User Login", description = "Authenticate user with email and password. Returns JWT access token and refresh token.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login credentials", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class), examples = @ExampleObject(name = "User Login", value = """
                        {
                          "email": "john.doe@example.com",
                          "password": "StrongPass123!"
                        }
                        """))), responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                        HttpServletRequest request, HttpServletResponse response) {
                String email = loginRequest.getEmail();
                String password = loginRequest.getPassword();

                String accessToken = userService.loginUser(email, password);

                // Get user ID for refresh token generation
                UserDto user = userService.getUserByEmail(email);

                // Get client information for security tracking
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                // Generate secure refresh token with family tracking
                String refreshToken = refreshTokenServiceImpl.generateRefreshToken(user.getId(), userAgent);

                // Log business event for Elasticsearch
                StructuredLoggingUtil.logBusinessEvent(
                                "USER_LOGIN",
                                email,
                                "AUTHENTICATE",
                                "SUCCESS",
                                Map.of(
                                                "source", "WEB",
                                                "ipAddress", ipAddress,
                                                "userAgent", userAgent,
                                                "authMethod", "EMAIL_PASSWORD"));

                log.info("Email/password login successful for user: {}", LoggingUtil.maskEmail(email));

                // Log authentication event asynchronously
                auditService.logAuthenticationEventAsync(
                                user.getId(),
                                "LOGIN_SUCCESS",
                                getClientIpAddress(request),
                                request.getHeader("User-Agent"),
                                "Email/password login successful");

                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path(ApiConfig.API_BASE_PATH)
                                .maxAge(Duration.ofDays(7))
                                .sameSite("Strict")
                                .build();

                response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                AuthResponse authResponse = AuthResponse.builder()
                                .accessToken(accessToken)
                                .tokenType("Bearer")
                                .refreshToken(refreshToken)
                                .expiresIn(900L) // 15 minutes
                                .email(email)
                                .build();

                return ResponseEntity.ok(authResponse);
        }

        @PostMapping("/refresh")
        @Operation(summary = "Refresh Access Token (Cookie)", description = "Refresh an expired access token using a valid refresh token from cookies. This endpoint does not require authentication.", responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Refresh token not found in cookies")
        })
        public ResponseEntity<AuthResponse> refreshToken(
                        @CookieValue(value = "refreshToken", required = false) String refreshToken,
                        HttpServletRequest request,
                        HttpServletResponse response) {
                if (refreshToken == null || refreshToken.isEmpty()) {
                        log.warn("Refresh token not found in cookies");
                        return ResponseEntity.badRequest().build();
                }

                try {
                        // Get client information for security tracking
                        String ipAddress = getClientIpAddress(request);
                        String userAgent = request.getHeader("User-Agent");

                        // Use the secure refresh token service with theft detection
                        RefreshTokenResponse tokenResponse = refreshTokenServiceImpl
                                        .refreshAccessToken(refreshToken, ipAddress, userAgent);

                        log.info("Token refreshed successfully");

                        // Clear the old refresh token cookie
                        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                                        .httpOnly(true)
                                        .secure(true)
                                        .path(ApiConfig.API_BASE_PATH)
                                        .maxAge(0)
                                        .sameSite("Strict")
                                        .build();
                        response.setHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

                        // Set the new refresh token as a cookie
                        ResponseCookie newCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                                        .httpOnly(true)
                                        .secure(true)
                                        .path(ApiConfig.API_BASE_PATH)
                                        .maxAge(7 * 24 * 60 * 60) // 7 days
                                        .sameSite("Strict")
                                        .build();
                        response.setHeader(HttpHeaders.SET_COOKIE, newCookie.toString());

                        AuthResponse authResponse = AuthResponse.builder()
                                        .accessToken(tokenResponse.getAccessToken())
                                        .tokenType("Bearer")
                                        .expiresIn(900L) // 15 minutes
                                        .build();

                        return ResponseEntity.ok(authResponse);
                } catch (SecurityException e) {
                        log.error("Security violation during token refresh: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(AuthResponse.builder()
                                                        .error("Security violation detected - please re-authenticate"
                                                                        + " " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        log.warn("Failed to refresh token: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
        }

        @PostMapping("/refresh-token")
        @Operation(summary = "Refresh Access Token (Body)", description = "Refresh an expired access token using a valid refresh token in the request body. This endpoint does not require authentication.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refresh token request", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshTokenRequest.class), examples = @ExampleObject(name = "Refresh Token Request", value = """
                        {
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        }
                        """))), responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body")
        })
        public ResponseEntity<AuthResponse> refreshTokenBody(@Valid @RequestBody RefreshTokenRequest request,
                        HttpServletRequest httpRequest) {
                return processRefreshToken(request.getRefreshToken(), httpRequest);
        }

        /**
         * Process refresh token request with comprehensive security tracking
         * 
         * SECURITY INTEGRATION: This method integrates client information
         * extraction with the secure refresh token service to enable
         * comprehensive theft detection and security monitoring.
         * 
         * SECURITY FEATURES:
         * 1. CLIENT FINGERPRINTING: Extracts IP address and user agent for
         * security tracking and theft detection
         * 2. THEFT DETECTION: Uses RefreshTokenService to detect:
         * - Token reuse (immediate theft detection)
         * - Family compromise (sequential theft detection)
         * - Suspicious activity (IP/UA changes, rapid generation)
         * - Concurrent session limits (abuse prevention)
         * 3. SECURITY RESPONSE: Handles security violations with proper
         * error responses and logging
         * 
         * FLOW:
         * 1. Extract client information (IP + User Agent)
         * 2. Call secure refresh service with theft detection
         * 3. Handle security exceptions with appropriate responses
         * 4. Return new tokens or security violation message
         * 
         * SECURITY EXCEPTIONS:
         * - SecurityException: Token theft or family compromise detected
         * - General Exception: Invalid token or other errors
         * 
         * @param refreshToken The refresh token to process
         * @param request      The HTTP request for client information extraction
         * @return ResponseEntity with new tokens or error response
         */
        private ResponseEntity<AuthResponse> processRefreshToken(String refreshToken, HttpServletRequest request) {
                try {
                        // Get client information for security tracking
                        String ipAddress = getClientIpAddress(request);
                        String userAgent = request.getHeader("User-Agent");

                        // Use the secure refresh token service with theft detection
                        RefreshTokenResponse tokenResponse = refreshTokenServiceImpl
                                        .refreshAccessToken(refreshToken, ipAddress, userAgent);

                        log.info("Token refreshed successfully");

                        // For body-based refresh, include the new refresh token in response
                        AuthResponse authResponse = AuthResponse.builder()
                                        .accessToken(tokenResponse.getAccessToken())
                                        .tokenType("Bearer")
                                        .refreshToken(tokenResponse.getRefreshToken())
                                        .expiresIn(900L) // 15 minutes
                                        .build();

                        return ResponseEntity.ok(authResponse);
                } catch (SecurityException e) {
                        log.error("Security violation during token refresh: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(AuthResponse.builder()
                                                        .error("Security violation detected - please re-authenticate")
                                                        .build());
                } catch (Exception e) {
                        log.warn("Failed to refresh token: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
        }

        @PostMapping("/logout")
        @Operation(summary = "User Logout", description = "Logout user and revoke all refresh tokens. Requires authentication via Bearer token.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Successful Logout", value = """
                                        {
                                          "message": "Logged out successfully"
                                        }
                                        """))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
        })
        /**
         * User logout with comprehensive token revocation
         * 
         * SECURITY FEATURE: This method implements secure logout by revoking
         * all refresh tokens for the user, ensuring complete session termination.
         * 
         * LOGOUT STRATEGY:
         * 1. TOKEN REVOCATION: Extracts user information from the access token
         * in the Authorization header and revokes all refresh tokens for that user
         * - Prevents any remaining refresh tokens from being used
         * - Forces re-authentication for all user sessions
         * 2. COOKIE CLEANUP: Clears the refresh token cookie from the client
         * - Removes stored refresh token from browser
         * - Prevents client-side token reuse
         * 
         * ACCESS TOKEN EXTRACTION:
         * - Extracts Bearer token from Authorization header
         * - Decodes JWT to get user email
         * - Finds user and revokes all their refresh tokens
         * 
         * SECURITY BENEFITS:
         * - Complete session termination across all devices
         * - Prevents token reuse after logout
         * - Enables "logout everywhere" functionality
         * - Supports security incident response
         * 
         * ERROR HANDLING:
         * - Graceful handling if token extraction fails
         * - Continues with cookie cleanup even if revocation fails
         * - Logs issues for debugging without exposing details
         * 
         * @param request  The HTTP request containing the access token
         * @param response The HTTP response for cookie cleanup
         * @return ResponseEntity with logout confirmation
         */
        public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
                String userEmail = "unknown";

                // Get user ID from access token in Authorization header
                try {
                        String accessToken = request.getHeader("Authorization");
                        if (accessToken != null && accessToken.startsWith("Bearer ")) {
                                accessToken = accessToken.substring(7);
                                // Extract user email from access token
                                userEmail = jwtUtil.getEmailFromToken(accessToken);
                                UserDto user = userService.getUserByEmail(userEmail);
                                // Revoke all refresh tokens for the user
                                refreshTokenServiceImpl.revokeAllUserTokens(user.getId());
                                // Log business event for Elasticsearch
                                StructuredLoggingUtil.logBusinessEvent(
                                                "USER_LOGOUT",
                                                userEmail,
                                                "LOGOUT",
                                                "SUCCESS",
                                                Map.of(
                                                                "source", "WEB",
                                                                "ipAddress", getClientIpAddress(request),
                                                                "userAgent", request.getHeader("User-Agent"),
                                                                "logoutType", "FULL_LOGOUT"));

                                log.info("All tokens revoked for user: {}", LoggingUtil.maskEmail(userEmail));

                                // Log authentication event asynchronously
                                auditService.logAuthenticationEventAsync(
                                                user.getId(),
                                                "LOGOUT",
                                                getClientIpAddress(request),
                                                request.getHeader("User-Agent"),
                                                "User logged out - all tokens revoked");
                        }
                } catch (Exception e) {
                        log.warn("Could not revoke tokens during logout: {}", e.getMessage());
                }

                // Clear the refresh token cookie
                ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .secure(true)
                                .path(ApiConfig.API_BASE_PATH)
                                .maxAge(0)
                                .sameSite("Strict")
                                .build();

                response.setHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

                log.info("User logged out successfully: {}", LoggingUtil.maskEmail(userEmail));
                return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
        }

        @PostMapping("/verify-email")
        @Operation(summary = "Verify Email", description = "Verify email address using verification code. Designed for mobile app integration.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email verification data", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Verify Email", value = """
                        {
                          "email": "user@example.com",
                          "code": "123456"
                        }
                        """))), responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired verification code"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmailMobile(
                        @RequestBody Map<String, String> request,
                        HttpServletRequest httpRequest, HttpServletResponse response) {
                try {
                        String email = request.get("email");
                        String code = request.get("code");

                        if (email == null || email.trim().isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.error("Email is required"));
                        }

                        if (code == null || code.trim().isEmpty()) {
                                return ResponseEntity.badRequest().body(ApiResponse
                                                .error("Verification code is required"));
                        }

                        log.info("Mobile email verification request received for: {} with code: {}",
                                        LoggingUtil.maskEmail(email), LoggingUtil.maskSensitiveData(code));

                        // Find user by email first
                        User user = userService.getUserByEmailForVerification(email);

                        // Confirm email using the code
                        User confirmedUser = userService.confirmEmail(code, user);

                        // Verify that the confirmed user matches the requested email
                        if (!confirmedUser.getEmail().equals(email)) {
                                log.warn("Email verification failed - code mismatch for: {}",
                                                LoggingUtil.maskEmail(email));
                                return ResponseEntity.badRequest().body(ApiResponse.error(
                                                "Invalid verification code for this email"));
                        }

                        // Log successful email confirmation
                        StructuredLoggingUtil.logSecurityEvent(
                                        "EMAIL_VERIFIED_MOBILE",
                                        confirmedUser.getEmail(),
                                        getClientIpAddress(httpRequest),
                                        httpRequest.getHeader("User-Agent"),
                                        "MEDIUM",
                                        Map.of(
                                                        "userId", confirmedUser.getId(),
                                                        "username", confirmedUser.getUsername(),
                                                        "thread", Thread.currentThread().getName()));

//                        // Log audit event asynchronously
//                        auditService.logSecurityEventAsync(
//                                        confirmedUser.getId(),
//                                        "EMAIL_VERIFIED_MOBILE",
//                                        "Email verified via mobile app");

                        log.info("Email verified successfully via mobile app for user: {}",
                                        LoggingUtil.maskEmail(confirmedUser.getEmail()));

                        // Generate tokens after successful email verification
                        String accessToken = userService.generateAccessToken(confirmedUser);
                        String refreshToken = refreshTokenServiceImpl.generateRefreshToken(
                                        confirmedUser.getId(),
                                        httpRequest.getHeader("User-Agent"));

                        // Set refresh token as a secure cookie
                        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                                        .httpOnly(true)
                                        .secure(true)
                                        .path(ApiConfig.API_BASE_PATH)
                                        .maxAge(Duration.ofDays(7))
                                        .sameSite("Strict")
                                        .build();

                        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                        return ResponseEntity.ok(ApiResponse.success(
                                        Map.of(
                                                        "email", confirmedUser.getEmail(),
                                                        "username", confirmedUser.getUsername(),
                                                        "verified", true,
                                                        "accessToken", accessToken,
                                                        "refreshToken", refreshToken,
                                                        "tokenType", "Bearer",
                                                        "expiresIn", 900L),
                                        "Email verified successfully! You can now login."));

                } catch (UserNotFoundException e) {
                        log.warn("Mobile email verification failed - user not found: {}", e.getMessage());
                        return ResponseEntity.notFound().build();
                } catch (IllegalArgumentException e) {
                        log.warn("Mobile email verification failed - validation error: {}", e.getMessage());
                        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));

                } catch (BusinessException e) {
                        log.warn("Mobile email verification failed - invalid code: {}", e.getMessage());
                        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
                } catch (Exception e) {
                        log.error("Mobile email verification failed - unexpected error: {}", e.getMessage(), e);
                        return ResponseEntity.internalServerError()
                                        .body(ApiResponse.error("An unexpected error occurred"));
                }
        }

        @PostMapping("/resend-email-verification")
        @Operation(summary = "Resend Email Verification", description = "Resend email verification to a user who hasn't verified their email yet.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email for verification resend", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Resend Email Verification", value = """
                        {
                          "email": "john.doe@example.com"
                        }
                        """))), responses = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verification resent successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email or email already verified"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<Map<String, Object>> resendEmailVerification(@RequestBody Map<String, String> request,
                        HttpServletRequest httpRequest) {
                try {
                        String email = request.get("email");
                        if (email == null || email.trim().isEmpty()) {
                                return ResponseEntity.badRequest().body(Map.of(
                                                "success", false,
                                                "message", "Email is required",
                                                "error", "MISSING_EMAIL"));
                        }

                        log.info("Email verification resend request received for: {}", LoggingUtil.maskEmail(email));

                        // Resend email verification
                        userService.resendEmailVerification(email);

                        // Log security event
                        StructuredLoggingUtil.logSecurityEvent(
                                        "EMAIL_VERIFICATION_RESENT",
                                        email,
                                        getClientIpAddress(httpRequest),
                                        httpRequest.getHeader("User-Agent"),
                                        "LOW",
                                        Map.of(
                                                        "email", LoggingUtil.maskEmail(email),
                                                        "thread", Thread.currentThread().getName()));

                        log.info("Email verification resent successfully for: {}", LoggingUtil.maskEmail(email));

                        return ResponseEntity.ok(Map.of(
                                        "success", true,
                                        "message", "Email verification sent successfully",
                                        "data", Map.of(
                                                        "email", LoggingUtil.maskEmail(email))));

                } catch (UserNotFoundException e) {
                        log.warn("Email verification resend failed - user not found: {}", e.getMessage());
                        return ResponseEntity.notFound().build();
                } catch (IllegalArgumentException e) {
                        log.warn("Email verification resend failed - validation error: {}", e.getMessage());
                        return ResponseEntity.badRequest().body(Map.of(
                                        "success", false,
                                        "message", e.getMessage(),
                                        "error", "VALIDATION_ERROR"));
                } catch (Exception e) {
                        log.error("Email verification resend failed - unexpected error: {}", e.getMessage(), e);
                        return ResponseEntity.internalServerError().body(Map.of(
                                        "success", false,
                                        "message", "An unexpected error occurred",
                                        "error", "INTERNAL_ERROR"));
                }
        }

        /**
         * Extract client IP address from request
         * Handles proxy/load balancer scenarios
         * 
         * NETWORK ARCHITECTURE CHALLENGE: Modern web applications often have
         * complex network architectures with multiple layers:
         * 
         * Client → Load Balancer → Reverse Proxy → Application Server
         * 
         * The direct client IP is often hidden behind these layers, making
         * accurate client identification challenging for security purposes.
         * 
         * EXTRACTION STRATEGY (Priority Order):
         * 1. X-Forwarded-For Header: Most common standard for proxy chains
         * - Format: "client_ip, proxy1_ip, proxy2_ip, ..."
         * - We take the first IP (original client)
         * - Used by: AWS ALB, Cloudflare, most load balancers
         * 
         * 2. X-Real-IP Header: Nginx and some other proxies
         * - Contains the original client IP
         * - Simpler than X-Forwarded-For
         * 
         * 3. RemoteAddr: Direct connection IP (fallback)
         * - Used when no proxy headers are present
         * - Represents the immediate connection source
         * 
         * SECURITY BENEFITS:
         * - Accurate client identification for theft detection
         * - Works with any proxy/load balancer setup
         * - Enables IP-based security monitoring
         * - Supports geographic and network-based security policies
         * 
         * EXAMPLE SCENARIOS:
         * 
         * Direct Connection:
         * - Headers: None
         * - Result: "192.168.1.100"
         * 
         * Behind Load Balancer:
         * - X-Forwarded-For: "203.0.113.1, 10.0.0.1"
         * - Result: "203.0.113.1" (original client)
         * 
         * Behind Cloudflare:
         * - X-Forwarded-For: "192.0.2.1, 104.16.0.1"
         * - Result: "192.0.2.1" (original client)
         * 
         * @param request The HTTP request object
         * @return The extracted client IP address
         */
        private String getClientIpAddress(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                        return xForwardedFor.split(",")[0].trim();
                }

                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                        return xRealIp;
                }

                return request.getRemoteAddr();
        }

        @PostMapping("/request-password-reset")
        @Operation(summary = "Request Password Reset", description = "Request a password reset link to be sent to the user's email")
        public ResponseEntity<ApiResponse<PasswordResetResponse>> requestPasswordReset(
                        @Valid @RequestBody PasswordResetRequestDto requestDto) {
                try {
                        userService.requestPasswordReset(requestDto.getEmail());
                        PasswordResetResponse response = PasswordResetResponse.builder()
                                        .email(requestDto.getEmail())
                                        .message("Password reset link has been sent to " + requestDto.getEmail())
                                        .emailSent(true)
                                        .build();
                        return ResponseEntity.ok(ApiResponse.success(response, "Password reset link has been sent"));
                } catch (Exception e) {
                        PasswordResetResponse response = PasswordResetResponse.builder()
                                        .email(requestDto.getEmail())
                                        .message("If the email exists, a reset link has been sent")
                                        .emailSent(true)
                                        .build();
                        return ResponseEntity.ok(ApiResponse.success(response, "Password reset request processed"));
                }
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset Password", description = "Reset user password using the reset token")
        public ResponseEntity<ApiResponse<Void>> resetPassword(
                        @Valid @RequestBody PasswordResetDto resetDto) {

                if (!resetDto.getNewPassword().equals(resetDto.getConfirmPassword())) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("New password and confirm password do not match"));
                }

                try {
                        userService.resetPassword(resetDto.getResetToken(), resetDto.getNewPassword());
                        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
                } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("Invalid reset token or password"));
                }
        }

}
