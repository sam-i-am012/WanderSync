package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.sprintproject.model.FirebaseAuthManager;
import com.example.sprintproject.model.Result;

public class CreateAccountViewModel extends ViewModel {

    private final MutableLiveData<Result> createAccountResult = new MutableLiveData<>();
    private final FirebaseAuthManager createAccountManager = new FirebaseAuthManager();

    public LiveData<Result> getCreateAccountResult() {
        return createAccountResult;
    }

    public void createAccount(String email, String password) {
        createAccountManager.createAccount(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createAccountResult.setValue(new Result(true, null));
                    } else {
                        createAccountResult.setValue(new Result(false,
                                task.getException().getMessage()));
                    }
                });
    }
}
