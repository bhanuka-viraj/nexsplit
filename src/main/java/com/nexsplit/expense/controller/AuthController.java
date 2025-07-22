package com.nexsplit.expense.controller;

import com.nexsplit.expense.model.User;
import com.nexsplit.expense.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<Map<String, String>> oauth2Success(@AuthenticationPrincipal OidcUser oidcUser) {
        User user = userService.processOAuthUser(oidcUser);
        String token = userService.generateJwtForOAuthUser(oidcUser);

        logger.info("OAuth2 login successful for user: {}", user.getEmail());

        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer"
        ));
    }
}
