package com.example.wanderSync.model.manager;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.databaseModel.Dining;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiningManager {
    private final FirebaseFirestore firestore;
    private final FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public DiningManager() {
        firestore = firestoreSingleton.getFirestore();
    }

    public void addDining(Dining dining, OnCompleteListener<DocumentReference> listener) {
        firestore.collection("dining")
                .add(dining);
    }

    public LiveData<List<Dining>> getDiningLogsByLocation(String locationId) {
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
}