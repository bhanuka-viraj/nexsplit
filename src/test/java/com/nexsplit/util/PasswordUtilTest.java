package com.nexsplit.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void isStrongPassword_StrongPassword_ShouldReturnTrue() {
        // Given
        String password = "StrongPass123!";

        // When
        boolean result = PasswordUtil.isStrongPassword(password);

        // Then
        assertTrue(result);
    }

    @Test
    void isStrongPassword_AnotherStrongPassword_ShouldReturnTrue() {
        // Given
        String password = "MySecureP@ssw0rd";

        // When
        boolean result = PasswordUtil.isStrongPassword(password);

        // Then
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "weak",
            "short",
            "12345678",
            "abcdefgh",
            "ABCDEFGH",
            "!@#$%^&*"
    })
    void isStrongPassword_WeakPasswords_ShouldReturnFalse(String password) {
        // When
        boolean result = PasswordUtil.isStrongPassword(password);

        // Then
        assertFalse(result, "Password should be weak: " + password);
    }

    @Test
    void isStrongPassword_NullPassword_ShouldReturnFalse() {
        // When
        boolean result = PasswordUtil.isStrongPassword(null);

        // Then
        assertFalse(result);
    }

    @Test
    void isStrongPassword_EmptyPassword_ShouldReturnFalse() {
        // When
        boolean result = PasswordUtil.isStrongPassword("");

        // Then
        assertFalse(result);
    }

    @Test
    void isStrongPassword_ShortPassword_ShouldReturnFalse() {
        // Given
        String password = "Short1!";

        // When
        boolean result = PasswordUtil.isStrongPassword(password);

        // Then
        assertFalse(result);
    }

    @Test
    void getPasswordStrengthMessage_StrongPassword_ShouldReturnStrongMessage() {
        // Given
        String password = "StrongPass123!";

        // When
        String result = PasswordUtil.getPasswordStrengthMessage(password);

        // Then
        assertEquals("Strong password", result);
    }

    @Test
    void getPasswordStrengthMessage_ShortPassword_ShouldReturnLengthMessage() {
        // Given
        String password = "short";

        // When
        String result = PasswordUtil.getPasswordStrengthMessage(password);

        // Then
        assertEquals("Password must be at least 8 characters long", result);
    }

    @Test
    void getPasswordStrengthMessage_NoUppercase_ShouldReturnUppercaseMessage() {
        // Given
        String password = "lowercase123!";

        // When
        String result = PasswordUtil.getPasswordStrengthMessage(password);

        // Then
        assertTrue(result.contains("uppercase"));
    }

    @Test
    void getPasswordStrengthMessage_NoLowercase_ShouldReturnLowercaseMessage() {
        // Given
        String password = "UPPERCASE123!";

        // When
        String result = PasswordUtil.getPasswordStrengthMessage(password);

        // Then
        assertTrue(result.contains("lowercase"));
    }

    @Test
    void getPasswordStrengthMessage_NoNumbers_ShouldReturnNumbersMessage() {
        // Given
        String password = "NoNumbers!@";

        // When
        String result = PasswordUtil.getPasswordStrengthMessage(password);

        // Then
        assertTrue(result.contains("numbers"));
    }

    @Test
    void getPasswordStrengthMessage_NoSpecialChars_ShouldReturnSpecialCharsMessage() {
        // Given
        String password = "NoSpecial123";

        // When
        String result = PasswordUtil.getPasswordStrengthMessage(password);

        // Then
        assertTrue(result.contains("special characters"));
    }

    @Test
    void getPasswordStrengthMessage_NullPassword_ShouldReturnLengthMessage() {
        // When
        String result = PasswordUtil.getPasswordStrengthMessage(null);

        // Then
        assertEquals("Password must be at least 8 characters long", result);
    }

    @Test
    void getPasswordStrengthMessage_MultipleIssues_ShouldReturnMultipleMessages() {
        // Given
        String password = "weakpassword"; // 12 characters but missing uppercase, numbers, and special chars

        // When
        String result = PasswordUtil.getPasswordStrengthMessage(password);

        // Then
        assertTrue(result.contains("uppercase"));
        assertTrue(result.contains("numbers"));
        assertTrue(result.contains("special characters"));
    }
}
