package com.nexsplit.controller;

import com.nexsplit.config.ApiConfig;
import com.nexsplit.dto.auth.ChangePasswordDto;
import com.nexsplit.dto.auth.PasswordValidationRequest;
import com.nexsplit.dto.auth.PasswordValidationResponse;
import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.dto.user.UserSearchDto;
import com.nexsplit.dto.ApiResponse;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.dto.response.ValidationResponse;
import com.nexsplit.service.AuditService;
import com.nexsplit.service.UserService;
import com.nexsplit.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.nexsplit.util.LoggingUtil;
import com.nexsplit.util.StructuredLoggingUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/users")
@Tag(name = "User Management", description = "User profile and management endpoints")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuditService auditService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, AuditService auditService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.auditService = auditService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get User Profile", description = "Retrieve the current user's profile information. Requires authentication.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.error("UserDetails is null - authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtUtil.getEmailFromCurrentToken();
        if (email == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StructuredLoggingUtil.logBusinessEvent(
                "PROFILE_VIEW",
                email,
                "VIEW_PROFILE",
                "SUCCESS",
                Map.of("source", "WEB"));

        log.info("Getting profile for user: {}", LoggingUtil.maskEmail(email));
        UserProfileDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update User Profile", description = "Update the current user's profile information. Requires authentication.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid input data")
    })
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        if (userDetails == null) {
            log.error("UserDetails is null - authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtUtil.getEmailFromCurrentToken();
        if (email == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StructuredLoggingUtil.logBusinessEvent(
                "PROFILE_UPDATE",
                email,
                "UPDATE_PROFILE",
                "SUCCESS",
                Map.of(
                        "source", "WEB",
                        "updatedFields", updateUserDto.toString()));

        log.info("Updating profile for user: {}", LoggingUtil.maskEmail(email));
        UserProfileDto updatedProfile = userService.updateUserProfile(email, updateUserDto);

        // Log user action asynchronously
        auditService.logUserActionAsync(
                email,
                "PROFILE_UPDATE",
                "User profile updated successfully");

        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "User profile updated successfully"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Change the current user's password", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        String email = jwtUtil.getEmailFromCurrentToken();
        if (email == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("New password and confirm password do not match"));
        }

        userService.changePassword(email, changePasswordDto.getCurrentPassword(),
                changePasswordDto.getNewPassword());

        log.info("Password changed successfully for user: {}", LoggingUtil.maskEmail(email));

        auditService.logUserActionAsync(
                email,
                "PASSWORD_CHANGE",
                "Password changed successfully");

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Deactivate User", description = "Deactivate the current user account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = jwtUtil.getEmailFromCurrentToken();
        if (email == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.deactivateUser(email);

        log.info("User deactivated: {}", LoggingUtil.maskEmail(email));
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }

    @GetMapping("/validate/email")
    @Operation(summary = "Validate Email", description = "Check if an email address is available for registration")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateEmail(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        ValidationResponse response = ValidationResponse.builder()
                .available(isAvailable)
                .field("email")
                .message(isAvailable ? "Email is available" : "Email is already taken")
                .reason(isAvailable ? null : "already_taken")
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, "Email validation completed"));
    }

    @GetMapping("/validate/username")
    @Operation(summary = "Validate Username", description = "Check if a username is available for registration")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateUsername(@RequestParam String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        ValidationResponse response = ValidationResponse.builder()
                .available(isAvailable)
                .field("username")
                .message(isAvailable ? "Username is available" : "Username is already taken")
                .reason(isAvailable ? null : "already_taken")
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, "Username validation completed"));
    }

    @PostMapping("/validate/password")
    @Operation(summary = "Validate Password Strength", description = "Validate if a password meets strength requirements")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password validation result", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PasswordValidationResponse.class))
    })
    public ResponseEntity<ApiResponse<PasswordValidationResponse>> validatePassword(
            @Valid @RequestBody PasswordValidationRequest request) {
        boolean isValid = userService.validatePasswordStrength(request.getPassword());
        String message = isValid ? "Strong password" : "Password is not strong enough";
        PasswordValidationResponse response = new PasswordValidationResponse(isValid, message);
        return ResponseEntity.ok(ApiResponse.success(response, "Password validation completed"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search Users", description = "Search for users by email, username, first name, or last name. Useful for finding users to invite to Nex groups.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid search parameters")
    })
    public ResponseEntity<ApiResponse<PaginatedResponse<UserSearchDto>>> searchUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        if (userDetails == null) {
            log.error("UserDetails is null - authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUserEmail = jwtUtil.getEmailFromCurrentToken();
        if (currentUserEmail == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate search query
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search query cannot be empty"));
        }

        if (q.trim().length() < 2) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search query must be at least 2 characters long"));
        }

        log.info("User {} searching for users with query: {}", LoggingUtil.maskEmail(currentUserEmail), q);

        PaginatedResponse<UserSearchDto> searchResults = userService.searchUsers(q.trim(), page, size);

        StructuredLoggingUtil.logBusinessEvent(
                "USER_SEARCH",
                currentUserEmail,
                "SEARCH_USERS",
                "SUCCESS",
                Map.of(
                        "searchQuery", q,
                        "resultCount", searchResults.getPagination().getTotalElements(),
                        "page", page,
                        "size", size));

        return ResponseEntity.ok(ApiResponse.success(searchResults, "Users found successfully"));
    }

    @GetMapping("/search/email")
    @Operation(summary = "Search Users by Email", description = "Search for users by email address only. Useful for finding specific users by their email.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSearchDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - Invalid email parameter")
    })
    public ResponseEntity<ApiResponse<List<UserSearchDto>>> searchUsersByEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String email) {

        if (userDetails == null) {
            log.error("UserDetails is null - authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUserEmail = jwtUtil.getEmailFromCurrentToken();
        if (currentUserEmail == null) {
            log.error("Failed to extract email from JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate email parameter
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email parameter cannot be empty"));
        }

        log.info("User {} searching for users by email: {}", LoggingUtil.maskEmail(currentUserEmail),
                LoggingUtil.maskEmail(email));

        List<UserSearchDto> searchResults = userService.searchUsersByEmail(email.trim());

        StructuredLoggingUtil.logBusinessEvent(
                "USER_EMAIL_SEARCH",
                currentUserEmail,
                "SEARCH_USERS_BY_EMAIL",
                "SUCCESS",
                Map.of(
                        "searchEmail", LoggingUtil.maskEmail(email),
                        "resultCount", searchResults.size()));

        return ResponseEntity.ok(ApiResponse.success(searchResults, "Users found successfully"));
    }
}
