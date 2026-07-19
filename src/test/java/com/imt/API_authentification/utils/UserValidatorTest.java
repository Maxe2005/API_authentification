package com.imt.API_authentification.utils;

import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidatorTest {

    @Test
    void validateUsername_acceptsValidUsername() {
        assertDoesNotThrow(() -> UserValidator.validateUsername("valid_user.name-1"));
    }

    @Test
    void validateUsername_rejectsNull() {
        assertThrows(ValidationException.class, () -> UserValidator.validateUsername(null));
    }

    @Test
    void validateUsername_rejectsBlank() {
        assertThrows(ValidationException.class, () -> UserValidator.validateUsername("   "));
    }

    @Test
    void validateUsername_rejectsTooShort() {
        assertThrows(ValidationException.class, () -> UserValidator.validateUsername("ab"));
    }

    @Test
    void validateUsername_rejectsTooLong() {
        assertThrows(ValidationException.class, () -> UserValidator.validateUsername("a".repeat(33)));
    }

    @Test
    void validateUsername_rejectsInvalidCharacters() {
        assertThrows(ValidationException.class, () -> UserValidator.validateUsername("invalid user!"));
    }

    @Test
    void validatePassword_acceptsValidPassword() {
        assertDoesNotThrow(() -> UserValidator.validatePassword("password123"));
    }

    @Test
    void validatePassword_rejectsNull() {
        assertThrows(ValidationException.class, () -> UserValidator.validatePassword(null));
    }

    @Test
    void validatePassword_rejectsBlank() {
        assertThrows(ValidationException.class, () -> UserValidator.validatePassword("   "));
    }

    @Test
    void validatePassword_rejectsTooShort() {
        assertThrows(ValidationException.class, () -> UserValidator.validatePassword("short1"));
    }
}
