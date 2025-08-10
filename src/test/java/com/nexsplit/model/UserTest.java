package com.nexsplit.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void builder_ShouldCreateUserWithDefaultValues() {
        // When
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .contactNumber("1234567890")
                .build();

        // Then
        assertNotNull(user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("1234567890", user.getContactNumber());
        assertEquals(0, user.getLastValidationCode());
        assertFalse(user.getIsEmailValidate());
        assertFalse(user.getIsGoogleAuth());
        assertEquals(User.Status.ACTIVE, user.getStatus());
        assertNull(user.getDeletedAt());
    }

    @Test
    void getFullName_WithFirstNameAndLastName_ShouldReturnFullName() {
        // Given
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        // When
        String fullName = user.getFullName();

        // Then
        assertEquals("John Doe", fullName);
    }

    @Test
    void getFullName_WithOnlyFirstName_ShouldReturnFirstName() {
        // Given
        User user = User.builder()
                .firstName("John")
                .build();

        // When
        String fullName = user.getFullName();

        // Then
        assertEquals("John", fullName);
    }

    @Test
    void getFullName_WithOnlyLastName_ShouldReturnLastName() {
        // Given
        User user = User.builder()
                .lastName("Doe")
                .build();

        // When
        String fullName = user.getFullName();

        // Then
        assertEquals("Doe", fullName);
    }

    @Test
    void getFullName_WithNoNames_ShouldReturnUsername() {
        // Given
        User user = User.builder()
                .username("johndoe")
                .build();

        // When
        String fullName = user.getFullName();

        // Then
        assertEquals("johndoe", fullName);
    }

    @Test
    void setFullName_WithSpace_ShouldSetFirstNameAndLastName() {
        // Given
        User user = User.builder().build();

        // When
        user.setFullName("John Doe");

        // Then
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
    }

    @Test
    void setFullName_WithoutSpace_ShouldSetOnlyFirstName() {
        // Given
        User user = User.builder().build();

        // When
        user.setFullName("John");

        // Then
        assertEquals("John", user.getFirstName());
        assertNull(user.getLastName());
    }

    @Test
    void setFullName_WithNull_ShouldHandleGracefully() {
        // Given
        User user = User.builder().build();

        // When
        user.setFullName(null);

        // Then
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
    }

    @Test
    void isActive_WithActiveStatusAndNoDeletedAt_ShouldReturnTrue() {
        // Given
        User user = User.builder()
                .status(User.Status.ACTIVE)
                .deletedAt(null)
                .build();

        // When
        boolean isActive = user.isActive();

        // Then
        assertTrue(isActive);
    }

    @Test
    void isActive_WithInactiveStatus_ShouldReturnFalse() {
        // Given
        User user = User.builder()
                .status(User.Status.INACTIVE)
                .deletedAt(null)
                .build();

        // When
        boolean isActive = user.isActive();

        // Then
        assertFalse(isActive);
    }

    @Test
    void isActive_WithDeletedAt_ShouldReturnFalse() {
        // Given
        User user = User.builder()
                .status(User.Status.ACTIVE)
                .deletedAt(LocalDateTime.now())
                .build();

        // When
        boolean isActive = user.isActive();

        // Then
        assertFalse(isActive);
    }

    @Test
    void softDelete_ShouldSetInactiveStatusAndDeletedAt() {
        // Given
        User user = User.builder()
                .status(User.Status.ACTIVE)
                .deletedAt(null)
                .build();

        // When
        user.softDelete();

        // Then
        assertEquals(User.Status.INACTIVE, user.getStatus());
        assertNotNull(user.getDeletedAt());
    }

    @Test
    void softDelete_ShouldSetCurrentTimestamp() {
        // Given
        User user = User.builder().build();
        LocalDateTime beforeDelete = LocalDateTime.now();

        // When
        user.softDelete();

        // Then
        assertNotNull(user.getDeletedAt());
        assertTrue(user.getDeletedAt().isAfter(beforeDelete) || user.getDeletedAt().isEqual(beforeDelete));
    }

    @Test
    void equals_WithSameId_ShouldReturnTrue() {
        // Given
        String id = UUID.randomUUID().toString();
        User user1 = User.builder().id(id).build();
        User user2 = User.builder().id(id).build();

        // When & Then
        assertEquals(user1, user2);
    }

    @Test
    void equals_WithDifferentId_ShouldReturnFalse() {
        // Given
        User user1 = User.builder().id(UUID.randomUUID().toString()).build();
        User user2 = User.builder().id(UUID.randomUUID().toString()).build();

        // When & Then
        assertNotEquals(user1, user2);
    }

    @Test
    void hashCode_WithSameId_ShouldReturnSameHashCode() {
        // Given
        String id = UUID.randomUUID().toString();
        User user1 = User.builder().id(id).build();
        User user2 = User.builder().id(id).build();

        // When & Then
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void toString_ShouldContainUserInformation() {
        // Given
        User user = User.builder()
                .id("test-id")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .build();

        // When
        String toString = user.toString();

        // Then
        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertTrue(toString.contains("johndoe"));
    }
}
