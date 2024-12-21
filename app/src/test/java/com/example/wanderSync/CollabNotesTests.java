package com.example.wanderSync;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.wanderSync.model.CollabNotesValidator;

import org.junit.Test;

// tests for functions needed for collaboration functionality
public class CollabNotesTests {

    @Test
    public void testValidEmailValidInputs() {
        // valid email
        assertTrue(CollabNotesValidator.isValidEmail("test@example.com"));
        assertTrue(CollabNotesValidator.isValidEmail("user.name@domain.com"));
        assertTrue(CollabNotesValidator.isValidEmail("user+tag@domain.com"));
    }

    @Test
    public void testInvalidEmail() {
        // invalid email
        assertFalse(CollabNotesValidator.isValidEmail("plainaddress"));
        assertFalse(CollabNotesValidator.isValidEmail("user@domain,com"));
    }

    @Test
    public void testValidEmailDifferentDomains() {
        // valid email
        assertTrue(CollabNotesValidator.isValidEmail("plainaddress@gmail.com"));
        assertTrue(CollabNotesValidator.isValidEmail("plainaddress@gmail.edu"));
        assertTrue(CollabNotesValidator.isValidEmail("plainaddress@gmail.org"));
        assertFalse(CollabNotesValidator.isValidEmail("plainaddress@gmail.um"));
    }

    @Test
    public void testValidEmailWhitespaceInputs() {
        // email with whitespace
        assertFalse(CollabNotesValidator.isValidEmail("   "));
        assertTrue(CollabNotesValidator.isValidEmail("   user@domain.com"));
        assertTrue(CollabNotesValidator.isValidEmail("user@domain.com   "));
    }

}
