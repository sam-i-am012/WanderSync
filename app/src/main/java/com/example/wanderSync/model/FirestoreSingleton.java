package com.example.wanderSync.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.wanderSync.model.databaseModel.Accommodation;
import com.example.wanderSync.model.databaseModel.Dining;
import com.example.wanderSync.model.databaseModel.travelCommunity;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.model.databaseModel.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FirestoreSingleton {
    private static FirestoreSingleton instance;
    private FirebaseFirestore firestore;
    private MutableLiveData<List<travelCommunity>> travelCommunityLiveData = new MutableLiveData<>();
    private FirebaseAuth auth;

    private FirestoreSingleton() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirestoreSingleton getInstance() {
        if (instance == null) {
            instance = new FirestoreSingleton();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public String getCurrentUserId() {
        return Objects.requireNonNull(auth.getCurrentUser()).getUid();
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

    // synchronizes the associatedDestinations field (will be used at the login screen in case
    // destinations were manually removed from database)
    public void syncUserAssociatedDestinationsOnLogin(String userId,
                                                      OnCompleteListener<Void> onCompleteListener) {
        // get all valid destinations from the travelLogs collection associated with the userId
        firestore.collection("travelLogs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> validDestinations = new ArrayList<>();

                    // collect all destination IDs from travelLogs that match the user
                    for (DocumentSnapshot destinationDoc : querySnapshot) {
                        validDestinations.add(destinationDoc.getId());
                    }

                    // update the user's associated destinations with only the valid destinations
                    firestore.collection("users").document(userId)
                            .update("associatedDestinations", validDestinations)
                            .addOnCompleteListener(onCompleteListener);
                });
    }

    // adds new travel log ID to the asssociatedDestinations array field for specific user
    public void updateUserAssociatedDestinations(String userId, String travelLogId) {
        firestore.collection("users").document(userId)
                .update("associatedDestinations", FieldValue.arrayUnion(travelLogId));
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

    public void addTravelPost(travelCommunity travelPost, OnCompleteListener<DocumentReference> listener) {
        // Check for duplicate entry first
        firestore.collection("travel_community")
                .whereEqualTo("postUsername", travelPost.getPostUsername())
                .whereEqualTo("destination", travelPost.getPostDestination())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // If a document exists, it's a duplicate, so don't add it
                        if (listener != null) {
                            // Create a custom exception to signal the error
                            Exception duplicateException = new Exception("Duplicate post detected");
                            listener.onComplete(Tasks.forException(duplicateException));
                        }
                    } else {
                        // If no duplicates found, proceed with adding the travel post
                        firestore.collection("travel_community")
                                .add(travelPost)
                                .addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful()) {
                                        String travelPostId = innerTask.getResult().getId();
                                        updateUserAssociatedDestinations(travelPost.
                                                getPostUsername(), travelPostId);
                                    }
                                    if (listener != null) {
                                        listener.onComplete(innerTask);
                                    }
                                });
                    }
                });
    }

    public LiveData<List<travelCommunity>> getTravelPosts() {
        //travelCommunityLiveData
        firestore.collection("travel_community") // query logs
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return; // to avoid null pointer
                    }
                    List<travelCommunity> postLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        travelCommunity log = document.toObject(travelCommunity.class);
                        postLogs.add(log);
                    }
                    travelCommunityLiveData.setValue(postLogs);
                });
        return travelCommunityLiveData;
    }

    public void populateCommunityDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String user1 = "George P. Burdell";
        String user2 = "Buzz";
        LiveData<List<travelCommunity>> postsLiveData = getTravelPosts();
        postsLiveData.observeForever(new Observer<List<travelCommunity>>() {
            @Override
            public void onChanged(List<travelCommunity> posts) {
                if (posts.size() < 2) {
                    addTravelPost(new travelCommunity(user1, "New York",
                            "2023-12-05",
                            "2023-12-15",
                            "Hilton Hotel",
                            "Lombardi's Pizza",
                            "Almost got robbed by a Mickey Mouse"), null);
                    addTravelPost(new travelCommunity(user2, "Paris",
                            "2023-11-25",
                            "2023-12-05",
                            "Paris Hotel",
                            "Café de Flore",
                            "Saw the Eiffel Tower"), null);
                }
                postsLiveData.removeObserver(this);
            }
        });

    }
}