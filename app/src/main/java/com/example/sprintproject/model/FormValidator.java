package com.example.sprintproject.model;

import java.time.LocalDate;


// used for the travel community screen
public class FormValidator {
    private FormValidator() {
    }

    public static boolean isValidDestination(String destination) {
        return destination != null && !destination.trim().isEmpty();
    }

    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }

    public static boolean isValidTravelLog(String destination, LocalDate startDate,
                                           LocalDate endDate) {
        return isValidDestination(destination) && isValidDateRange(startDate, endDate);
    }
}
