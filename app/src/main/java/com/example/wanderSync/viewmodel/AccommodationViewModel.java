package com.example.wanderSync.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.manager.AccomodationsManager;
import com.example.wanderSync.model.manager.TravelLogManager;
import com.example.wanderSync.model.databaseModel.Accommodation;
import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.Location;
import com.example.wanderSync.model.Result;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.view.accomodations.AccommodationsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AccommodationViewModel extends AndroidViewModel {
    
    private AccommodationsAdapter accommodationsAdapter = new AccommodationsAdapter();
    private final FirestoreSingleton firestoreSingleton;
    private final TravelLogManager travelLogManager = new TravelLogManager();
    private final AccomodationsManager accomodationsManager = new AccomodationsManager();
    private MutableLiveData<List<Accommodation>> accommodationLogs;
    private MutableLiveData<Result> resValidationResult = new MutableLiveData<>();
    private MutableLiveData<List<Location>> userLocations = new MutableLiveData<>();


    public AccommodationViewModel(@NonNull Application application) {
        super(application);
        firestoreSingleton = FirestoreSingleton.getInstance();
        accommodationLogs = new MutableLiveData<>();
        loadUserLocations();
    }

    // Fetch accommodations for the current destination selected
    public void fetchAccommodationLogsForDestination(String travelId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            accomodationsManager.getAccommodationLogsByUser(travelId).observeForever(accommodations -> {
                accommodationLogs.setValue(accommodations);
                Log.d("Accommodation", "travel id: " + travelId);
            });
        }
    }

    // Getter for accommodation logs
    public LiveData<List<Accommodation>> getAccommodationLogs() {
        return accommodationLogs;
    }

    // getter for user locations (used for spinner)
    public LiveData<List<Location>> getUserLocations() {
        return userLocations;
    }

    // Add an accommodation to the repository
    public void addAccommodation(Accommodation accommodation) {
        if (accommodationsAdapter != null) {
            accomodationsManager.addAccommodation(accommodation);
            resValidationResult = new MutableLiveData<>();
        }
    }

    // load user locations (used for spinner)
    private void loadUserLocations() {
        String currentUserId = firestoreSingleton.getCurrentUserId();
        travelLogManager.getTravelLogsByUser(currentUserId).observeForever(travelLogs -> {
            List<Location> locations = new ArrayList<>();
            for (TravelLog log : travelLogs) {
                locations.add(new Location(log.getDocumentId(), log.getDestination()));
            }
            userLocations.setValue(locations);
        });
    }

    public AccommodationsAdapter getAccommodationsAdapter() {
        return accommodationsAdapter;
    }

    public MutableLiveData<Result> getResValidationResult() {
        return resValidationResult;
    }

}
