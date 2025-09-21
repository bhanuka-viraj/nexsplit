package com.nexsplit.service;

import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.dto.user.UserSearchDto;
import com.nexsplit.dto.PaginatedResponse;
import com.nexsplit.model.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;

/**
 * Service interface for user management operations.
 * 
 * This service provides comprehensive user management functionality including
 * user registration, authentication, profile management, password operations,
 * and OAuth2 integration. It handles all user-related business logic and
 * integrates with authentication and authorization systems.
 * 
 * Key Features:
 * - User registration and email verification
 * - OAuth2 integration (Google Sign-In)
 * - User authentication and JWT token generation
 * - Profile management and updates
 * - Password operations (change, reset, validation)
 * - User deactivation and account management
 * - Email and username availability checking
 * - Password strength validation
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
public interface UserService {
    /**
     * Process OAuth2 user authentication and registration.
     * 
     * @param oidcUser OIDC user information from OAuth2 provider
     * @return User entity (existing or newly created)
     */
    User processOAuthUser(OidcUser oidcUser);

    /**
     * Process OAuth2 user by email and full name.
     * 
     * @param email    User's email address
     * @param fullName User's full name
     * @return User entity (existing or newly created)
     */
    User processOAuthUserByEmail(String email, String fullName);

    /**
     * Register a new user with email verification.
     * 
     * @param userDto User registration data
     * @return Created user entity
     */
    User registerUser(UserDto userDto);

    /**
     * Authenticate user and generate access token.
     * 
     * @param email    User's email address
     * @param password User's password
     * @return JWT access token
     */
    String loginUser(String email, String password);

    /**
     * Generate JWT access token for user.
     * 
     * @param user User entity
     * @return JWT access token
     */
    String generateAccessToken(User user);

    /**
     * Get user by email address.
     * 
     * @param email User's email address
     * @return User DTO
     */
    UserDto getUserByEmail(String email);

    /**
     * Get user by email for verification purposes.
     * 
     * @param email User's email address
     * @return User entity
     */
    User getUserByEmailForVerification(String email);

    /**
     * Confirm user's email address using confirmation token.
     * 
     * @param confirmationToken Email confirmation token
     * @param user              User entity
     * @return Updated user entity
     */
    User confirmEmail(String confirmationToken, User user);

    /**
     * Resend email verification to user.
     * 
     * @param email User's email address
     */
    void resendEmailVerification(String email);

    /**
     * Get user profile information.
     * 
     * @param email User's email address
     * @return User profile DTO
     */
    UserProfileDto getUserProfile(String email);

    /**
     * Update user profile information.
     * 
     * @param email         User's email address
     * @param updateUserDto Updated user data
     * @return Updated user profile DTO
     */
    UserProfileDto updateUserProfile(String email, UpdateUserDto updateUserDto);

    /**
     * Change user's password.
     * 
     * @param email           User's email address
     * @param currentPassword Current password
     * @param newPassword     New password
     */
    void changePassword(String email, String currentPassword, String newPassword);

    /**
     * Request password reset for user.
     * 
     * @param email User's email address
     */
    void requestPasswordReset(String email);

    /**
     * Reset user's password using reset token.
     * 
     * @param resetToken  Password reset token
     * @param newPassword New password
     */
    void resetPassword(String resetToken, String newPassword);

    /**
     * Deactivate user account.
     * 
     * @param email User's email address
     */
    void deactivateUser(String email);

    /**
     * Check if email address is available for registration.
     * 
     * @param email Email address to check
     * @return true if email is available, false otherwise
     */
    boolean isEmailAvailable(String email);

    /**
     * Check if username is available for registration.
     * 
     * @param username Username to check
     * @return true if username is available, false otherwise
     */
    boolean isUsernameAvailable(String username);

    /**
     * Validate password strength.
     * 
     * @param password Password to validate
     * @return true if password meets strength requirements, false otherwise
     */
    boolean validatePasswordStrength(String password);

    /**
     * Search users by search term (email, username, first name, or last name).
     * Only returns active users for invitation purposes.
     * 
     * @param searchTerm The search term to match against
     * @param page       Page number (0-based)
     * @param size       Page size
     * @return Paginated response with matching user search DTOs
     */
    PaginatedResponse<UserSearchDto> searchUsers(String searchTerm, int page, int size);

    /**
     * Search users by email only.
     * Only returns active users for invitation purposes.
     * 
     * @param email The email to search for
     * @return List of matching user search DTOs
     */
    List<UserSearchDto> searchUsersByEmail(String email);
}
