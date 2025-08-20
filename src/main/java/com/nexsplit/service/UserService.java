package com.nexsplit.service;

import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.dto.user.UserDto;
import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.model.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface UserService {
    User processOAuthUser(OidcUser oidcUser);

    User registerUser(UserDto userDto);

    String loginUser(String email, String password);

    String generateAccessToken(User user);

    String generateRefreshToken(String userEmail);

    String generateAccessToken(String refreshToken);

    String getEmailFromRefreshToken(String refreshToken);

    UserDto getUserByEmail(String email);

    UserProfileDto getUserProfile(String email);

    UserProfileDto updateUserProfile(String email, UpdateUserDto updateUserDto);

    void changePassword(String email, String currentPassword, String newPassword);

    void requestPasswordReset(String email);

    void resetPassword(String resetToken, String newPassword);

    void deactivateUser(String email);

    boolean isEmailAvailable(String email);

    boolean isUsernameAvailable(String username);

    boolean validatePasswordStrength(String password);
}
