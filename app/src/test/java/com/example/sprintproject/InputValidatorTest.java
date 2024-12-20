package com.example.sprintproject;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.example.sprintproject.model.InputValidator;

/**
 * Tests to ensure that inputs for account and login creation don't have white space, are not null,
 * and are not empty inputs
 */
public class InputValidatorTest {
    @Test
    public void testIsValidEmailInvalidEmailWithSpaces() {
        assertFalse(InputValidator.isValidEmail("  "));
    }

    @Test
    public void testIsValidPasswordValidPassword() {
        assertTrue(InputValidator.isValidPassword("securepassword123"));
    }

    @Test
    public void testIsValidPasswordEmptyPassword() {
        assertFalse(InputValidator.isValidPassword(""));
    }

    @Test
    public void testIsValidPasswordNullPassword() {
        assertFalse(InputValidator.isValidPassword(null));
    }

    @Test
    public void testIsValidEmailEmptyEmail() {
        assertFalse(InputValidator.isValidEmail(""));
    }

    @Test
    public void testIsValidEmailNullEmail() {
        assertFalse(InputValidator.isValidEmail(null));
    }
}