package com.example.wanderSync.model.manager;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TravelLogManager {
    private final FirebaseFirestore firestore;
    private final FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public TravelLogManager() {
        firestore = firestoreSingleton.getFirestore();
    }

    public LiveData<List<TravelLog>> getTravelLogsByUser(String userId) {
        MutableLiveData<List<TravelLog>> travelLogsLiveData = new MutableLiveData<>();
        firestore.collection("travelLogs")
                // use arrayContains to check if userId is in the associatedUsers array
                .whereArrayContains("associatedUsers", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    List<TravelLog> travelLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        TravelLog log = document.toObject(TravelLog.class);
                        travelLogs.add(log);
                    }
                    travelLogsLiveData.setValue(travelLogs);
                });
        return travelLogsLiveData;
    }

    public LiveData<List<TravelLog>> getLastFiveTravelLogsByUser(String userId) {
        MutableLiveData<List<TravelLog>> travelLogsLiveData = new MutableLiveData<>();
        firestore.collection("travelLogs")
                // use arrayContains to check if userId is in the associatedUsers array
                .whereArrayContains("associatedUsers", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)  // creation date, newest first
                .limit(5)  // limit to the last 5 entries
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    List<TravelLog> travelLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        TravelLog log = document.toObject(TravelLog.class);
                        travelLogs.add(log);
                    }
                    travelLogsLiveData.setValue(travelLogs);
                });
        return travelLogsLiveData;
    }

    public void addTravelLog(TravelLog log, OnCompleteListener<DocumentReference> listener) {
        // Automatically set createdAt
        log.setCreatedAt(Timestamp.now());

        // ensure that the creator's userId is added to associatedUserIds
        if (!log.getAssociatedUsers().contains(log.getUserId())) {
            log.addAssociatedUser(log.getUserId());
        }

        firestore.collection("travelLogs")
                .add(log)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String travelLogId = task.getResult().getId();

                        // Update user's associatedDestinations
                        updateUserAssociatedDestinations(log.getUserId(), travelLogId);

                        // set documentId in the TravelLog object
                        String documentId = task.getResult().getId();

                        // update travel log id
                        firestore.collection("travelLogs").document(documentId)
                                .update("documentId", documentId);
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }


    // so initial travel log is not empty
    public void prepopulateDatabase() {
        String userId = firestoreSingleton.getCurrentUserId();
        LiveData<List<TravelLog>> travelLogsLiveData = getTravelLogsByUser(userId);
        travelLogsLiveData.observeForever(new Observer<List<TravelLog>>() {
            @Override
            public void onChanged(List<TravelLog> logs) {
                TravelLog entry1 = new TravelLog(userId, "Paris", "12/01/2023",
                        "12/10/2023", new ArrayList<>(Arrays.asList(userId)), new ArrayList<>());

                TravelLog entry2 = new TravelLog(userId, "New York", "11/15/2023",
                        "11/20/2023", new ArrayList<>(Arrays.asList(userId)), new ArrayList<>());

                if (logs.size() < 2) {
                    addTravelLog(entry1, null);
                    addTravelLog(entry2, null);
                }
                travelLogsLiveData.removeObserver(this);
            }
        });
    }

    public void addUserToTrip(String invitingUserId, String invitedUserId, String location) {
        // find the travel log by location
        firestore.collection("travelLogs")
                .whereEqualTo("destination", location)
                // so people added as a collaborator can also invite other people
                .whereArrayContains("associatedUsers", invitingUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // TODO: change so location is loactionid and not just string name
                        // assuming only one trip with the given location, get the first result
                        String tripId = querySnapshot.getDocuments().get(0).getId();

                        Log.d("Firestore", "Found travel log for location: " + location
                                + ", Trip ID: " + tripId);

                        // add the userId to the associatedUsers array for travel logs
                        firestore.collection("travelLogs")
                                .document(tripId)
                                .update("associatedUsers", FieldValue.arrayUnion(invitedUserId))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "User added to trip successfully!");

                                    // Update user's associatedDestinations array to include trip
                                    updateUserAssociatedDestinations(invitedUserId, tripId);
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error adding user to trip", e));
                    } else {
                        Log.w("Firestore", "No travel log found for location: " + location
                                + " with inviting user " + invitingUserId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error finding travel log by location", e));
    }

    // adds new travel log ID to the asssociatedDestinations array field for specific user
    private void updateUserAssociatedDestinations(String userId, String travelLogId) {
        firestore.collection("users").document(userId)
                .update("associatedDestinations", FieldValue.arrayUnion(travelLogId));
    }
}
