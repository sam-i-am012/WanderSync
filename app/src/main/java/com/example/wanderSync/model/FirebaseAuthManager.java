package com.example.wanderSync.model;

import com.example.wanderSync.model.databaseModel.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;


public class FirebaseAuthManager {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore firestore;

    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public Task<AuthResult> login(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        // Get the logged-in user's ID and email
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        String userEmail = mAuth.getCurrentUser().getEmail();

                        // Fetch existing user data to avoid overwriting fields like startDate,
                        // endDate, duration

                        // defined constant instead of duplicating literal 3 times
                        String collectionPath = "users";
                        return firestore.collection(collectionPath).document(userId).get()
                                .continueWithTask(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        // Check if user already exists in Firestore
                                        if (userTask.getResult().exists()) {
                                            // User already exists, just update the email
                                            // if it's different
                                            firestore.collection(collectionPath).
                                                    document(userId).
                                                    update("email", userEmail);
                                        } else {
                                            // User doesn't exist, create a new user with
                                            // default fields
                                            User newUser = new User(userId, userEmail,
                                                    new ArrayList<>());
                                            firestore.collection(collectionPath)
                                                    .document(userId).set(newUser);
                                        }

                                        // Return the original AuthResult wrapped in a Task
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

