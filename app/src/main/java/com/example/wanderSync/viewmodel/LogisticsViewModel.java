package com.example.wanderSync.viewmodel;


import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.manager.TravelLogManager;
import com.example.wanderSync.model.databaseModel.Invitation;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.model.utils.TripUtils;
import com.example.wanderSync.model.manager.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LogisticsViewModel extends ViewModel {
    private final FirestoreSingleton firestore;
    private final TravelLogManager travelLogManager = new TravelLogManager();
    private final UserManager userManager = new UserManager();
    private MutableLiveData<List<String>> userLocations = new MutableLiveData<>();
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private MutableLiveData<Integer> plannedDaysLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> allocatedLiveData = new MutableLiveData<>();
    private MutableLiveData<Invitation> invitationLiveData = new MutableLiveData<>();


    public LogisticsViewModel() {
        firestore = FirestoreSingleton.getInstance();
        loadUserLocations();
        loadTripDays();
        loadDuration();
        listenForInvitations(); // listen for new invitations to collab on a trip
    }

    // live data for invitations
    public LiveData<Invitation> getInvitationLiveData() {
        return invitationLiveData;
    }

    public LiveData<List<String>> getUserLocations() {
        return userLocations;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public MutableLiveData<Integer> getPlannedDaysLiveData() {
        return plannedDaysLiveData;
    }

    public MutableLiveData<Integer> getAllocatedLiveData() {
        return allocatedLiveData;
    }



    // load user's associated locations and update the LiveData
    private void loadUserLocations() {
        String currentUserId = firestore.getCurrentUserId();
        travelLogManager.getTravelLogsByUser(currentUserId).observeForever(travelLogs -> {
            List<String> locations = new ArrayList<>();
            for (TravelLog log : travelLogs) {
                locations.add(log.getDestination());
            }
            userLocations.setValue(locations);
        });
    }

    // for planned days
    public void loadTripDays() {
        String currentUserId = firestore.getCurrentUserId();
        // Fetch trip data from Firestore and update LiveData accordingly
        travelLogManager.getTravelLogsByUser(currentUserId).observeForever(travelLogs -> {
            int totalDays = TripUtils.calculateTotalDays(travelLogs);  // use utility
            plannedDaysLiveData.setValue(totalDays);
        });
    }

    // for allocated days
    public void loadDuration() {
        String currentUserId = firestore.getCurrentUserId();
        allocatedLiveData = (MutableLiveData<Integer>) userManager.getDurationForUser(currentUserId);
    }

    // to listen for invitations to accept / deny them
    private void listenForInvitations() {
        String currentUserId = firestore.getCurrentUserId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("invitations")
                .whereEqualTo("invitedUserId", currentUserId)
                .whereEqualTo("status", "pending") // only fetch pending invitations
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error fetching invitations", error);
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Invitation invitation = doc.toObject(Invitation.class);
                            invitation.setInvitationId(doc.getId());  // store the document ID

                            // notify UI about the new invitation
                            invitationLiveData.setValue(invitation);
                        }
                    }
                });
    }

    // accept or reject invitation
    public void updateInvitationStatus(String invitationId, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("invitations")
                .document(invitationId)  // use the invitation ID stored in the Invitation model
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    toastMessage.setValue("Invitation " + status);
                    invitationLiveData.setValue(null);  // stop showing the dialog
                })
                .addOnFailureListener(e -> {
                    toastMessage.setValue("Error updating invitation: " + e.getMessage());
                    Log.d("Firestore", "Failed to update invitation with ID: "
                            + invitationId, e);
                });
    }

    // accept invitation
    public void acceptInvitation(Invitation invitation) {
        updateInvitationStatus(invitation.getInvitationId(), "accepted");
        travelLogManager.addUserToTrip(invitation.getInvitingUserId(),
                invitation.getInvitedUserId(), invitation.getTripLocation());
    }
}