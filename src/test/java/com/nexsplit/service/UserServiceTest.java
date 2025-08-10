package com.nexsplit.service;

import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.exception.UserNotFoundException;
import com.nexsplit.mapper.user.UserMapperRegistry;
import com.nexsplit.model.User;
import com.nexsplit.repository.UserRepository;
import com.nexsplit.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserMapperRegistry userMapperRegistry;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private OidcUser testOidcUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID().toString())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .password("encodedPassword")
                .isEmailValidate(true)
                .isGoogleAuth(false)
                .status(User.Status.ACTIVE)
                .build();

        testOidcUser = mock(OidcUser.class);
        when(testOidcUser.getEmail()).thenReturn("oauth@example.com");
        when(testOidcUser.getFullName()).thenReturn("OAuth User");
        when(testOidcUser.getPreferredUsername()).thenReturn("oauthuser");
    }

    @Test
    void processOAuthUser_NewUser_ShouldCreateAndReturnUser() {
        // Given
        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("oauthuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID().toString());
            return user;
        });

        // When
        User result = userService.processOAuthUser(testOidcUser);

        // Then
        assertNotNull(result);
        assertEquals("oauth@example.com", result.getEmail());
        assertTrue(result.getIsGoogleAuth());
        assertTrue(result.getIsEmailValidate());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void processOAuthUser_ExistingUser_ShouldReturnExistingUser() {
        // Given
        User existingUser = User.builder()
                .id(UUID.randomUUID().toString())
                .email("oauth@example.com")
                .firstName("OAuth")
                .lastName("User")
                .username("oauthuser")
                .contactNumber("1234567890")
                .password("encodedPassword")
                .isEmailValidate(true)
                .isGoogleAuth(true)
                .status(User.Status.ACTIVE)
                .build();

        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(existingUser));

        // When
        User result = userService.processOAuthUser(testOidcUser);

        // Then
        assertNotNull(result);
        assertEquals(existingUser.getEmail(), result.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void processOAuthUser_UsernameTaken_ShouldGenerateUniqueUsername() {
        // Given
        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("oauthuser")).thenReturn(true);
        when(userRepository.existsByUsername("oauthuser_1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID().toString());
            return user;
        });

        // When
        User result = userService.processOAuthUser(testOidcUser);

        // Then
        assertNotNull(result);
        verify(userRepository).existsByUsername("oauthuser");
        verify(userRepository).existsByUsername("oauthuser_1");
    }

    @Test
    void registerUser_ValidData_ShouldCreateAndReturnUser() {
        // Given
        when(userRepository.existsActiveUserByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsActiveUserByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID().toString());
            return user;
        });

        // When
        User result = userService.registerUser("new@example.com", "StrongPass123!", "New", "User", "newuser",
                "1234567890");

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("StrongPass123!");
    }

    @Test
    void registerUser_EmailAlreadyExists_ShouldThrowException() {
        // Given
        when(userRepository.existsActiveUserByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("existing@example.com",
                "StrongPass123!", "New", "User", "newuser", "1234567890"));
    }

    @Test
    void registerUser_UsernameAlreadyExists_ShouldThrowException() {
        // Given
        when(userRepository.existsActiveUserByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsActiveUserByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("new@example.com", "StrongPass123!",
                "New", "User", "existinguser", "1234567890"));
    }

    @Test
    void registerUser_WeakPassword_ShouldThrowException() {
        // Given
        when(userRepository.existsActiveUserByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsActiveUserByUsername("newuser")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("new@example.com", "weak", "New", "User", "newuser", "1234567890"));
    }

    @Test
    void loginUser_ValidCredentials_ShouldReturnToken() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken("test@example.com", "USER")).thenReturn("jwtToken");

        // When
        String result = userService.loginUser("test@example.com", "password123");

        // Then
        assertEquals("jwtToken", result);
        verify(jwtUtil).generateAccessToken("test@example.com", "USER");
    }

    @Test
    void loginUser_InvalidCredentials_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.loginUser("test@example.com", "wrongpassword"));
    }

    @Test
    void loginUser_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.loginUser("nonexistent@example.com", "password123"));
    }

    @Test
    void getUserProfile_ValidEmail_ShouldReturnProfile() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        UserProfileDto expectedProfile = UserProfileDto.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .firstName(testUser.getFirstName())
                .lastName(testUser.getLastName())
                .username(testUser.getUsername())
                .contactNumber(testUser.getContactNumber())
                .fullName(testUser.getFullName())
                .build();
        when(userMapperRegistry.toProfileDto(testUser)).thenReturn(expectedProfile);

        // When
        UserProfileDto result = userService.getUserProfile("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());
        verify(userMapperRegistry).toProfileDto(testUser);
    }

    @Test
    void getUserProfile_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserProfile("nonexistent@example.com"));
    }

    @Test
    void updateUserProfile_ValidData_ShouldUpdateAndReturnProfile() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsActiveUserByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        UserProfileDto expectedProfile = UserProfileDto.builder()
                .id(testUser.getId())
                .email(testUser.getEmail())
                .firstName("Jane")
                .lastName("Smith")
                .username("newusername")
                .contactNumber("9876543210")
                .fullName("Jane Smith")
                .build();
        when(userMapperRegistry.toProfileDto(testUser)).thenReturn(expectedProfile);

        // When
        UserProfileDto result = userService.updateUserProfile("test@example.com", "Jane", "Smith", "newusername",
                "9876543210");

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(userMapperRegistry).toProfileDto(testUser);
    }

    @Test
    void updateUserProfile_UsernameTaken_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsActiveUserByUsername("takenusername")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateUserProfile("test@example.com", "Jane",
                "Smith", "takenusername", "9876543210"));
    }

    @Test
    void changePassword_ValidCurrentPassword_ShouldUpdatePassword() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.changePassword("test@example.com", "currentPassword", "StrongPass123!");

        // Then
        verify(passwordEncoder).encode("StrongPass123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_InvalidCurrentPassword_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("test@example.com", "wrongPassword", "StrongPass123!"));
    }

    @Test
    void changePassword_WeakNewPassword_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("test@example.com", "currentPassword", "weak"));
    }

    @Test
    void requestPasswordReset_ValidEmail_ShouldGenerateResetToken() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.requestPasswordReset("test@example.com");

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void requestPasswordReset_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.requestPasswordReset("nonexistent@example.com"));
    }

    @Test
    void resetPassword_ValidToken_ShouldUpdatePassword() {
        // Given
        String resetToken = "123456";
        when(userRepository.findByLastValidationCode(123456)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.resetPassword(resetToken, "StrongPass123!");

        // Then
        verify(passwordEncoder).encode("StrongPass123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_InvalidToken_ShouldThrowException() {
        // Given
        String resetToken = "invalid";
        when(userRepository.findByLastValidationCode(0)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(resetToken, "StrongPass123!"));
    }

    @Test
    void deactivateUser_ValidEmail_ShouldSoftDeleteUser() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser("test@example.com");

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deactivateUser_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.deactivateUser("nonexistent@example.com"));
    }

    @Test
    void isEmailAvailable_EmailNotExists_ShouldReturnTrue() {
        // Given
        when(userRepository.existsActiveUserByEmail("available@example.com")).thenReturn(false);

        // When
        boolean result = userService.isEmailAvailable("available@example.com");

        // Then
        assertTrue(result);
    }

    @Test
    void isEmailAvailable_EmailExists_ShouldReturnFalse() {
        // Given
        when(userRepository.existsActiveUserByEmail("taken@example.com")).thenReturn(true);

        // When
        boolean result = userService.isEmailAvailable("taken@example.com");

        // Then
        assertFalse(result);
    }

    @Test
    void isUsernameAvailable_UsernameNotExists_ShouldReturnTrue() {
        // Given
        when(userRepository.existsActiveUserByUsername("availableuser")).thenReturn(false);

        // When
        boolean result = userService.isUsernameAvailable("availableuser");

        // Then
        assertTrue(result);
    }

    @Test
    void isUsernameAvailable_UsernameExists_ShouldReturnFalse() {
        // Given
        when(userRepository.existsActiveUserByUsername("takenuser")).thenReturn(true);

        // When
        boolean result = userService.isUsernameAvailable("takenuser");

        // Then
        assertFalse(result);
    }

    @Test
    void validatePasswordStrength_StrongPassword_ShouldReturnTrue() {
        // When
        boolean result = userService.validatePasswordStrength("StrongPass123!");

        // Then
        assertTrue(result);
    }

    @Test
    void validatePasswordStrength_WeakPassword_ShouldReturnFalse() {
        // When
        boolean result = userService.validatePasswordStrength("weak");

        // Then
        assertFalse(result);
    }

    @Test
    void validatePasswordStrength_NullPassword_ShouldReturnFalse() {
        // When
        boolean result = userService.validatePasswordStrength(null);

        // Then
        assertFalse(result);
    }
}
