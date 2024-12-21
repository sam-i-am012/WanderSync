package com.example.wanderSync.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wanderSync.model.FirebaseAuthManager;
import com.example.wanderSync.model.InputValidator;
import com.example.wanderSync.model.Result;
import com.example.wanderSync.model.FirestoreSingleton;


public class LoginViewModel extends ViewModel {
    private FirebaseAuthManager firebaseAuthManager;
    private MutableLiveData<Result> loginResult = new MutableLiveData<>();
    private FirestoreSingleton firestoreSingleton = FirestoreSingleton.getInstance();

    public LoginViewModel() {
        firebaseAuthManager = new FirebaseAuthManager();
    }

    public LiveData<Result> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        // validate email and password with InputValidator class
        if (!InputValidator.isValidEmail(email)) {
            loginResult.setValue(new Result(false, "Please enter a valid email address."));
            return;
        }

        if (!InputValidator.isValidPassword(password)) {
            loginResult.setValue(new Result(false, "Please enter a password."));
            return;
        }

        // continue with login
        firebaseAuthManager.login(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // sync associatedDestinations in case destinations were manually removed
                // from database
                String userId = firestoreSingleton.getCurrentUserId();
                firestoreSingleton.syncUserAssociatedDestinationsOnLogin(userId, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        loginResult.setValue(new Result(true, "Login Successful!"));
                    } else {
                        loginResult.setValue(new Result(false, "Failed to update "
                                + "associated destinations."));
                    }
                });
            } else {
                loginResult.setValue(new Result(false, "Login failed: "
                        + task.getException().getMessage()));
            }
        });
    }
}
