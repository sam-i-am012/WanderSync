package com.example.wanderSync.viewmodel;

import static com.example.wanderSync.model.InputValidator.isValidEmail;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.Location;
import com.example.wanderSync.model.Note;
import com.example.wanderSync.model.TravelLogManager;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.model.databaseModel.User;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollabNotesViewModel extends ViewModel {
    private final FirestoreSingleton firestore;
    private final TravelLogManager travelLogManager;
    private MutableLiveData<List<Location>> userLocations = new MutableLiveData<>();
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private MutableLiveData<List<Note>> notesLiveData = new MutableLiveData<>();  // display notes


    public CollabNotesViewModel() {
        firestore = FirestoreSingleton.getInstance();
        travelLogManager = new TravelLogManager();
        loadUserLocations();
        notesLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Location>> getUserLocations() {
        return userLocations;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }


    // fetch notes for a specific location and current user
    public LiveData<List<Note>> getNotesForTravelLog(String location, String locationId) {
        String currentUserId = firestore.getCurrentUserId();
        firestore.getNotesForTravelLog(location, currentUserId,
                locationId).observeForever(notes ->
                    // update the live data
                    notesLiveData.setValue(notes));
        return notesLiveData;
    }

    // load user's associated locations and update the LiveData
    private void loadUserLocations() {
        String currentUserId = firestore.getCurrentUserId();
        travelLogManager.getTravelLogsByUser(currentUserId).observeForever(travelLogs -> {
            List<Location> locations = new ArrayList<>();
            for (TravelLog log : travelLogs) {
                locations.add(new Location(log.getDocumentId(), log.getDestination()));
            }
            userLocations.setValue(locations);
        });
    }


    // invite a user to the trip after validating their email
    public void inviteUserToTrip(String email, String location) {
        if (!isValidEmail(email)) {
            toastMessage.setValue("Please enter a valid email address.");
            return;
        }

        // check if user invited is already an existing user
        firestore.checkEmailExists(email).addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                // uid of invited user
                String invitedUser = task.getResult().getDocuments().get(0).getId();

                // data for Invitation class
                Map<String, Object> invitationData = new HashMap<>();
                // current user (inviter)
                invitationData.put("invitingUserId", firestore.getCurrentUserId());
                invitationData.put("invitedUserId", invitedUser);  // user being invited
                invitationData.put("invitingUserEmail", email);  // inviter's email
                invitationData.put("tripLocation", location);  // location for the trip
                invitationData.put("status", "pending");  // initial status of the invitation
                invitationData.put("timestamp", FieldValue.serverTimestamp());  // timestamp


                // add the invitation to Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("invitations")
                        .add(invitationData)
                        .addOnSuccessListener(documentReference ->
                            toastMessage.setValue("Invitation sent to " + email + " for location "
                                    + location))
                        .addOnFailureListener(e ->
                            toastMessage.setValue("Error sending invitation: " + e.getMessage()));
            } else {
                toastMessage.setValue("No account found for this email.");
            }
        });
    }

    public LiveData<List<User>> getCollaboratorsForLocation(String location, String documentId) {
        return firestore.getCollaboratorsForLocation(location,
                firestore.getCurrentUserId(), documentId);
    }

    public void addNoteToTravelLog(String location, String locationId, String noteContent) {
        String userId = firestore.getCurrentUserId();

        Note newNote = new Note(noteContent, userId);

        firestore.addNoteToTravelLog(location, userId, locationId, newNote, task -> {
            if (task.isSuccessful()) {
                toastMessage.setValue("Note added successfully!");
            } else {
                toastMessage.setValue("Failed to add note.");
            }
        });
    }
}