package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.auth.AuthResponse;
import com.nexsplit.dto.auth.ChangePasswordDto;
import com.nexsplit.dto.auth.PasswordResetDto;
import com.nexsplit.dto.auth.PasswordResetRequestDto;
import com.nexsplit.dto.auth.PasswordValidationRequest;
import com.nexsplit.dto.auth.PasswordValidationResponse;
import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.model.ApiResponse;
import com.nexsplit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "User profile and management endpoints")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get User Profile", description = "Retrieve the current user's profile information. Requires authentication.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserProfileDto> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            logger.error("UserDetails is null - authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername();
        logger.info("Getting profile for user: {}", email);
        UserProfileDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update User Profile", description = "Update the current user's profile information. Requires authentication.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid input data")
    })
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        if (userDetails == null) {
            logger.error("UserDetails is null - authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername();
        logger.info("Updating profile for user: {}", email);
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
    @Operation(summary = "Validate Password Strength", description = "Validate if a password meets strength requirements")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password validation result", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PasswordValidationResponse.class))
    })
    public ResponseEntity<PasswordValidationResponse> validatePassword(
            @Valid @RequestBody PasswordValidationRequest request) {
        boolean isValid = userService.validatePasswordStrength(request.getPassword());
        String message = isValid ? "Strong password" : "Password is not strong enough";
        return ResponseEntity.ok(new PasswordValidationResponse(isValid, message));
    }
}
