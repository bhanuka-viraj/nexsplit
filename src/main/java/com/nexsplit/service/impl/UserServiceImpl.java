package com.nexsplit.service.impl;

import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UpdateUserRequest;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.dto.user.UserSearchDto;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.exception.UserNotFoundException;
import com.nexsplit.exception.BusinessException;
import com.nexsplit.dto.ErrorCode;
import com.nexsplit.mapper.user.UserMapStruct;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.nexsplit.util.PaginationUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapStruct userMapStruct;

    private final AuditService auditService;
    private final EmailService emailService;

    @Transactional
    public User processOAuthUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String fullName = oidcUser.getFullName() != null ? oidcUser.getFullName() : "Unknown";
        return processOAuthUserByEmail(email, fullName);
    }

    @Override
    @Transactional
    public User processOAuthUserByEmail(String email, String fullName) {
        String baseUsername = email.split("@")[0];

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
                    newUser.setFullName(fullName != null ? fullName : "Unknown");
                    return userRepository.save(newUser);
                });

        // Update name if changed
        if (fullName != null && !fullName.equals(user.getFullName())) {
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
            throw new BusinessException("Email already registered", ErrorCode.USER_EMAIL_EXISTS);
        }
        if (userRepository.existsActiveUserByUsername(userDto.getUsername())) {
            log.warn("Registration failed - username already taken: {}", userDto.getUsername());
            throw new BusinessException("Username already taken", ErrorCode.USER_USERNAME_EXISTS);
        }
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            log.warn("Registration failed - password is null or empty for: {}",
                    LoggingUtil.maskEmail(userDto.getEmail()));
            throw new BusinessException("Password cannot be null or empty", ErrorCode.USER_PASSWORD_INVALID);
        }
        if (!PasswordUtil.isStrongPassword(userDto.getPassword())) {
            log.warn("Registration failed - weak password for: {}", LoggingUtil.maskEmail(userDto.getEmail()));
            throw new BusinessException(
                    "Password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(userDto.getPassword()),
                    ErrorCode.USER_PASSWORD_WEAK);
        }

        // Use mapper to create User entity from DTO
        User user = userMapStruct.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Set email as unverified initially
        user.setIsEmailValidate(false);

        // Generate email verification token (6-digit code)
        // int verificationToken = (int) (Math.random() * 900000) + 100000;
        int verificationToken = 123456;
        user.setLastValidationCode(verificationToken);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", LoggingUtil.maskEmail(userDto.getEmail()));

        // Send email verification asynchronously
        emailService.sendEmailVerification(user.getEmail(), String.valueOf(verificationToken),
                user.getUsername())
                .exceptionally(throwable -> {
                    log.error("Failed to send email verification to: {}", LoggingUtil.maskEmail(user.getEmail()),
                            throwable);
                    return null;
                });

        // Log audit event asynchronously
        auditService.logUserActionAsync(savedUser.getId(), "USER_REGISTERED", "New user registered successfully");

        return savedUser;
    }

    public String loginUser(String email, String password) {
        log.debug("Processing login attempt for: {}", LoggingUtil.maskEmail(email));

        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found or inactive: {}", LoggingUtil.maskEmail(email));
                    return new BusinessException("User not found or inactive", ErrorCode.USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed - invalid credentials for: {}", LoggingUtil.maskEmail(email));
            throw new BusinessException("Invalid credentials", ErrorCode.USER_INVALID_CREDENTIALS);
        }

        // Check if email is verified
        if (!user.getIsEmailValidate()) {
            log.warn("Login failed - email not verified for: {}", LoggingUtil.maskEmail(email));
            throw new BusinessException("Email not verified. Please check your email and verify your account.",
                    ErrorCode.USER_EMAIL_NOT_VERIFIED);
        }

        log.info("User login successful: {}", LoggingUtil.maskEmail(email));
        return jwtUtil.generateAccessToken(user.getId(), user.getEmail(), "USER");
    }

    public String generateAccessToken(User user) {
        return jwtUtil.generateAccessToken(user.getId(), user.getEmail(), "USER");
    }

    public UserDto getUserByEmail(String email) {
        return userMapStruct.toDto(userRepository.getUserByEmail(email));
    }

    public User getUserByEmailForVerification(String email) {
        return userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapStruct.toProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateUserProfile(String email, UpdateUserDto updateUserDto) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if username is being changed and if it's already taken
        if (!updateUserDto.getUsername().equals(user.getUsername())
                && userRepository.existsActiveUserByUsername(updateUserDto.getUsername())) {
            throw new BusinessException("Username already taken", ErrorCode.USER_USERNAME_EXISTS);
        }

        // Use mapper to update User entity from DTO
        UpdateUserRequest updateRequest = userMapStruct.toUpdateUserRequest(updateUserDto);
        userMapStruct.updateEntityFromRequest(updateRequest, user);

        User updatedUser = userRepository.save(user);
        return userMapStruct.toProfileDto(updatedUser);
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
            throw new BusinessException("Current password is incorrect", ErrorCode.USER_INVALID_CREDENTIALS);
        }

        if (!PasswordUtil.isStrongPassword(newPassword)) {
            log.warn("Password change failed - weak new password for: {}", LoggingUtil.maskEmail(email));
            throw new BusinessException(
                    "New password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(newPassword),
                    ErrorCode.USER_PASSWORD_WEAK);
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
        // int resetToken = 123456;
        user.setLastValidationCode(resetToken);
        userRepository.save(user);

        log.info("Password reset token generated for: {} - Token: {}", LoggingUtil.maskEmail(email), resetToken);

        // Send password reset email asynchronously
        emailService.sendPasswordResetEmail(email, String.valueOf(resetToken), user.getUsername())
                .exceptionally(throwable -> {
                    log.error("Failed to send password reset email to: {}", LoggingUtil.maskEmail(email), throwable);
                    return null;
                });

        // Log audit event asynchronously
        auditService.logSecurityEventAsync(user.getId(), "PASSWORD_RESET_REQUESTED",
                "Password reset requested via email");
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
            throw new BusinessException("Invalid reset token format", ErrorCode.USER_INVALID_TOKEN);
        }

        User user = userRepository.findByLastValidationCode(tokenValue)
                .orElseThrow(() -> {
                    log.warn("Password reset failed - invalid token: {}", resetToken);
                    return new BusinessException("Invalid reset token", ErrorCode.USER_INVALID_TOKEN);
                });

        if (!PasswordUtil.isStrongPassword(newPassword)) {
            log.warn("Password reset failed - weak password for user: {}", LoggingUtil.maskEmail(user.getEmail()));
            throw new BusinessException(
                    "New password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(newPassword),
                    ErrorCode.USER_PASSWORD_WEAK);
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
        user.softDelete(user.getId());
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
            throw new BusinessException("Email is already verified", ErrorCode.USER_EMAIL_ALREADY_VERIFIED);
        }

        // Generate new verification token
        int verificationToken = (int) (Math.random() * 900000) + 100000;
        user.setLastValidationCode(verificationToken);
        userRepository.save(user);

        log.info("Email verification token regenerated for: {} - Token: {}", LoggingUtil.maskEmail(email),
                verificationToken);

        // Send email verification asynchronously
        emailService.sendEmailVerification(email, String.valueOf(verificationToken), user.getUsername())
                .exceptionally(throwable -> {
                    log.error("Failed to resend email verification to: {}", LoggingUtil.maskEmail(email), throwable);
                    return null;
                });

        // Log audit event asynchronously
        auditService.logSecurityEventAsync(user.getId(), "EMAIL_VERIFICATION_RESENT",
                "Email verification resent to user");
    }

    @Transactional
    public User confirmEmail(String confirmationToken, User user) {
        log.info("Processing email confirmation with token: {}", LoggingUtil.maskSensitiveData(confirmationToken));

        // Find user by confirmation token
        int tokenValue;
        try {
            tokenValue = Integer.parseInt(confirmationToken);
        } catch (NumberFormatException e) {
            log.warn("Email confirmation failed - invalid token format: {}", confirmationToken);
            throw new BusinessException("Invalid confirmation token format", ErrorCode.USER_INVALID_TOKEN);
        }

        // Check if email is already confirmed
        if (user.getIsEmailValidate()) {
            log.warn("Email confirmation failed - email already confirmed for: {}",
                    LoggingUtil.maskEmail(user.getEmail()));
            throw new BusinessException("Email is already confirmed", ErrorCode.USER_EMAIL_ALREADY_VERIFIED);
        }

        if (user.getLastValidationCode() != tokenValue) {
            log.warn("Email confirmation failed - invalid token for: {}", LoggingUtil.maskEmail(user.getEmail()));
            throw new BusinessException("Invalid confirmation token", ErrorCode.USER_INVALID_TOKEN);
        }
        // Mark email as verified and clear the token
        user.setIsEmailValidate(true);
        user.setLastValidationCode(0); // Clear the confirmation token
        User confirmedUser = userRepository.save(user);

        log.info("Email confirmed successfully for: {}", LoggingUtil.maskEmail(confirmedUser.getEmail()));

        // Send welcome email asynchronously after successful verification
        emailService.sendWelcomeEmail(confirmedUser.getEmail(), confirmedUser.getUsername())
                .exceptionally(throwable -> {
                    log.error("Failed to send welcome email after verification to: {}",
                            LoggingUtil.maskEmail(confirmedUser.getEmail()), throwable);
                    return null;
                });

        // Log audit event for email confirmation
        auditService.logSecurityEventAsync(confirmedUser.getId(), "EMAIL_CONFIRMED",
                "Email confirmed successfully");

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

    // Search methods
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<UserSearchDto> searchUsers(String searchTerm, int page, int size) {
        log.debug("Searching users with term: {} and pagination: page={}, size={}", searchTerm, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchActiveUsers(searchTerm, pageable);

        List<UserSearchDto> userDtos = users.getContent().stream()
                .map(this::convertToUserSearchDto)
                .collect(Collectors.toList());

        return PaginationUtil.createPaginatedResponse(
                userDtos,
                page,
                size,
                users.getTotalElements(),
                "/api/v1/users/search");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchDto> searchUsersByEmail(String email) {
        log.debug("Searching users by email: {}", LoggingUtil.maskEmail(email));

        List<User> users = userRepository.searchActiveUsersByEmail(email);
        return users.stream()
                .map(this::convertToUserSearchDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert User entity to UserSearchDto.
     * 
     * @param user User entity
     * @return UserSearchDto
     */
    private UserSearchDto convertToUserSearchDto(User user) {
        return UserSearchDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .contactNumber(user.getContactNumber())
                .profilePictureUrl(null) // TODO: Add profile picture support
                .isEmailVerified(user.getIsEmailValidate())
                .isGoogleAuth(user.getIsGoogleAuth())
                .status(user.getStatus() != null ? user.getStatus().name() : "ACTIVE")
                .build();
    }
}