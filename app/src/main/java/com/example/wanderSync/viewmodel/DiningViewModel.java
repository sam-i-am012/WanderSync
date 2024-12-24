package com.example.wanderSync.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.Location;
import com.example.wanderSync.model.ReservationValidator;
import com.example.wanderSync.model.manager.TravelLogManager;
import com.example.wanderSync.model.databaseModel.Dining;
import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.Result;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.view.DiningsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class DiningViewModel extends AndroidViewModel {
    private DiningsAdapter diningAdapter = new DiningsAdapter();

    private MutableLiveData<List<Location>> userLocations = new MutableLiveData<>();
    private FirestoreSingleton repository;
    private final TravelLogManager travelLogManager = new TravelLogManager();
    private MutableLiveData<List<Dining>> diningLogs;
    private MutableLiveData<List<Dining>> diningLogsByLocation;
    private MutableLiveData<Result> resValidationResult = new MutableLiveData<>();


    public DiningViewModel(@NonNull Application application) {
        super(application);
        repository = FirestoreSingleton.getInstance();
        diningLogs = new MutableLiveData<>();
        diningLogsByLocation = new MutableLiveData<>();
        loadUserLocations();
    }

    public LiveData<List<Location>> getUserLocations() {
        return userLocations;
    }

    // load user's associated locations and update the LiveData

    private void loadUserLocations() {
        String currentUserId = repository.getCurrentUserId();
        travelLogManager.getTravelLogsByUser(currentUserId).observeForever(travelLogs -> {
            List<Location> locations = new ArrayList<>();
            for (TravelLog log : travelLogs) {
                locations.add(new Location(log.getDocumentId(), log.getDestination()));
            }
            userLocations.setValue(locations);
        });
    }


    public void fetchDiningLogsForCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            repository.getDiningByUser(userId).observeForever(dinings ->
                // Update LiveData with fetched reservations
                diningLogs.setValue(dinings));
        }
    }

    public void fetchDiningLogsForLocation(String locationId) {
        Log.d("Dining", "fetching dining logs for location: " + locationId);
        repository.getDiningLogsByUserAndLocation(locationId).observeForever(dinings ->
            // update LiveData with fetched reservations
            diningLogsByLocation.setValue(dinings));
    }

    public LiveData<List<Dining>> getDiningLogs() {
        return diningLogs;
    }

    public LiveData<List<Dining>> getDiningLogsByLocation() {
        return diningLogsByLocation;
    }

    public void addDining(Dining dining) {
        if (diningAdapter != null) {
            repository.addDining(dining, null);
            resValidationResult = new MutableLiveData<>();

            fetchDiningLogsForLocation(dining.getTravelDestination());
            Log.d("DINING", "travel id: " + dining.getTravelDestination());
        }
    }

    public void addLog(Dining dining) {
        diningAdapter.addLog(dining);
    }

    public LiveData<Result> getResValidationResult() {
        return resValidationResult;
    }

    public void validateNewReservation(String name, String time, String location, String website) {
        Result finalResult;
        Result noMissingEntries = ReservationValidator.noMissingEntries(name, time,
                location, website);
        if (noMissingEntries.isSuccess()) {
            Result isValidTime = ReservationValidator.isValidTime(time);
            Result isValidWebsite = ReservationValidator.isValidWebsite(website);
            if (!isValidTime.isSuccess() && !isValidWebsite.isSuccess()) {
                finalResult = new Result(true, "Time and website entries are both invalid");
            } else if (!isValidTime.isSuccess()) {
                finalResult = isValidTime;
            } else if (!isValidWebsite.isSuccess()) {
                finalResult = isValidWebsite;
            } else {
                finalResult = new Result(true, "Reservation created successfully!");
            }
        } else {
            finalResult = noMissingEntries;
        }
        resValidationResult.setValue(finalResult);
    }

    public void resetResult() {
        resValidationResult = new MutableLiveData<>();
    }
}
