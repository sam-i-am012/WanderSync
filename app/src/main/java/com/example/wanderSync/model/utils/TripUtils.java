package com.example.wanderSync.model.utils;
import com.example.wanderSync.model.databaseModel.TravelLog;

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
