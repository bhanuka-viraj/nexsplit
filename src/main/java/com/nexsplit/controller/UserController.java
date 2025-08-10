package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.user.*;
import com.nexsplit.model.ApiResponse;
import com.nexsplit.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UserProfileDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        String email = userDetails.getUsername();
        UserProfileDto updatedProfile = userService.updateUserProfile(
                email,
                updateUserDto.getFirstName(),
                updateUserDto.getLastName(),
                updateUserDto.getUsername(),
                updateUserDto.getContactNumber());
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        String email = userDetails.getUsername();

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorResponse("New password and confirm password do not match",
                            HttpStatus.BAD_REQUEST.value()));
        }

        userService.changePassword(email, changePasswordDto.getCurrentPassword(), changePasswordDto.getNewPassword());

        logger.info("Password changed successfully for user: {}", email);
        return ResponseEntity.ok(ApiResponse.successResponse("Password changed successfully"));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto requestDto) {
        try {
            userService.requestPasswordReset(requestDto.getEmail());
            return ResponseEntity.ok(ApiResponse.successResponse("Password reset email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.successResponse("If the email exists, a reset link has been sent"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody PasswordResetDto resetDto) {

        if (!resetDto.getNewPassword().equals(resetDto.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorResponse("New password and confirm password do not match",
                            HttpStatus.BAD_REQUEST.value()));
        }

        try {
            userService.resetPassword(resetDto.getResetToken(), resetDto.getNewPassword());
            return ResponseEntity.ok(ApiResponse.successResponse("Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.errorResponse("Invalid reset token or password",
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.deactivateUser(email);

        logger.info("User deactivated: {}", email);
        return ResponseEntity.ok(ApiResponse.successResponse("User deactivated successfully"));
    }

    @GetMapping("/validate/email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    @GetMapping("/validate/username")
    public ResponseEntity<Map<String, Object>> validateUsername(@RequestParam String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    @PostMapping("/validate/password")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        boolean isValid = userService.validatePasswordStrength(password);
        String message = isValid ? "Strong password" : "Password is not strong enough";
        return ResponseEntity.ok(Map.of("valid", isValid, "message", message));
    }
}
