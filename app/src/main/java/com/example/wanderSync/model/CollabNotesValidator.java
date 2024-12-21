package com.example.wanderSync.model;

public class CollabNotesValidator {

    private CollabNotesValidator() {
    }

    // check if email is valid
    public static boolean isValidEmail(String email) {
        if (email != null) {
            email = email.trim();
            return !email.trim().isEmpty() && email.contains("@")
                    && (email.contains(".com") || email.contains(".edu") || email.contains(".org"));
        }
        return false;
    }
}
