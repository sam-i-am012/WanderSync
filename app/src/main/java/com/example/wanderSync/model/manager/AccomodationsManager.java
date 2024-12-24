package com.example.wanderSync.model.manager;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.databaseModel.Accommodation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccomodationsManager {
    private final FirebaseFirestore firestore;
    private final FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public AccomodationsManager() {
        firestore = firestoreSingleton.getFirestore();
    }

    public void addAccommodation(Accommodation accommodation) {
        firestore.collection("accommodation")
                .add(accommodation);
    }
    public LiveData<List<Accommodation>> getAccommodationLogsByUser(String destinationId) {
        MutableLiveData<List<Accommodation>> accommodationLiveData = new MutableLiveData<>();
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();

        fireStore.collection("accommodation")
                .whereEqualTo("travelDestination", destinationId) // query logs for this user
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return; // to avoid null pointer
                    }
                    Log.d("Accommodation", "fetching accommodation for: " + destinationId);

                    List<Accommodation> accommodationLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Accommodation log = document.toObject(Accommodation.class);
                        accommodationLogs.add(log);
                    }

                    // Sort the logs by date (checkoutTime is in "yyyy-MM-dd" format)
                    accommodationLogs.sort((o1, o2) -> {
                        String date1 = o1.getCheckOutTime();
                        String date2 = o2.getCheckOutTime();

                        // Compare dates as strings (lexicographical order)
                        return date2.compareTo(date1);
                    });

                    accommodationLiveData.setValue(accommodationLogs);
                });

        return accommodationLiveData;
    }
}