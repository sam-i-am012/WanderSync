package com.example.wanderSync.model;

import android.annotation.SuppressLint;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class VacationTimeCalculator {

    private static int[] separateDate(String date) {
        String[] parts = date.split("-");

        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        return new int[] {year, month, day};
    }

    @SuppressLint("NewApi")
    private static long getDaysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    @SuppressLint("NewApi")
    private static LocalDate addDays(LocalDate date, long amount) {
        return ChronoUnit.DAYS.addTo(date, amount);
    }

    @SuppressLint("NewApi")
    private static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    @SuppressLint("NewApi")
    public String calculateEntry(String entry1, String entry2) {
        boolean entry1IsDate = entry1.contains("-");
        boolean entry2IsDate = entry2.contains("-");

        if (entry1IsDate && entry2IsDate) {
            // Both entries are dates
            int[] startDateParts = separateDate(entry1);
            int[] endDateParts = separateDate(entry2);
            LocalDate startDate = LocalDate.of(startDateParts[0], startDateParts[1],
                    startDateParts[2]);
            LocalDate endDate = LocalDate.of(endDateParts[0], endDateParts[1], endDateParts[2]);
            return Long.toString(getDaysBetween(startDate, endDate));
        } else if (entry1IsDate) {
            // end date is missing
            int[] endDateParts = separateDate(entry1);
            LocalDate endDate = LocalDate.of(endDateParts[0], endDateParts[1], endDateParts[2]);
            LocalDate addedEndDate = addDays(endDate, Long.parseLong(entry2));
            return formatDate(addedEndDate);
        } else if (entry2IsDate) {
            // start date is missing
            int[] startDateParts = separateDate(entry2);
            LocalDate startDate = LocalDate.of(startDateParts[0], startDateParts[1],
                    startDateParts[2]);
            LocalDate addedStartDate = addDays(startDate, Long.parseLong(entry1) * -1);
            String startDateString = formatDate(addedStartDate);

            Result validStartDate = CalcVacationTimeValidator.validateDateRange(startDateString,
                    entry2);
            if (validStartDate.isSuccess()) {
                return formatDate(addedStartDate);
            } else {
                return null;
            }
        }
        return null;
    }
}
