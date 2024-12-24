package com.example.wanderSync.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wanderSync.model.databaseModel.travelCommunity;
import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.Result;
import com.example.wanderSync.model.manager.TravelCommunityManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class CommunityViewModel extends AndroidViewModel {

    private FirestoreSingleton firestoreSingleton;
    private final TravelCommunityManager travelCommunityManager = new TravelCommunityManager();
    private LiveData<List<travelCommunity>> posts;
    private MutableLiveData<Result> resValidationResult = new MutableLiveData<>();

    public CommunityViewModel(@NonNull Application application) {
        super(application);
        firestoreSingleton = FirestoreSingleton.getInstance();

        // Fetch posts from firestore
        posts = travelCommunityManager.getTravelPosts();
    }

    // Returns the LiveData object holding the list of travel posts
    public LiveData<List<travelCommunity>> getTravelPostsLiveData() {
        return posts;
    }

    // Adds a new travel post to the repository
    public void addTravelPost(travelCommunity travelPost, OnCompleteListener<DocumentReference> listener) {
        travelCommunityManager.addTravelPost(travelPost, task -> {
            if (listener != null) {
                listener.onComplete(task);
            }
        });
    }

    public MutableLiveData<Result> getResValidationResult() {
        return resValidationResult;
    }
}
