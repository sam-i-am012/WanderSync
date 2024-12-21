package com.example.wanderSync.model;
import java.util.List;


public class TripUtils {

    private TripUtils() {
    }

    // utility class for trip day calculation
    public static int calculateTotalDays(List<TravelLog> travelLogs) {
        int totalDays = 0;
        for (TravelLog log : travelLogs) {
            totalDays += TravelLogValidator.calculateDays(log.getStartDate(), log.getEndDate());
        }
        return totalDays;
    }
}
