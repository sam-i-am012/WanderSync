package com.example.wanderSync.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.wanderSync.model.manager.AuthenticationManager;
import com.example.wanderSync.model.Result;

public class CreateAccountViewModel extends ViewModel {

    private final MutableLiveData<Result> createAccountResult = new MutableLiveData<>();
    private final AuthenticationManager createAccountManager = new AuthenticationManager();

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
