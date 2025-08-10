package com.nexsplit.mapper;

import com.nexsplit.dto.user.UserDto;
import com.nexsplit.mapper.user.UserMapper;
import com.nexsplit.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toEntity_ValidUserDto_ShouldCreateUserEntity() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId("test-id");
        userDto.setEmail("test@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setUsername("johndoe");
        userDto.setContactNumber("1234567890");
        userDto.setPassword("password123");

        // When
        User user = userMapper.toEntity(userDto);

        // Then
        assertNotNull(user);
        assertEquals("test-id", user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("1234567890", user.getContactNumber());
        assertFalse(user.getIsEmailValidate());
        assertFalse(user.getIsGoogleAuth());
        assertEquals(User.Status.ACTIVE, user.getStatus());
        assertNull(user.getPassword()); // Password should not be set by mapper
    }

    @Test
    void toEntity_UserDtoWithoutId_ShouldGenerateId() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setUsername("johndoe");
        userDto.setContactNumber("1234567890");

        // When
        User user = userMapper.toEntity(userDto);

        // Then
        assertNotNull(user);
        assertNotNull(user.getId());
        assertTrue(user.getId().length() > 0);
    }

    @Test
    void toEntity_NullUserDto_ShouldReturnNull() {
        // When
        User user = userMapper.toEntity(null);

        // Then
        assertNull(user);
    }

    @Test
    void toDto_ValidUser_ShouldCreateUserDto() {
        // Given
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
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // When
        UserDto userDto = userMapper.toDto(user);

        // Then
        assertNotNull(userDto);
        assertEquals("test-id", userDto.getId());
        assertEquals("test@example.com", userDto.getEmail());
        assertEquals("John", userDto.getFirstName());
        assertEquals("Doe", userDto.getLastName());
        assertEquals("johndoe", userDto.getUsername());
        assertEquals("1234567890", userDto.getContactNumber());
        assertNull(userDto.getPassword()); // Password should not be included for security
    }

    @Test
    void toDto_NullUser_ShouldReturnNull() {
        // When
        UserDto userDto = userMapper.toDto(null);

        // Then
        assertNull(userDto);
    }

    @Test
    void updateEntityFromDto_ValidData_ShouldUpdateUser() {
        // Given
        User user = User.builder()
                .id("test-id")
                .email("old@example.com")
                .firstName("Old")
                .lastName("Name")
                .username("olduser")
                .contactNumber("0000000000")
                .build();

        UserDto userDto = new UserDto();
        userDto.setEmail("new@example.com");
        userDto.setFirstName("New");
        userDto.setLastName("Name");
        userDto.setUsername("newuser");
        userDto.setContactNumber("1234567890");

        // When
        User updatedUser = userMapper.updateEntityFromDto(user, userDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals("New", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertEquals("newuser", updatedUser.getUsername());
        assertEquals("1234567890", updatedUser.getContactNumber());
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

        UserDto userDto = new UserDto();
        userDto.setFirstName("New"); // Only update firstName

        // When
        User updatedUser = userMapper.updateEntityFromDto(user, userDto);

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
        UserDto userDto = new UserDto();
        userDto.setFirstName("New");

        // When
        User updatedUser = userMapper.updateEntityFromDto(null, userDto);

        // Then
        assertNull(updatedUser);
    }

    @Test
    void updateEntityFromDto_NullUserDto_ShouldReturnOriginalUser() {
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
        User updatedUser = userMapper.updateEntityFromDto(user, null);

        // Then
        assertSame(user, updatedUser);
    }
}
