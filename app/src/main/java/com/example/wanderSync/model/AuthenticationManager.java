package com.example.wanderSync.model;

import com.example.wanderSync.model.databaseModel.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Objects;

public class AuthenticationManager {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore firestore;

    public AuthenticationManager() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirestoreSingleton.getInstance().getFirestore();
    }

    public Task<AuthResult> login(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        String userEmail = mAuth.getCurrentUser().getEmail();

                        // Fetch existing user data to avoid overwriting fields (like startDate,
                        // endDate, duration)
                        return firestore.collection("users").document(userId).get()
                                .continueWithTask(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        if (!userTask.getResult().exists()) {
                                            // user doesn't exist, create a new user
                                            User newUser = new User(userId, userEmail, new ArrayList<>());
                                            firestore.collection("users").document(userId).set(newUser);
                                        }

                                        // Return the original AuthResult
                                        return Tasks.forResult(task.getResult());
                                    } else {
                                        throw Objects.requireNonNull(userTask.getException());
                                    }
                                });
                    } else {
                        // if login failed, propagate the error
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }


    public Task<AuthResult> createAccount(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }
}