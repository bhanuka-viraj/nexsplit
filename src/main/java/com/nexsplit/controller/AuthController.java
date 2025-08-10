package com.nexsplit.controller;

import com.nexsplit.dto.user.AuthResponse;
import com.nexsplit.dto.user.LoginRequest;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.model.User;
import com.nexsplit.service.UserService;
import com.nexsplit.config.ApiConfig;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/auth")
public class AuthController {

        private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

        private final UserService userService;

        public AuthController(UserService userService) {
                this.userService = userService;
        }

        @GetMapping("/oauth-login")
        public ResponseEntity<AuthResponse> oauthLogin(@AuthenticationPrincipal OidcUser oidcUser) {
                User user = userService.processOAuthUser(oidcUser);
                String accessToken = userService.generateAccessToken(user);
                String refreshToken = userService.generateRefreshToken(user.getEmail());

                logger.info("OAuth2 login successful for user: {}", user.getEmail());

                AuthResponse response = AuthResponse.builder()
                                .accessToken(accessToken)
                                .tokenType("Bearer")
                                .refreshToken(refreshToken)
                                .expiresIn(3600L) // 1 hour
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/register")
        public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserDto userDto,
                        HttpServletResponse response) {
                String email = userDto.getEmail();
                String password = userDto.getPassword();
                String firstName = userDto.getFirstName();
                String lastName = userDto.getLastName();
                String username = userDto.getUsername();
                String contactNumber = userDto.getContactNumber();

                User user = userService.registerUser(email, password, firstName, lastName, username, contactNumber);

                String accessToken = userService.generateAccessToken(user);
                String refreshToken = userService.generateRefreshToken(user.getEmail());

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
                                .expiresIn(3600L) // 1 hour
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .build();

                return ResponseEntity.ok(authResponse);
        }

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                        HttpServletResponse response) {
                String email = loginRequest.getEmail();
                String password = loginRequest.getPassword();

                String accessToken = userService.loginUser(email, password);
                String refreshToken = userService.generateRefreshToken(email);

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
                                .expiresIn(3600L) // 1 hour
                                .email(email)
                                .build();

                return ResponseEntity.ok(authResponse);
        }

        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refreshToken(@CookieValue("refreshToken") String refreshToken) {
                String accessToken = userService.generateAccessToken(refreshToken);

                AuthResponse authResponse = AuthResponse.builder()
                                .accessToken(accessToken)
                                .tokenType("Bearer")
                                .expiresIn(3600L) // 1 hour
                                .build();

                return ResponseEntity.ok(authResponse);
        }

        @PostMapping("/logout")
        public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
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
}