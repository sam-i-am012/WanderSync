package com.example.sprintproject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.sprintproject.model.FormValidator;

import org.junit.Test;

import java.time.LocalDate;

public class TravelCommunityFormTests {
    @Test
    public void testValidTravelLogValidCase() {
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 10);
        String destination = "Tokyo";

        assertTrue(FormValidator.isValidTravelLog(destination, startDate, endDate));
    }

    @Test
    public void testValidTravelLogInvalidCases() {
        // Invalid destination
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 10);
        assertFalse(FormValidator.isValidTravelLog("", startDate, endDate));

        // Invalid date range
        LocalDate invalidEndDate = LocalDate.of(2024, 10, 25);
        assertFalse(FormValidator.isValidTravelLog("Paris", startDate, invalidEndDate));

        // Both invalid
        assertFalse(FormValidator.isValidTravelLog("", startDate, invalidEndDate));
    }

    @Test
    public void testValidDateRangeSameStartAndEndDate() {
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 1);
        assertFalse(FormValidator.isValidDateRange(startDate, endDate));
    }

    @Test
    public void testValidDestinationWithLeadingAndTrailingSpaces() {
        String destination = "   London  ";
        assertTrue(FormValidator.isValidDestination(destination));
    }
    
    @Test
    public void testValidDateRangeValidCases() {
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 10);
        assertTrue(FormValidator.isValidDateRange(startDate, endDate));
    }

    @Test
    public void testValidDateRangeInvalidCases() {
        LocalDate startDate = LocalDate.of(2024, 11, 10);
        LocalDate endDate = LocalDate.of(2024, 11, 1);
        assertFalse(FormValidator.isValidDateRange(startDate, endDate));

        assertFalse(FormValidator.isValidDateRange(null, LocalDate.of(2024, 11, 10)));
        assertFalse(FormValidator.isValidDateRange(LocalDate.of(2024, 11, 10), null));
    }

    @Test
    public void testValidDestinationValidCases() {
        assertTrue(FormValidator.isValidDestination("Paris"));
        assertTrue(FormValidator.isValidDestination(" New York "));
    }

    @Test
    public void testValidDestinationInvalidCases() {
        assertFalse(FormValidator.isValidDestination(null));
        assertFalse(FormValidator.isValidDestination(""));
        assertFalse(FormValidator.isValidDestination("   "));
    }
}

