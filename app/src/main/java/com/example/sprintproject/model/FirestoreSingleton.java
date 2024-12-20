package com.example.sprintproject.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

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
import java.util.Set;

public class FirestoreSingleton {
    private static FirestoreSingleton instance;
    private FirebaseFirestore firestore;
    private MutableLiveData<List<Post>> travelCommunityLiveData = new MutableLiveData<>();
    private FirebaseAuth auth;
    private static final String USERS = "users";
    private static final String TRAVEL_LOGS = "travelLogs";
    private static final String FIRESTORES = "Firestore";
    private static final String DURATION = "duration";
    private static final String ASSOCIATED_USERS = "associatedUsers";
    private static final String DOCUMENT_ID = "documentId";
    private static final String USER_ID = "userId";
    private static final String DESTINATION = "destination";
    private static final String DINING = "dining";
    private static final String TRAVEL_COMMUNITY = "travel_community";

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

    public String getCurrentUserId() {
        return auth.getCurrentUser().getUid();
    }


    // Fetch duration (allocated days) for the current user
    public LiveData<Integer> getDurationForUser(String userId) {
        MutableLiveData<Integer> durationLiveData = new MutableLiveData<>();

        firestore.collection(USERS).document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        processDocumentForDuration(task.getResult(), durationLiveData);
                    } else {
                        durationLiveData.setValue(0); // Failure case
                    }
                });

        return durationLiveData;
    }

    private void processDocumentForDuration(DocumentSnapshot document, MutableLiveData<Integer> durationLiveData) {
        if (document.exists() && document.contains(DURATION)) {
            Object durationObj = document.get(DURATION);
            durationLiveData.setValue(parseDuration(durationObj));
        }
    }

    private int parseDuration(Object durationObj) {
        if (durationObj instanceof Number) {
            return ((Number) durationObj).intValue();
        } else if (durationObj instanceof String) {
            return parseDurationString((String) durationObj);
        }
        return 0; // Default value for unsupported types
    }

    private int parseDurationString(String durationStr) {
        try {
            return Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            return 0; // Default for invalid string
        }
    }

    public LiveData<List<TravelLog>> getTravelLogsByUser(String userId) {
        MutableLiveData<List<TravelLog>> travelLogsLiveData = new MutableLiveData<>();
        firestore.collection(TRAVEL_LOGS)
                // use arrayContains to check if userId is in the associatedUsers array
                .whereArrayContains(ASSOCIATED_USERS, userId)
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
        firestore.collection(TRAVEL_LOGS)
                // use arrayContains to check if userId is in the associatedUsers array
                .whereArrayContains(ASSOCIATED_USERS, userId)
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

        firestore.collection(TRAVEL_LOGS)
                .add(log)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String travelLogId = task.getResult().getId();

                        // Update user's associatedDestinations
                        updateUserAssociatedDestinations(log.getUserId(), travelLogId);

                        // set documentId in the TravelLog object
                        String documentId = task.getResult().getId();

                        // update travel log id
                        firestore.collection(TRAVEL_LOGS).document(documentId)
                                .update(DOCUMENT_ID, documentId);
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }


    // adds new travel log ID to the asssociatedDestinations array field for specific user
    private void updateUserAssociatedDestinations(String userId, String travelLogId) {
        firestore.collection(USERS).document(userId)
                .update("associatedDestinations", FieldValue.arrayUnion(travelLogId));
    }

    // synchronizes the associatedDestinations field (will be used at the login screen in case
    // destinations were manually removed from database)
    public void syncUserAssociatedDestinationsOnLogin(String userId,
                                                      OnCompleteListener<Void> onCompleteListener) {
        // get all valid destinations from the travelLogs collection associated with the userId
        firestore.collection(TRAVEL_LOGS)
                .whereEqualTo(USER_ID, userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> validDestinations = new ArrayList<>();

                    // collect all destination IDs from travelLogs that match the user
                    for (DocumentSnapshot destinationDoc : querySnapshot) {
                        validDestinations.add(destinationDoc.getId());
                    }

                    // update the user's associated destinations with only the valid destinations
                    firestore.collection(USERS).document(userId)
                            .update("associatedDestinations", validDestinations)
                            .addOnCompleteListener(onCompleteListener);
                });
    }

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

    public void populateCommunityDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String user1 = "George P. Burdell";
        String user2 = "Buzz";
        LiveData<List<Post>> postsLiveData = getTravelPosts();
        postsLiveData.observeForever(new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                if (posts.size() < 2) {
                    addTravelPost(new Post(user1, "New York",
                            "2023-12-05",
                            "2023-12-15",
                            "Hilton Hotel",
                            "Lombardi's Pizza",
                            "Almost got robbed by a Mickey Mouse"), null);
                    addTravelPost(new Post(user2, "Paris",
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

    // method to check if an email exists in the users collection
    public Task<QuerySnapshot> checkEmailExists(String email) {
        return firestore.collection(USERS)
                .whereEqualTo("email", email)
                .get();
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        firestore.collection(USERS).document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        User user = task.getResult().toObject(User.class);
                        userLiveData.setValue(user);
                    } else {
                        userLiveData.setValue(null); // Handle the case where the user doesn't exist
                    }
                });

        return userLiveData;
    }

    public LiveData<List<User>> getCollaboratorsForLocation(String location, String currentUserId, String documentId) {
        MutableLiveData<List<User>> collaboratorsLiveData = new MutableLiveData<>();

        fetchTravelLogs(location, currentUserId, documentId, collaboratorsLiveData);

        return collaboratorsLiveData;
    }

    private void fetchTravelLogs(String location, String currentUserId, String documentId, MutableLiveData<List<User>> collaboratorsLiveData) {
        firestore.collection(TRAVEL_LOGS)
                .whereEqualTo(DESTINATION, location)
                .whereEqualTo(DOCUMENT_ID, documentId)
                .whereArrayContains(ASSOCIATED_USERS, currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        List<String> userIds = extractUserIds(task.getResult());
                        fetchUsersByIds(userIds, collaboratorsLiveData);
                    } else {
                        collaboratorsLiveData.setValue(new ArrayList<>()); // No matching travel log
                    }
                });
    }

    private List<String> extractUserIds(QuerySnapshot result) {
        Set<String> userIds = new HashSet<>(); // Use Set to automatically remove duplicates
        for (DocumentSnapshot document : result) {
            List<String> associatedUsers = (List<String>) document.get(ASSOCIATED_USERS);
            if (associatedUsers != null) {
                userIds.addAll(associatedUsers);
            }
        }
        return new ArrayList<>(userIds);
    }

    private void fetchUsersByIds(List<String> userIds, MutableLiveData<List<User>> collaboratorsLiveData) {
        if (userIds.isEmpty()) {
            collaboratorsLiveData.setValue(new ArrayList<>()); // No users found
            return;
        }

        firestore.collection(USERS)
                .whereIn(USER_ID, userIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> usersList = new ArrayList<>();
                    for (DocumentSnapshot userDoc : querySnapshot) {
                        User user = userDoc.toObject(User.class);
                        usersList.add(user);
                    }
                    collaboratorsLiveData.setValue(usersList);
                })
                .addOnFailureListener(e -> collaboratorsLiveData.setValue(null));
    }

    public void addUserToTrip(String invitingUserId, String invitedUserId, String location) {
        // find the travel log by location
        firestore.collection(TRAVEL_LOGS)
                .whereEqualTo(DESTINATION, location)
                // so people added as a collaborator can also invite other people
                .whereArrayContains(ASSOCIATED_USERS, invitingUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // assuming only one trip with the given location, get the first result
                        String tripId = querySnapshot.getDocuments().get(0).getId();

                        Log.d(FIRESTORES, "Found travel log for location: " + location
                                + ", Trip ID: " + tripId);

                        // add the userId to the associatedUsers array for travel logs
                        firestore.collection(TRAVEL_LOGS)
                                .document(tripId)
                                .update(ASSOCIATED_USERS, FieldValue.arrayUnion(invitedUserId))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(FIRESTORES, "User added to trip successfully!");

                                    // Update user's associatedDestinations array to include trip
                                    updateUserAssociatedDestinations(invitedUserId, tripId);
                                })
                                .addOnFailureListener(e -> Log.e(FIRESTORES, "Error adding user to trip", e));
                    } else {
                        Log.w(FIRESTORES, "No travel log found for location: " + location
                                + " with inviting user " + invitingUserId);
                    }
                })
                .addOnFailureListener(e -> Log.e(FIRESTORES, "Error finding travel log by location", e));
    }

    // Adds startDate, endDate, and duration to their respective locations in a specific user's
    // database entry
    public void addDatesAndDuration(String userId, String startDate, String endDate,
                                    String duration) {
        // Create a map to hold the fields and their values
        Map<String, Object> updates = new HashMap<>();
        updates.put("startDate", startDate);
        updates.put("endDate", endDate);
        updates.put(DURATION, duration);

        // Perform the update with all fields at once
        firestore.collection(USERS).document(userId).update(updates)
                .addOnSuccessListener(aVoid ->
                    // Successfully updated the document
                    Log.d(FIRESTORES, "DocumentSnapshot successfully updated!"))
                .addOnFailureListener(e ->
                    // Failed to update the document
                    Log.w(FIRESTORES, "Error updating document", e));
    }
    public void addDining(Dining dining, OnCompleteListener<DocumentReference> listener) {
        firestore.collection(DINING)
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

        firestore.collection(DINING)
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
                        Log.e(FIRESTORES, "Error getting dining logs: ", task.getException());
                        diningLogsLiveData.setValue(Collections.emptyList());
                    }
                });
        Log.d(FIRESTORES, "Getting dining log for: " + locationId);
        return diningLogsLiveData;
    }


    public LiveData<List<Dining>> getDiningByUser(String userId) {
        MutableLiveData<List<Dining>> diningLiveData = new MutableLiveData<>();
        firestore.collection(DINING)
                .whereEqualTo(USER_ID, userId) // query logs for this user
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
        firestore.collection(TRAVEL_LOGS)
                .whereEqualTo(DESTINATION, location)
                .whereEqualTo(DOCUMENT_ID, documentId)
                .whereArrayContains(ASSOCIATED_USERS, currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String travelLogId = document.getId();

                        // reference to the travel log document
                        DocumentReference travelLogRef = firestore
                                .collection(TRAVEL_LOGS).document(travelLogId);

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

        firestore.collection(TRAVEL_LOGS)
                .whereEqualTo(DESTINATION, location)
                .whereEqualTo(DOCUMENT_ID, documentId)
                .whereArrayContains(ASSOCIATED_USERS, currentUserId)
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

        firestore.collection(USERS)
                .whereIn(USER_ID, new ArrayList<>(userIds))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, String> userIdToEmailMap = buildUserIdToEmailMap(querySnapshot);
                    List<Note> notes = buildNotesList(notesData, userIdToEmailMap);

                    sortNotesByTimestamp(notes);

                    notesLiveData.setValue(notes);
                })
                .addOnFailureListener(e -> {
                    Log.d(FIRESTORES, "Failed to fetch user emails", e);
                    notesLiveData.setValue(new ArrayList<>());
                });
    }

    private Set<String> extractUserIdsFromNotes(List<Map<String, Object>> notesData) {
        Set<String> userIds = new HashSet<>();
        for (Map<String, Object> noteData : notesData) {
            String userId = (String) noteData.get(USER_ID);
            if (userId != null) {
                userIds.add(userId);
            }
        }
        return userIds;
    }

    private Map<String, String> buildUserIdToEmailMap(QuerySnapshot querySnapshot) {
        Map<String, String> userIdToEmailMap = new HashMap<>();
        for (DocumentSnapshot userDoc : querySnapshot) {
            String userId = userDoc.getString(USER_ID);
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
            String userId = (String) noteData.get(USER_ID);
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
        Log.d(FIRESTORES, "No notes found in the document");
        notesLiveData.setValue(new ArrayList<>());
    }

    private void handleQueryFailure(String location, MutableLiveData<List<Note>> notesLiveData) {
        Log.d(FIRESTORES, "Query failed or no matching travel log found for: " + location);
        notesLiveData.setValue(new ArrayList<>());
    }


    public void addTravelPost(Post travelPost, OnCompleteListener<DocumentReference> listener) {
        // Check for duplicate entry first
        firestore.collection(TRAVEL_COMMUNITY)
                .whereEqualTo("postUsername", travelPost.getPostUsername())
                .whereEqualTo(DESTINATION, travelPost.getPostDestination())
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
                        firestore.collection(TRAVEL_COMMUNITY)
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

    public LiveData<List<Post>> getTravelPosts() {
        //travelCommunityLiveData
        firestore.collection(TRAVEL_COMMUNITY) // query logs
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return; // to avoid null pointer
                    }
                    List<Post> postLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Post log = document.toObject(Post.class);
                        postLogs.add(log);
                    }
                    travelCommunityLiveData.setValue(postLogs);
                });
        return travelCommunityLiveData;
    }
}