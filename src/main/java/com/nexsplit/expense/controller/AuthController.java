package com.nexsplit.expense.controller;

import com.nexsplit.expense.dto.user.UserDto;
import com.nexsplit.expense.model.User;
import com.nexsplit.expense.service.UserService;
import com.nexsplit.expense.config.ApiConfig;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("/oath-login")
    public ResponseEntity<Map<String, String>> login(@AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.processOAuthUser(oidcUser);
        String accessToken = userService.generateAccessToken(user);

        logger.info("OAuth2 login successful for user: {}", user.getEmail());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer"
        ));
    }

    @PostMapping("/register")                        // the request should be a new dto
    public ResponseEntity<Map<String, String>> register(@RequestBody UserDto userDto, HttpServletResponse response) {
        String email = userDto.getEmail();
        String password = userDto.getPassword();
        String name = userDto.getName();
        int contactNumber = userDto.getContactNumber();

        User user = userService.registerUser(email, password, name, contactNumber);

        String accessToken = userService.generateAccessToken(user);
        String refreshToken = userService.generateRefreshToken(user.getEmail());

        logger.info("User registered successfully: {}", email);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(ApiConfig.API_BASE_PATH)
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginData, HttpServletResponse response) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        String accessToken = userService.loginUser(email, password);
        String refreshToken = userService.generateRefreshToken(email);

        logger.info("Email/password login successful for user: {}", email);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(ApiConfig.API_BASE_PATH)
                .maxAge(Duration .ofDays(7))
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer"
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        String accessToken = userService.generateAccessToken(refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer"
        ));
    }
}