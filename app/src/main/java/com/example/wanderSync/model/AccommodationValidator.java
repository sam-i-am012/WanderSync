package com.example.wanderSync.model;

public class AccommodationValidator {

    // private constructor to hide the implicit public one
    private AccommodationValidator() {
    }

    public static boolean validateInputs(String checkIn, String checkOut,
                                         String location, String hotel) {
        boolean isValid = true;

        if (checkIn == null || checkIn.isEmpty()) {
            isValid = false;
        }

        if (checkOut == null || checkOut.isEmpty()) {
            isValid = false;
        }

        if (location == null || location.isEmpty()) {
            isValid = false;
        }

        if (hotel == null || hotel.isEmpty()) {
            isValid = false;
        }

        return isValid;
    }
}
