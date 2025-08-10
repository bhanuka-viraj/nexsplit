package com.nexsplit.util;

import java.util.regex.Pattern;

public class PasswordUtil {

    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
    private static final Pattern HAS_NUMBER = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = HAS_UPPER.matcher(password).find();
        boolean hasLower = HAS_LOWER.matcher(password).find();
        boolean hasNumber = HAS_NUMBER.matcher(password).find();
        boolean hasSpecial = HAS_SPECIAL.matcher(password).find();

        // Require at least 3 out of 4 criteria
        int criteriaMet = 0;
        if (hasUpper)
            criteriaMet++;
        if (hasLower)
            criteriaMet++;
        if (hasNumber)
            criteriaMet++;
        if (hasSpecial)
            criteriaMet++;

        return criteriaMet >= 3;
    }

    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long";
        }

        boolean hasUpper = HAS_UPPER.matcher(password).find();
        boolean hasLower = HAS_LOWER.matcher(password).find();
        boolean hasNumber = HAS_NUMBER.matcher(password).find();
        boolean hasSpecial = HAS_SPECIAL.matcher(password).find();

        StringBuilder message = new StringBuilder();
        if (!hasUpper)
            message.append("Include uppercase letters. ");
        if (!hasLower)
            message.append("Include lowercase letters. ");
        if (!hasNumber)
            message.append("Include numbers. ");
        if (!hasSpecial)
            message.append("Include special characters. ");

        return message.length() > 0 ? message.toString().trim() : "Strong password";
    }
}
