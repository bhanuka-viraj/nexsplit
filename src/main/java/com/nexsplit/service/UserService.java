package com.nexsplit.service;

import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.exception.UserNotFoundException;
import com.nexsplit.exception.UserUnauthorizedException;
import com.nexsplit.mapper.user.UserMapperRegistry;
import com.nexsplit.model.User;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.util.JwtUtil;
import com.nexsplit.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapperRegistry userMapperRegistry;

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
    public User registerUser(String email, String password, String firstName, String lastName, String username,
            String contactNumber) {
        // Validate input
        if (userRepository.existsActiveUserByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsActiveUserByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (!PasswordUtil.isStrongPassword(password)) {
            throw new IllegalArgumentException(
                    "Password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(password));
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .contactNumber(contactNumber)
                .isGoogleAuth(false)
                .isEmailValidate(false)
                .build();
        return userRepository.save(user);
    }

    public String loginUser(String email, String password) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found or inactive"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

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

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapperRegistry.toProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateUserProfile(String email, String firstName, String lastName, String username,
            String contactNumber) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if username is being changed and if it's already taken
        if (!username.equals(user.getUsername()) && userRepository.existsActiveUserByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setContactNumber(contactNumber);

        User updatedUser = userRepository.save(user);
        return userMapperRegistry.toProfileDto(updatedUser);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!PasswordUtil.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException(
                    "New password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(newPassword));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Generate reset token
        int resetToken = (int) (Math.random() * 900000) + 100000; // 6-digit number
        user.setLastValidationCode(resetToken);
        userRepository.save(user);

        // TODO: Send email with reset token
        // just logging for now
        System.out.println("Password reset token for " + email + ": " + resetToken);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        // Find user by reset token
        int tokenValue;
        try {
            tokenValue = Integer.parseInt(resetToken);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid reset token format");
        }

        User user = userRepository.findByLastValidationCode(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (!PasswordUtil.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException(
                    "New password is not strong enough. " + PasswordUtil.getPasswordStrengthMessage(newPassword));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastValidationCode(0); // Clear the reset token
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(String email) {
        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.softDelete();
        userRepository.save(user);
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