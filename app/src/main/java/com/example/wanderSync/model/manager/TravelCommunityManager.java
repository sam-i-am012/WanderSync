package com.example.wanderSync.model.manager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.databaseModel.travelCommunity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TravelCommunityManager {
    private final FirebaseFirestore firestore;
    private final FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public TravelCommunityManager() {
        firestore = firestoreSingleton.getFirestore();
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
                                .add(travelPost);
                    }
                });
    }

    public LiveData<List<travelCommunity>> getTravelPosts() {
        MutableLiveData<List<travelCommunity>> travelCommunityLiveData = new MutableLiveData<>();

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
