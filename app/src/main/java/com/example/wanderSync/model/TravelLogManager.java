package com.example.wanderSync.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    public TravelLogManager() {
        firestore = FirestoreSingleton.getInstance().getFirestore();
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

    // adds new travel log ID to the asssociatedDestinations array field for specific user
    private void updateUserAssociatedDestinations(String userId, String travelLogId) {
        firestore.collection("users").document(userId)
                .update("associatedDestinations", FieldValue.arrayUnion(travelLogId));
    }

    // so initial travel log is not empty
    public void prepopulateDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String userId = user.getUid();
        LiveData<List<TravelLog>> travelLogsLiveData = getTravelLogsByUser(userId);
        travelLogsLiveData.observeForever(new Observer<List<TravelLog>>() {
            @Override
            public void onChanged(List<TravelLog> logs) {
                if (logs.size() < 2) {
                    addTravelLog(new TravelLog(userId, "Paris", "2023-12-01", "2023-12-10",
                            new ArrayList<>(Arrays.asList(userId)), new ArrayList<>()), null);
                    addTravelLog(new TravelLog(userId, "New York", "2023-11-15", "2023-11-20",
                            new ArrayList<>(Arrays.asList(userId)), new ArrayList<>()), null);
                }
                travelLogsLiveData.removeObserver(this);
            }
        });
    }
}
