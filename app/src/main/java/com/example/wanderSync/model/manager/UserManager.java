package com.example.wanderSync.model.manager;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.FirestoreSingleton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// for the user database
public class UserManager {
    private final FirebaseFirestore firestore;
    private final FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public UserManager() {
        firestore = firestoreSingleton.getFirestore();
    }

    // Fetch duration (allocated days) for the current user
    public LiveData<Integer> getDurationForUser(String userId) {
        MutableLiveData<Integer> durationLiveData = new MutableLiveData<>();

        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.contains("duration")) {
                            Object durationObj = document.get("duration");
                            int duration = 0;
                            if (durationObj instanceof String) { // should always be the case
                                try {
                                    duration = Integer.parseInt((String) durationObj);
                                } catch (NumberFormatException e) {
                                    duration = 0; // just default if parsing doesn't work
                                }
                            }
                            durationLiveData.setValue(duration);
                        }
                    } else {
                        durationLiveData.setValue(0); // Failure case
                    }
                });

        return durationLiveData;
    }

    // Adds startDate, endDate, and duration to their respective locations in a specific user's
    // database entry
    public void addDatesAndDuration(String userId, String startDate, String endDate,
                                    String duration) {
        // Create a map to hold the fields and their values
        Map<String, Object> updates = new HashMap<>();
        updates.put("startDate", startDate);
        updates.put("endDate", endDate);
        updates.put("duration", duration);

        // Perform the update with all fields at once
        firestore.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid ->
                        // Successfully updated the document
                        Log.d("Firestore", "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e ->
                        // Failed to update the document
                        Log.w("Firestore", "Error updating document", e));
    }
}
