package com.example.sprintproject.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.FirestoreSingleton;
import com.example.sprintproject.model.Result;
import com.example.sprintproject.model.TravelLog;
import com.example.sprintproject.model.TravelLogValidator;
import com.example.sprintproject.model.VacationTimeCalculator;
import com.example.sprintproject.model.CalcVacationTimeValidator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DestinationsViewModel extends ViewModel {
    private FirestoreSingleton repository;
    private LiveData<List<TravelLog>> travelLogs;
    private LiveData<List<TravelLog>> lastFivetravelLogs;
    private VacationTimeCalculator vtCalculator = new VacationTimeCalculator();
    private MutableLiveData<Integer> plannedDaysLiveData = new MutableLiveData<>();

    public DestinationsViewModel() {
        repository = FirestoreSingleton.getInstance();
    }

    public void fetchTravelLogsForCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String userId = user.getUid();
        travelLogs = repository.getTravelLogsByUser(userId);
    }

    public LiveData<List<TravelLog>> getTravelLogs() {
        return travelLogs;
    }

    // for only getting the last five entries
    public void fetchLastFiveTravelLogsForCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String userId = user.getUid();
        Log.d("Firestore", "Fetching travel logs for user: " + userId);
        lastFivetravelLogs = repository.getLastFiveTravelLogsByUser(userId);
    }

    public LiveData<List<TravelLog>> getLastFiveTravelLogs() {
        return lastFivetravelLogs;
    }

    public void addTravelLog(TravelLog log) {
        repository.addTravelLog(log, null);
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
        repository.addDatesAndDuration(userId, startDate, endDate, duration);
    }

    public void loadTripDays() {
        String currentUserId = repository.getCurrentUserId();
        // Fetch trip data from Firestore and update LiveData accordingly
        repository.getTravelLogsByUser(currentUserId).observeForever(logs -> {
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