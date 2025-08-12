package com.nexsplit.controller;

import com.nexsplit.dto.auth.AuthResponse;
import com.nexsplit.dto.auth.LoginRequest;
import com.nexsplit.dto.auth.RefreshTokenRequest;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.exception.SecurityException;
import com.nexsplit.model.User;
import com.nexsplit.service.RefreshTokenService;
import com.nexsplit.service.UserService;
import com.nexsplit.config.ApiConfig;
import com.nexsplit.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

        private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

        private final UserService userService;
        private final RefreshTokenService refreshTokenService;
        private final JwtUtil jwtUtil;

        public AuthController(UserService userService, RefreshTokenService refreshTokenService, JwtUtil jwtUtil) {
                this.userService = userService;
                this.refreshTokenService = refreshTokenService;
                this.jwtUtil = jwtUtil;
        }

        @GetMapping("/oauth-login")
        @Operation(summary = "OAuth2 Login Callback", description = "Callback endpoint for Google OAuth2 authentication. This endpoint is called by Spring Security after successful OAuth2 authentication.", responses = {
                        @ApiResponse(responseCode = "200", description = "OAuth2 login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name = "Successful OAuth2 Login", value = """
                                        {
                                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                          "tokenType": "Bearer",
                                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                          "expiresIn": 900,
                                          "email": "user@gmail.com",
                                          "fullName": "John Doe"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "OAuth2 authentication failed")
        })
        public ResponseEntity<AuthResponse> oauthLogin(@AuthenticationPrincipal OidcUser oidcUser,
                        HttpServletRequest request, HttpServletResponse response) {
                User user = userService.processOAuthUser(oidcUser);
                String accessToken = userService.generateAccessToken(user);

                // Get client information for security tracking
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                // Generate secure refresh token with family tracking
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);

                logger.info("OAuth2 login successful for user: {}", user.getEmail());

                // Set refresh token as a cookie
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
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .build();

                return ResponseEntity.ok(authResponse);
        }

        @PostMapping("/register")
        @Operation(summary = "Register New User", description = "Register a new user account with email and password authentication.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration data", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class), examples = @ExampleObject(name = "User Registration", value = """
                        {
                          "email": "john.doe@example.com",
                          "password": "StrongPass123!",
                          "firstName": "John",
                          "lastName": "Doe",
                          "username": "johndoe",
                          "contactNumber": "+1234567890"
                        }
                        """))), responses = {
                        @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data or user already exists")
        })
        public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserDto userDto,
                        HttpServletRequest request, HttpServletResponse response) {
                String email = userDto.getEmail();
                String password = userDto.getPassword();
                String firstName = userDto.getFirstName();
                String lastName = userDto.getLastName();
                String username = userDto.getUsername();
                String contactNumber = userDto.getContactNumber();

                User user = userService.registerUser(email, password, firstName, lastName, username, contactNumber);

                String accessToken = userService.generateAccessToken(user);

                // Get client information for security tracking
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                // Generate secure refresh token with family tracking
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);

                logger.info("User registered successfully: {}", email);

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
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .build();

                return ResponseEntity.ok(authResponse);
        }

        @PostMapping("/login")
        @Operation(summary = "User Login", description = "Authenticate user with email and password. Returns JWT access token and refresh token.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Login credentials", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginRequest.class), examples = @ExampleObject(name = "User Login", value = """
                        {
                          "email": "john.doe@example.com",
                          "password": "StrongPass123!"
                        }
                        """))), responses = {
                        @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                        HttpServletRequest request, HttpServletResponse response) {
                String email = loginRequest.getEmail();
                String password = loginRequest.getPassword();

                String accessToken = userService.loginUser(email, password);

                // Get user ID for refresh token generation
                User user = userService.getUserByEmail(email);

                // Get client information for security tracking
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                // Generate secure refresh token with family tracking
                String refreshToken = refreshTokenService.generateRefreshToken(user.getId(), ipAddress, userAgent);

                logger.info("Email/password login successful for user: {}", email);

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
                        @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
                        @ApiResponse(responseCode = "400", description = "Refresh token not found in cookies")
        })
        public ResponseEntity<AuthResponse> refreshToken(
                        @CookieValue(value = "refreshToken", required = false) String refreshToken,
                        HttpServletRequest request,
                        HttpServletResponse response) {
                if (refreshToken == null || refreshToken.isEmpty()) {
                        logger.warn("Refresh token not found in cookies");
                        return ResponseEntity.badRequest().build();
                }

                try {
                        // Get client information for security tracking
                        String ipAddress = getClientIpAddress(request);
                        String userAgent = request.getHeader("User-Agent");

                        // Use the secure refresh token service with theft detection
                        RefreshTokenService.RefreshTokenResponse tokenResponse = refreshTokenService
                                        .refreshAccessToken(refreshToken, ipAddress, userAgent);

                        logger.info("Token refreshed successfully");

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
                        logger.error("Security violation during token refresh: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(AuthResponse.builder()
                                                        .error("Security violation detected - please re-authenticate"
                                                                        + " " + e.getMessage())
                                                        .build());
                } catch (Exception e) {
                        logger.warn("Failed to refresh token: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
        }

        @PostMapping("/refresh-token")
        @Operation(summary = "Refresh Access Token (Body)", description = "Refresh an expired access token using a valid refresh token in the request body. This endpoint does not require authentication.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refresh token request", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshTokenRequest.class), examples = @ExampleObject(name = "Refresh Token Request", value = """
                        {
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                        }
                        """))), responses = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
                        @ApiResponse(responseCode = "400", description = "Invalid request body")
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
                        RefreshTokenService.RefreshTokenResponse tokenResponse = refreshTokenService
                                        .refreshAccessToken(refreshToken, ipAddress, userAgent);

                        logger.info("Token refreshed successfully");

                        // For body-based refresh, include the new refresh token in response
                        AuthResponse authResponse = AuthResponse.builder()
                                        .accessToken(tokenResponse.getAccessToken())
                                        .tokenType("Bearer")
                                        .refreshToken(tokenResponse.getRefreshToken())
                                        .expiresIn(900L) // 15 minutes
                                        .build();

                        return ResponseEntity.ok(authResponse);
                } catch (SecurityException e) {
                        logger.error("Security violation during token refresh: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(AuthResponse.builder()
                                                        .error("Security violation detected - please re-authenticate")
                                                        .build());
                } catch (Exception e) {
                        logger.warn("Failed to refresh token: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
        }

        @PostMapping("/logout")
        @Operation(summary = "User Logout", description = "Logout user and revoke all refresh tokens. Requires authentication via Bearer token.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Successful Logout", value = """
                                        {
                                          "message": "Logged out successfully"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
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
        public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
                // Get user ID from access token in Authorization header
                try {
                        String accessToken = request.getHeader("Authorization");
                        if (accessToken != null && accessToken.startsWith("Bearer ")) {
                                accessToken = accessToken.substring(7);
                                // Extract user email from access token
                                String email = jwtUtil.getEmailFromToken(accessToken);
                                User user = userService.getUserByEmail(email);
                                // Revoke all refresh tokens for the user
                                refreshTokenService.revokeAllUserTokens(user.getId());
                                logger.info("All tokens revoked for user: {}", email);
                        }
                } catch (Exception e) {
                        logger.warn("Could not revoke tokens during logout: {}", e.getMessage());
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

                logger.info("User logged out successfully");
                return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
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
}