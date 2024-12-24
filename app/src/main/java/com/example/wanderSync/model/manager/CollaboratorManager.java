package com.example.wanderSync.model.manager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.databaseModel.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollaboratorManager {
    private final FirebaseFirestore firestore;
    private final FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public CollaboratorManager() {
        firestore = firestoreSingleton.getFirestore();
    }

    // method to check if an email exists in the users collection
    public Task<QuerySnapshot> checkEmailExists(String email) {
        return firestore.collection("users")
                .whereEqualTo("email", email)
                .get();
    }

    public LiveData<List<User>> getCollaboratorsForLocation(String location, String currentUserId,
                                                            String documentId) {
        MutableLiveData<List<User>> collaboratorsLiveData = new MutableLiveData<>();
        firestore.collection("travelLogs")
                .whereEqualTo("destination", location)
                .whereEqualTo("documentId", documentId)
                .whereArrayContains("associatedUsers", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        List<String> userIds = extractUserIds(task.getResult());
                        if (userIds.isEmpty()) {
                            collaboratorsLiveData.setValue(new ArrayList<>()); // No users found
                            return;
                        }

                        firestore.collection("users")
                                .whereIn("userId", userIds)
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
                    } else {
                        collaboratorsLiveData.setValue(new ArrayList<>()); // No matching travel log
                    }
                });
        return collaboratorsLiveData;
    }


    private List<String> extractUserIds(QuerySnapshot result) {
        Set<String> userIds = new HashSet<>(); // Use set to auto remove duplicates
        for (DocumentSnapshot document : result) {
            List<String> associatedUsers = (List<String>) document.get("associatedUsers");
            if (associatedUsers != null) {
                userIds.addAll(associatedUsers);
            }
        }
        return new ArrayList<>(userIds);
    }

}
