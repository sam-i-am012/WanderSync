package com.example.wanderSync.model.utils;

import android.text.TextUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TravelLogValidator {

    private TravelLogValidator() {
    }

    public static boolean areFieldsEmpty(String destination, String startDate, String endDate) {
        return TextUtils.isEmpty(destination) || TextUtils.isEmpty(startDate)
                || TextUtils.isEmpty(endDate);
    }

    // validate date format (YYYY-MM-DD)
    public static boolean isDateFormatInvalid(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(date);
        } catch (ParseException e) {
            return true;
        }
        return false;
    }

    // calc the num of days between start and end dates
    public static int calculateDays(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            long difference = end.getTime() - start.getTime();
            return (int) (difference / (1000 * 60 * 60 * 24)); // convert milliseconds to days
        } catch (ParseException e) {
            return -1; // error in parsing
        }
    }
}