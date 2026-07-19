package com.imt.API_authentification.utils;

import jakarta.xml.bind.ValidationException;

import java.util.regex.Pattern;

public final class UserValidator {

    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 32;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final int PASSWORD_MIN_LENGTH = 8;

    private UserValidator() {
    }

    public static void validateUsername(String username) throws ValidationException {
        if (username == null || username.isBlank()) throw new ValidationException("Empty username");
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            throw new ValidationException(
                    "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException("Username may only contain letters, digits, dots, underscores and dashes");
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.isBlank()) throw new ValidationException("Empty password");
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new ValidationException("Password must be at least " + PASSWORD_MIN_LENGTH + " characters");
        }
    }
}
