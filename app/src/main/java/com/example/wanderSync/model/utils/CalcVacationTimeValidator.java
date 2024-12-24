package com.example.wanderSync.model.utils;

import com.example.wanderSync.model.Result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CalcVacationTimeValidator {

    // private constructor to hide the implicit public one
    private CalcVacationTimeValidator() {
    }

    public static Result validateMissingEntry(String startDate, String endDate, String duration) {
        int missingCount = 0;
        String missingEntry = "None";
        if (startDate.isEmpty()) {
            missingCount++;
            missingEntry = "Start Date";
        }
        if (endDate.isEmpty()) {
            missingCount++;
            missingEntry = "End Date";
        }
        if (duration.isEmpty()) {
            missingCount++;
            missingEntry = "Duration";
        }

        if (missingCount > 1) {
            return new Result(false, "Only one missing entry permitted");
        } else {
            return new Result(true, missingEntry);
        }
    }

    public static Result validateDateRange(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false); // Ensures strict date parsing

        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            Date today = new Date(); // Current date

            if (start.before(today)) {
                return new Result(false, "Start date cannot be in the past");
            } else if (!start.before(end)) {
                return new Result(false, "Start date must be before end date");
            } else {
                return new Result(true, null);
            }
        } catch (ParseException e) {
            return new Result(false,
                    "Invalid date format, Please use:\nYYYY-MM-DD");
        }
    }
}
