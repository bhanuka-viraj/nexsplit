package com.nexsplit.mapper;

import com.nexsplit.dto.user.UpdateUserDto;
import com.nexsplit.mapper.user.UpdateUserMapper;
import com.nexsplit.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserMapperTest {

    private UpdateUserMapper updateUserMapper;

    @BeforeEach
    void setUp() {
        updateUserMapper = new UpdateUserMapper();
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

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setFirstName("New");
        updateUserDto.setLastName("Name");
        updateUserDto.setUsername("newuser");
        updateUserDto.setContactNumber("1234567890");

        // When
        User updatedUser = updateUserMapper.updateEntityFromDto(user, updateUserDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals("New", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertEquals("newuser", updatedUser.getUsername());
        assertEquals("1234567890", updatedUser.getContactNumber());
        // Email should remain unchanged
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

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setFirstName("New"); // Only update firstName

        // When
        User updatedUser = updateUserMapper.updateEntityFromDto(user, updateUserDto);

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
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setFirstName("New");

        // When
        User updatedUser = updateUserMapper.updateEntityFromDto(null, updateUserDto);

        // Then
        assertNull(updatedUser);
    }

    @Test
    void updateEntityFromDto_NullUpdateUserDto_ShouldReturnOriginalUser() {
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
        User updatedUser = updateUserMapper.updateEntityFromDto(user, null);

        // Then
        assertSame(user, updatedUser);
    }

    @Test
    void toDto_ValidUser_ShouldCreateUpdateUserDto() {
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
        UpdateUserDto updateUserDto = updateUserMapper.toDto(user);

        // Then
        assertNotNull(updateUserDto);
        assertEquals("John", updateUserDto.getFirstName());
        assertEquals("Doe", updateUserDto.getLastName());
        assertEquals("johndoe", updateUserDto.getUsername());
        assertEquals("1234567890", updateUserDto.getContactNumber());
    }

    @Test
    void toDto_NullUser_ShouldReturnNull() {
        // When
        UpdateUserDto updateUserDto = updateUserMapper.toDto(null);

        // Then
        assertNull(updateUserDto);
    }

    @Test
    void toChangedFieldsDto_NoChanges_ShouldReturnEmptyDto() {
        // Given
        User originalUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        User updatedUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        // When
        UpdateUserDto updateUserDto = updateUserMapper.toChangedFieldsDto(originalUser, updatedUser);

        // Then
        assertNotNull(updateUserDto);
        assertNull(updateUserDto.getFirstName());
        assertNull(updateUserDto.getLastName());
        assertNull(updateUserDto.getUsername());
        assertNull(updateUserDto.getContactNumber());
    }

    @Test
    void toChangedFieldsDto_SomeChanges_ShouldReturnOnlyChangedFields() {
        // Given
        User originalUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        User updatedUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("Jane") // Changed
                .lastName("Doe") // Unchanged
                .username("janedoe") // Changed
                .contactNumber("1234567890") // Unchanged
                .build();

        // When
        UpdateUserDto updateUserDto = updateUserMapper.toChangedFieldsDto(originalUser, updatedUser);

        // Then
        assertNotNull(updateUserDto);
        assertEquals("Jane", updateUserDto.getFirstName());
        assertNull(updateUserDto.getLastName()); // Should be null as unchanged
        assertEquals("janedoe", updateUserDto.getUsername());
        assertNull(updateUserDto.getContactNumber()); // Should be null as unchanged
    }

    @Test
    void toChangedFieldsDto_AllChanges_ShouldReturnAllFields() {
        // Given
        User originalUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        User updatedUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .username("janesmith")
                .contactNumber("9876543210")
                .build();

        // When
        UpdateUserDto updateUserDto = updateUserMapper.toChangedFieldsDto(originalUser, updatedUser);

        // Then
        assertNotNull(updateUserDto);
        assertEquals("Jane", updateUserDto.getFirstName());
        assertEquals("Smith", updateUserDto.getLastName());
        assertEquals("janesmith", updateUserDto.getUsername());
        assertEquals("9876543210", updateUserDto.getContactNumber());
    }

    @Test
    void toChangedFieldsDto_NullOriginalUser_ShouldReturnNull() {
        // Given
        User updatedUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .username("janesmith")
                .contactNumber("9876543210")
                .build();

        // When
        UpdateUserDto updateUserDto = updateUserMapper.toChangedFieldsDto(null, updatedUser);

        // Then
        assertNull(updateUserDto);
    }

    @Test
    void toChangedFieldsDto_NullUpdatedUser_ShouldReturnNull() {
        // Given
        User originalUser = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        // When
        UpdateUserDto updateUserDto = updateUserMapper.toChangedFieldsDto(originalUser, null);

        // Then
        assertNull(updateUserDto);
    }
}
