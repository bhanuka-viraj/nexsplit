package com.nexsplit.service.impl;

import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.exception.UserNotFoundException;
import com.nexsplit.exception.UserUnauthorizedException;
import com.nexsplit.mapper.user.UserMapperRegistry;
import com.nexsplit.model.User;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.service.UserService;

import com.nexsplit.service.AuditService;
import com.nexsplit.service.EmailService;
import com.nexsplit.util.JwtUtil;
import com.nexsplit.util.LoggingUtil;
import com.nexsplit.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapperRegistry userMapperRegistry;

    private final AuditService auditService;
    private final EmailService emailService;

    @Transactional
    public User processOAuthUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String fullName = oidcUser.getFullName() != null ? oidcUser.getFullName() : "Unknown";
        String baseUsername = oidcUser.getPreferredUsername() != null ? oidcUser.getPreferredUsername()
                : email.split("@")[0];

        // Generate unique username
        String username = generateUniqueUsername(baseUsername);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Creating new OAuth user: {}", LoggingUtil.maskEmail(email));
                    User newUser = User.builder()
                            .id(UUID.randomUUID().toString())
                            .email(email)
                            .username(username)
                            .isGoogleAuth(true)
                            .isEmailValidate(true)
                            .build();
                    newUser.setFullName(fullName);
                    return userRepository.save(newUser);
                });

        // Update name if changed
        if (!fullName.equals(user.getFullName())) {
            log.info("Updating OAuth user profile: {}", LoggingUtil.maskEmail(email));
            user.setFullName(fullName);
            user = userRepository.save(user);
        }

        return user;
    }

    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter++;
        }
        return username;
    }

    @Transactional
    public User registerUser(UserDto userDto) {
        log.info("Processing user registration for: {}", LoggingUtil.maskEmail(userDto.getEmail()));

        // Validate input
        if (userRepository.existsActiveUserByEmail(userDto.getEmail())) {
            log.warn("Registration failed - email already exists: {}", LoggingUtil.maskEmail(userDto.getEmail()));
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsActiveUserByUsername(userDto.getUsername())) {
            log.warn("Registration failed - username already taken: {}", userDto.getUsername());
            throw new IllegalArgumentException("Username already taken");
        }
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            log.warn("Registration failed - password is null or empty for: {}",
                    LoggingUtil.maskEmail(userDto.getEmail()));
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (!PasswordUtil.isStrongPassword(userDto.getPassword())) {
            log.warn("Registration failed - weak password for: {}", LoggingUtil.maskEmail(userDto.getEmail()));
            throw new IllegalArgumentException(
                    "Password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(userDto.getPassword()));
        }

        // Use mapper to create User entity from DTO
        User user = userMapperRegistry.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Set email as unverified initially
        user.setIsEmailValidate(false);

        // Generate email verification token (6-digit code)
        int verificationToken = (int) (Math.random() * 900000) + 100000;
        user.setLastValidationCode(verificationToken);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", LoggingUtil.maskEmail(userDto.getEmail()));

        // Send email verification asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmailVerification(user.getEmail(), String.valueOf(verificationToken),
                        user.getUsername());
                log.info("Email verification sent successfully to: {}", LoggingUtil.maskEmail(user.getEmail()));
            } catch (Exception e) {
                log.error("Failed to send email verification to: {}", LoggingUtil.maskEmail(user.getEmail()), e);
                // Don't throw exception to avoid affecting user registration
            }
        });

        // Log audit event asynchronously
        CompletableFuture.runAsync(() -> {
            auditService.logUserActionAsync(savedUser.getId(), "USER_REGISTERED", "New user registered successfully");
        }).exceptionally(throwable -> {
            log.error("Error in audit logging for user registration: {}", throwable.getMessage(), throwable);
            return null;
        });

        return savedUser;
    }

    public String loginUser(String email, String password) {
        log.debug("Processing login attempt for: {}", LoggingUtil.maskEmail(email));

        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found or inactive: {}", LoggingUtil.maskEmail(email));
                    return new IllegalArgumentException("User not found or inactive");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed - invalid credentials for: {}", LoggingUtil.maskEmail(email));
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Check if email is verified
        if (!user.getIsEmailValidate()) {
            log.warn("Login failed - email not verified for: {}", LoggingUtil.maskEmail(email));
            throw new IllegalArgumentException("Email not verified. Please check your email and verify your account.");
        }

        log.info("User login successful: {}", LoggingUtil.maskEmail(email));
        return jwtUtil.generateAccessToken(email, "USER");
    }

    public String generateAccessToken(User user) {
        return jwtUtil.generateAccessToken(user.getEmail(), "USER");
    }

    public String generateRefreshToken(String userEmail) {
        return jwtUtil.generateRefreshToken(userEmail);
    }

    public String generateAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UserUnauthorizedException("Invalid refresh token");
        }
        String email = jwtUtil.getEmailFromToken(refreshToken);
        User user = userRepository.getUserByEmail(email);
        return jwtUtil.generateAccessToken(email, "USER");
    }

    public String getEmailFromRefreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UserUnauthorizedException("Invalid refresh token");
        }
        return jwtUtil.getEmailFromToken(refreshToken);
    }

    public UserDto getUserByEmail(String email) {
        return userMapperRegistry.toDto(userRepository.getUserByEmail(email));
    }

    public User getUserByEmailForVerification(String email) {
        return userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapperRegistry.toProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateUserProfile(String email, UpdateUserDto updateUserDto) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if username is being changed and if it's already taken
        if (!updateUserDto.getUsername().equals(user.getUsername())
                && userRepository.existsActiveUserByUsername(updateUserDto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Use mapper to update User entity from DTO
        user = userMapperRegistry.updateEntityFromUpdateDto(user, updateUserDto);

        User updatedUser = userRepository.save(user);
        return userMapperRegistry.toProfileDto(updatedUser);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        log.info("Processing password change for: {}", LoggingUtil.maskEmail(email));

        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password change failed - user not found: {}", LoggingUtil.maskEmail(email));
                    return new UserNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password change failed - incorrect current password for: {}", LoggingUtil.maskEmail(email));
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!PasswordUtil.isStrongPassword(newPassword)) {
            log.warn("Password change failed - weak new password for: {}", LoggingUtil.maskEmail(email));
            throw new IllegalArgumentException(
                    "New password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(newPassword));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for: {}", LoggingUtil.maskEmail(email));
    }

    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Processing password reset request for: {}", LoggingUtil.maskEmail(email));

        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset request failed - user not found: {}", LoggingUtil.maskEmail(email));
                    return new UserNotFoundException("User not found");
                });

        // Generate reset token
        int resetToken = (int) (Math.random() * 900000) + 100000; // 6-digit number
        user.setLastValidationCode(resetToken);
        userRepository.save(user);

        log.info("Password reset token generated for: {} - Token: {}", LoggingUtil.maskEmail(email), resetToken);

        // Send password reset email
        try {
            emailService.sendPasswordResetEmail(email, String.valueOf(resetToken), user.getUsername());
            log.info("Password reset email sent successfully to: {}", LoggingUtil.maskEmail(email));
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", LoggingUtil.maskEmail(email), e);
            // Don't throw exception to avoid revealing if email exists
        }

        // Log audit event asynchronously
        CompletableFuture.runAsync(() -> {
            auditService.logSecurityEventAsync(user.getId(), "PASSWORD_RESET_REQUESTED",
                    "Password reset requested via email");
        }).exceptionally(throwable -> {
            log.error("Error in audit logging for password reset: {}", throwable.getMessage(), throwable);
            return null;
        });
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        log.info("Processing password reset with token: {}", LoggingUtil.maskSensitiveData(resetToken));

        // Find user by reset token
        int tokenValue;
        try {
            tokenValue = Integer.parseInt(resetToken);
        } catch (NumberFormatException e) {
            log.warn("Password reset failed - invalid token format: {}", resetToken);
            throw new IllegalArgumentException("Invalid reset token format");
        }

        User user = userRepository.findByLastValidationCode(tokenValue)
                .orElseThrow(() -> {
                    log.warn("Password reset failed - invalid token: {}", resetToken);
                    return new IllegalArgumentException("Invalid reset token");
                });

        if (!PasswordUtil.isStrongPassword(newPassword)) {
            log.warn("Password reset failed - weak password for user: {}", LoggingUtil.maskEmail(user.getEmail()));
            throw new IllegalArgumentException(
                    "New password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(newPassword));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastValidationCode(0); // Clear the reset token
        userRepository.save(user);
        log.info("Password reset successful for: {}", LoggingUtil.maskEmail(user.getEmail()));
    }

    @Transactional
    public void deactivateUser(String email) {
        log.info("Processing user deactivation for: {}", LoggingUtil.maskEmail(email));

        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User deactivation failed - user not found: {}", LoggingUtil.maskEmail(email));
                    return new UserNotFoundException("User not found");
                });
        user.softDelete();
        userRepository.save(user);
        log.info("User deactivated successfully: {}", LoggingUtil.maskEmail(email));
    }

    @Transactional
    public void resendEmailVerification(String email) {
        log.info("Processing email verification resend for: {}", LoggingUtil.maskEmail(email));

        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Email verification resend failed - user not found: {}", LoggingUtil.maskEmail(email));
                    return new UserNotFoundException("User not found");
                });

        // Check if email is already verified
        if (user.getIsEmailValidate()) {
            log.warn("Email verification resend failed - email already verified: {}", LoggingUtil.maskEmail(email));
            throw new IllegalArgumentException("Email is already verified");
        }

        // Generate new verification token
        int verificationToken = (int) (Math.random() * 900000) + 100000;
        user.setLastValidationCode(verificationToken);
        userRepository.save(user);

        log.info("Email verification token regenerated for: {} - Token: {}", LoggingUtil.maskEmail(email),
                verificationToken);

        // Send email verification asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmailVerification(email, String.valueOf(verificationToken), user.getUsername());
                log.info("Email verification resent successfully to: {}", LoggingUtil.maskEmail(email));
            } catch (Exception e) {
                log.error("Failed to resend email verification to: {}", LoggingUtil.maskEmail(email), e);
                // Don't throw exception to avoid affecting the request
            }
        });

        // Log audit event asynchronously
        CompletableFuture.runAsync(() -> {
            auditService.logSecurityEventAsync(user.getId(), "EMAIL_VERIFICATION_RESENT",
                    "Email verification resent to user");
        }).exceptionally(throwable -> {
            log.error("Error in audit logging for email verification resend: {}", throwable.getMessage(), throwable);
            return null;
        });
    }

    @Transactional
    public User confirmEmail(String confirmationToken) {
        log.info("Processing email confirmation with token: {}", LoggingUtil.maskSensitiveData(confirmationToken));

        // Find user by confirmation token
        int tokenValue;
        try {
            tokenValue = Integer.parseInt(confirmationToken);
        } catch (NumberFormatException e) {
            log.warn("Email confirmation failed - invalid token format: {}", confirmationToken);
            throw new IllegalArgumentException("Invalid confirmation token format");
        }

        User user = userRepository.findByLastValidationCode(tokenValue)
                .orElseThrow(() -> {
                    log.warn("Email confirmation failed - invalid token: {}", confirmationToken);
                    return new IllegalArgumentException("Invalid confirmation token");
                });

        // Check if email is already confirmed
        if (user.getIsEmailValidate()) {
            log.warn("Email confirmation failed - email already confirmed for: {}",
                    LoggingUtil.maskEmail(user.getEmail()));
            throw new IllegalArgumentException("Email is already confirmed");
        }

        // Mark email as verified and clear the token
        user.setIsEmailValidate(true);
        user.setLastValidationCode(0); // Clear the confirmation token
        User confirmedUser = userRepository.save(user);

        log.info("Email confirmed successfully for: {}", LoggingUtil.maskEmail(confirmedUser.getEmail()));

        // Send welcome email asynchronously after successful verification
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendWelcomeEmail(confirmedUser.getEmail(), confirmedUser.getUsername());
                log.info("Welcome email sent successfully after verification to: {}",
                        LoggingUtil.maskEmail(confirmedUser.getEmail()));
            } catch (Exception e) {
                log.error("Failed to send welcome email after verification to: {}",
                        LoggingUtil.maskEmail(confirmedUser.getEmail()), e);
                // Don't throw exception to avoid affecting email confirmation
            }
        });

        // Log audit event for email confirmation
        CompletableFuture.runAsync(() -> {
            auditService.logSecurityEventAsync(confirmedUser.getId(), "EMAIL_CONFIRMED",
                    "Email confirmed successfully");
        }).exceptionally(throwable -> {
            log.error("Error in audit logging for email confirmation: {}", throwable.getMessage(), throwable);
            return null;
        });

        return confirmedUser;
    }

    // Validation methods
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsActiveUserByEmail(email);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsActiveUserByUsername(username);
    }

    public boolean validatePasswordStrength(String password) {
        return PasswordUtil.isStrongPassword(password);
    }
}