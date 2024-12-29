package com.example.wanderSync.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.Result;
import com.example.wanderSync.model.manager.TravelLogManager;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.model.utils.TravelLogValidator;
import com.example.wanderSync.model.utils.VacationTimeCalculator;
import com.example.wanderSync.model.utils.CalcVacationTimeValidator;
import com.example.wanderSync.model.manager.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DestinationsViewModel extends ViewModel {
    private final FirestoreSingleton repository;
    private final TravelLogManager travelLogManager = new TravelLogManager();
    private  final UserManager userManager = new UserManager();
    private LiveData<List<TravelLog>> travelLogs;
    private LiveData<List<TravelLog>> lastFivetravelLogs;
    private VacationTimeCalculator vtCalculator = new VacationTimeCalculator();
    private MutableLiveData<Integer> plannedDaysLiveData = new MutableLiveData<>();

    public DestinationsViewModel() {
        repository = FirestoreSingleton.getInstance();
    }


    public void fetchTravelLogsForCurrentUser() {
        String userId = FirestoreSingleton.getInstance().getCurrentUserId();
        travelLogs = travelLogManager.getTravelLogsByUser(userId);
    }

    public LiveData<List<TravelLog>> getTravelLogs() {
        return travelLogs;
    }

    // for only getting the last five entries
    public void fetchLastFiveTravelLogsForCurrentUser() {
        String userId = FirestoreSingleton.getInstance().getCurrentUserId();
        Log.d("Firestore", "Fetching travel logs for user: " + userId);
        lastFivetravelLogs = travelLogManager.getLastFiveTravelLogsByUser(userId);
    }

    public LiveData<List<TravelLog>> getLastFiveTravelLogs() {
        return lastFivetravelLogs;
    }

    public void addTravelLog(TravelLog log) {
        travelLogManager.addTravelLog(log, null);
    }

    public Result validateMissingEntry(String startDate, String endDate, String duration) {
        return CalcVacationTimeValidator.validateMissingEntry(startDate, endDate, duration);
    }

    public Result validateDateRange(String startDate, String endDate) {
        return CalcVacationTimeValidator.validateDateRange(startDate, endDate);
    }

    public String calculateMissingEntry(String entry1, String entry2) {
        return vtCalculator.calculateEntry(entry1, entry2);
    }

    public void addDatesAndDuration(String userId, String startDate, String endDate,
                                    String duration) {
        userManager.addDatesAndDuration(userId, startDate, endDate, duration);
    }

    public void loadTripDays() {
        String currentUserId = repository.getCurrentUserId();
        // Fetch trip data from Firestore and update LiveData accordingly
        travelLogManager.getTravelLogsByUser(currentUserId).observeForever(logs -> {
            int totalDays = 0;
            for (TravelLog log : logs) {
                totalDays += TravelLogValidator.calculateDays(log.getStartDate(), log.getEndDate());
            }
            plannedDaysLiveData.setValue(totalDays);
        });
    }

    public MutableLiveData<Integer> getPlannedDaysLiveData() {
        return plannedDaysLiveData;
    }
}