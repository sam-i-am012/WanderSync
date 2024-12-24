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

    public void addDining(Dining dining, OnCompleteListener<DocumentReference> listener) {
        firestore.collection("dining")
                .add(dining)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String diningId = task.getResult().getId();

                        updateUserAssociatedDestinations(dining.getUserId(), diningId);
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }

    public LiveData<List<Dining>> getDiningLogsByUserAndLocation(String locationId) {
        MutableLiveData<List<Dining>> diningLogsLiveData = new MutableLiveData<>();

        firestore.collection("dining")
                .whereEqualTo("travelDestination", locationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Dining> diningLogs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Dining dining = document.toObject(Dining.class);
                            diningLogs.add(dining);
                        }
                        diningLogsLiveData.setValue(diningLogs);
                    } else {
                        Log.e("Firestore", "Error getting dining logs: ", task.getException());
                        diningLogsLiveData.setValue(Collections.emptyList());
                    }
                });
        Log.d("Firestore", "Getting dining log for: " + locationId);
        return diningLogsLiveData;
    }


    public LiveData<List<Dining>> getDiningByUser(String userId) {
        MutableLiveData<List<Dining>> diningLiveData = new MutableLiveData<>();
        firestore.collection("dining")
                .whereEqualTo("userId", userId) // query logs for this user
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return; // to avoid null pointer
                    }
                    List<Dining> diningLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Dining log = document.toObject(Dining.class);
                        diningLogs.add(log);
                    }
                    diningLiveData.setValue(diningLogs);
                });
        return diningLiveData;
    }

    public void addAccommodation(Accommodation accommodation,
                                 OnCompleteListener<DocumentReference> listener) {
        firestore.collection("accommodation")
                .add(accommodation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String accommodationId = task.getResult().getId();

                        updateUserAssociatedDestinations(accommodation.getUserId(),
                                accommodationId);
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
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
                    Log.d("Accomodatoin", "fetching accomodations for: " + destinationId);

                    List<Accommodation> accommodationLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Accommodation log = document.toObject(Accommodation.class);
                        accommodationLogs.add(log);
                    }

                    // Sort the logs by date (checkoutTime is in "yyyy-MM-dd" format)
                    Collections.sort(accommodationLogs, new Comparator<Accommodation>() {
                        @Override
                        public int compare(Accommodation o1, Accommodation o2) {
                            String date1 = o1.getCheckOutTime();
                            String date2 = o2.getCheckOutTime();

                            // Compare dates as strings (lexicographical order)
                            return date2.compareTo(date1);
                        }
                    });

                    accommodationLiveData.setValue(accommodationLogs);
                });

        return accommodationLiveData;
    }

    public void addNoteToTravelLog(String location, String currentUserId, String documentId,
                                   Note note,
                                   OnCompleteListener<Void> listener) {
        firestore.collection("travelLogs")
                .whereEqualTo("destination", location)
                .whereEqualTo("documentId", documentId)
                .whereArrayContains("associatedUsers", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String travelLogId = document.getId();

                        // reference to the travel log document
                        DocumentReference travelLogRef = firestore
                                .collection("travelLogs").document(travelLogId);

                        // add the new note
                        travelLogRef.update("notes", FieldValue.arrayUnion(note))
                                .addOnCompleteListener(listener);
                    } else {
                        // no travel log match
                        listener.onComplete(Tasks.forResult(null));
                    }
                });
    }

    public LiveData<List<Note>> getNotesForTravelLog(String location, String currentUserId, String documentId) {
        MutableLiveData<List<Note>> notesLiveData = new MutableLiveData<>();

        firestore.collection("travelLogs")
                .whereEqualTo("destination", location)
                .whereEqualTo("documentId", documentId)
                .whereArrayContains("associatedUsers", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        List<Map<String, Object>> notesData = (List<Map<String, Object>>) document.get("notes");

                        if (notesData != null && !notesData.isEmpty()) {
                            fetchUserEmailsForNotes(notesData, notesLiveData);
                        } else {
                            handleEmptyNotes(notesLiveData);
                        }
                    } else {
                        handleQueryFailure(location, notesLiveData);
                    }
                });

        return notesLiveData;
    }

    private void fetchUserEmailsForNotes(List<Map<String, Object>> notesData, MutableLiveData<List<Note>> notesLiveData) {
        Set<String> userIds = extractUserIdsFromNotes(notesData);

        firestore.collection("users")
                .whereIn("userId", new ArrayList<>(userIds))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, String> userIdToEmailMap = buildUserIdToEmailMap(querySnapshot);
                    List<Note> notes = buildNotesList(notesData, userIdToEmailMap);

                    sortNotesByTimestamp(notes);

                    notesLiveData.setValue(notes);
                })
                .addOnFailureListener(e -> {
                    Log.d("Firestore", "Failed to fetch user emails", e);
                    notesLiveData.setValue(new ArrayList<>());
                });
    }

    private Set<String> extractUserIdsFromNotes(List<Map<String, Object>> notesData) {
        Set<String> userIds = new HashSet<>();
        for (Map<String, Object> noteData : notesData) {
            String userId = (String) noteData.get("userId");
            if (userId != null) {
                userIds.add(userId);
            }
        }
        return userIds;
    }

    private Map<String, String> buildUserIdToEmailMap(QuerySnapshot querySnapshot) {
        Map<String, String> userIdToEmailMap = new HashMap<>();
        for (DocumentSnapshot userDoc : querySnapshot) {
            String userId = userDoc.getString("userId");
            String email = userDoc.getString("email");
            if (userId != null && email != null) {
                userIdToEmailMap.put(userId, email);
            }
        }
        return userIdToEmailMap;
    }

    private List<Note> buildNotesList(List<Map<String, Object>> notesData, Map<String, String> userIdToEmailMap) {
        List<Note> notes = new ArrayList<>();
        for (Map<String, Object> noteData : notesData) {
            String noteContent = (String) noteData.get("noteContent");
            String userId = (String) noteData.get("userId");
            String email = userIdToEmailMap.get(userId);

            if (email != null) {
                Note note = new Note(noteContent, email);
                notes.add(note);
            }
        }
        return notes;
    }

    private void sortNotesByTimestamp(List<Note> notes) {
        Collections.sort(notes, (note1, note2) -> Long.compare(note2.getTimestampMillis(), note1.getTimestampMillis()));
    }

    private void handleEmptyNotes(MutableLiveData<List<Note>> notesLiveData) {
        Log.d("Firestore", "No notes found in the document");
        notesLiveData.setValue(new ArrayList<>());
    }

    private void handleQueryFailure(String location, MutableLiveData<List<Note>> notesLiveData) {
        Log.d("Firestore", "Query failed or no matching travel log found for: " + location);
        notesLiveData.setValue(new ArrayList<>());
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