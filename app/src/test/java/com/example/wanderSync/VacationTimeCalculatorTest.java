package com.example.wanderSync;

import com.example.wanderSync.model.VacationTimeCalculator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class VacationTimeCalculatorTest {
    private String entry1 = "2024-01-01";
    private String entry2 = "2024-01-07";

    private VacationTimeCalculator calculator;

    @Before
    public void setUp() {
        calculator = new VacationTimeCalculator();
    }

    @Test
    public void testCalculateEntryBothDates() {
        String result = calculator.calculateEntry(entry1, entry2);
        assertEquals("6", result);  // 6 days between Jan 1 and Jan 7
    }

    @Test
    public void testCalculateEntryEndDateMissing() {
        entry2 = "5";            // Add 5 days
        String result = calculator.calculateEntry(entry1, entry2);
        assertEquals("2024-01-06", result);  // Should return Jan 6
    }

    @Test
    public void testCalculateEntryStartDateMissing() {
        entry1 = "5";            // Subtract 5 days
        String result = calculator.calculateEntry(entry1, entry2);
        assertNull(result);
    }

    @Test
    public void testCalculateEntryInvalidDateFormat() {
        entry1 = "invalid-date";  // Invalid date
        assertThrows(NumberFormatException.class, () ->
            calculator.calculateEntry(entry1, entry2));
    }

    @Test
    public void testCalculateEntryOneDateAndInvalid() {
        entry2 = "not-a-number";  // Invalid addition
        assertThrows(NumberFormatException.class, () ->
            calculator.calculateEntry(entry1, entry2));
    }
}