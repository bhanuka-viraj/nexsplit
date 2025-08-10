package com.nexsplit.mapper;

import com.nexsplit.dto.user.UserProfileDto;
import com.nexsplit.mapper.user.UserProfileMapper;
import com.nexsplit.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileMapperTest {

    private UserProfileMapper userProfileMapper;

    @BeforeEach
    void setUp() {
        userProfileMapper = new UserProfileMapper();
    }

    @Test
    void toDto_ValidUser_ShouldCreateUserProfileDto() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .password("encodedPassword")
                .isEmailValidate(true)
                .isGoogleAuth(false)
                .status(User.Status.ACTIVE)
                .createdAt(now)
                .modifiedAt(now)
                .build();

        // When
        UserProfileDto userProfileDto = userProfileMapper.toDto(user);

        // Then
        assertNotNull(userProfileDto);
        assertEquals("test-id", userProfileDto.getId());
        assertEquals("test@example.com", userProfileDto.getEmail());
        assertEquals("John", userProfileDto.getFirstName());
        assertEquals("Doe", userProfileDto.getLastName());
        assertEquals("johndoe", userProfileDto.getUsername());
        assertEquals("1234567890", userProfileDto.getContactNumber());
        assertEquals("John Doe", userProfileDto.getFullName());
        assertTrue(userProfileDto.getIsEmailValidate());
        assertFalse(userProfileDto.getIsGoogleAuth());
        assertEquals(User.Status.ACTIVE, userProfileDto.getStatus());
        assertEquals(now, userProfileDto.getCreatedAt());
        assertEquals(now, userProfileDto.getModifiedAt());
    }

    @Test
    void toDto_UserWithNullNames_ShouldHandleGracefully() {
        // Given
        User user = User.builder()
                .id("test-id")
                .email("test@example.com")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        // When
        UserProfileDto userProfileDto = userProfileMapper.toDto(user);

        // Then
        assertNotNull(userProfileDto);
        assertEquals("test-id", userProfileDto.getId());
        assertEquals("test@example.com", userProfileDto.getEmail());
        assertNull(userProfileDto.getFirstName());
        assertNull(userProfileDto.getLastName());
        assertEquals("johndoe", userProfileDto.getUsername());
        assertEquals("johndoe", userProfileDto.getFullName()); // Should fall back to username
    }

    @Test
    void toDto_NullUser_ShouldReturnNull() {
        // When
        UserProfileDto userProfileDto = userProfileMapper.toDto(null);

        // Then
        assertNull(userProfileDto);
    }

    @Test
    void updateEntityFromDto_ValidData_ShouldUpdateUser() {
        // Given
        User user = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("Old")
                .lastName("Name")
                .username("olduser")
                .contactNumber("0000000000")
                .build();

        UserProfileDto userProfileDto = UserProfileDto.builder()
                .firstName("New")
                .lastName("Name")
                .username("newuser")
                .contactNumber("1234567890")
                .build();

        // When
        User updatedUser = userProfileMapper.updateEntityFromDto(user, userProfileDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals("New", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertEquals("newuser", updatedUser.getUsername());
        assertEquals("1234567890", updatedUser.getContactNumber());
        // Email should remain unchanged as it's not updatable via profile
        assertEquals("test@example.com", updatedUser.getEmail());
    }

    @Test
    void updateEntityFromDto_PartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        User user = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("Old")
                .lastName("Name")
                .username("olduser")
                .contactNumber("0000000000")
                .build();

        UserProfileDto userProfileDto = UserProfileDto.builder()
                .firstName("New") // Only update firstName
                .build();

        // When
        User updatedUser = userProfileMapper.updateEntityFromDto(user, userProfileDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals("New", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName()); // Should remain unchanged
        assertEquals("olduser", updatedUser.getUsername()); // Should remain unchanged
        assertEquals("0000000000", updatedUser.getContactNumber()); // Should remain unchanged
    }

    @Test
    void updateEntityFromDto_NullUser_ShouldReturnNull() {
        // Given
        UserProfileDto userProfileDto = UserProfileDto.builder()
                .firstName("New")
                .build();

        // When
        User updatedUser = userProfileMapper.updateEntityFromDto(null, userProfileDto);

        // Then
        assertNull(updatedUser);
    }

    @Test
    void updateEntityFromDto_NullUserProfileDto_ShouldReturnOriginalUser() {
        // Given
        User user = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        // When
        User updatedUser = userProfileMapper.updateEntityFromDto(user, null);

        // Then
        assertSame(user, updatedUser);
    }

    @Test
    void toUpdateDto_ValidUser_ShouldCreatePartialDto() {
        // Given
        User user = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .isEmailValidate(true)
                .isGoogleAuth(false)
                .status(User.Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // When
        UserProfileDto userProfileDto = userProfileMapper.toUpdateDto(user);

        // Then
        assertNotNull(userProfileDto);
        assertEquals("John", userProfileDto.getFirstName());
        assertEquals("Doe", userProfileDto.getLastName());
        assertEquals("johndoe", userProfileDto.getUsername());
        assertEquals("1234567890", userProfileDto.getContactNumber());
        // Should not include sensitive or non-updatable fields
        assertNull(userProfileDto.getId());
        assertNull(userProfileDto.getEmail());
        assertNull(userProfileDto.getIsEmailValidate());
        assertNull(userProfileDto.getIsGoogleAuth());
        assertNull(userProfileDto.getStatus());
        assertNull(userProfileDto.getCreatedAt());
        assertNull(userProfileDto.getModifiedAt());
    }

    @Test
    void toUpdateDto_NullUser_ShouldReturnNull() {
        // When
        UserProfileDto userProfileDto = userProfileMapper.toUpdateDto(null);

        // Then
        assertNull(userProfileDto);
    }
}
