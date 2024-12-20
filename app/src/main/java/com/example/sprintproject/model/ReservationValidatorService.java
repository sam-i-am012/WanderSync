package com.example.sprintproject.model;

/**
 * util class used for the dining view model to ensure that the entries are valid
 */
public class ReservationValidatorService {
    public Result validate(String name, String time, String location, String website) {
        Result noMissingEntries = ReservationValidator.noMissingEntries(name, time, location,
                website);
        if (noMissingEntries.isSuccess()) {
            Result isValidTime = ReservationValidator.isValidTime(time);
            Result isValidWebsite = ReservationValidator.isValidWebsite(website);
            if (!isValidTime.isSuccess() && !isValidWebsite.isSuccess()) {
                return new Result(true, "Time and website entries are both invalid");
            } else if (!isValidTime.isSuccess()) {
                return isValidTime;
            } else if (!isValidWebsite.isSuccess()) {
                return isValidWebsite;
            } else {
                return new Result(true, "Reservation created successfully!");
            }
        }
        return noMissingEntries;
    }
}
