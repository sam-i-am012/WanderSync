package com.example.wanderSync.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.wandersync.R;
import com.example.wanderSync.viewmodel.CreateAccountViewModel;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Button;

public class CreateAccountActivity extends AppCompatActivity {

    private CreateAccountViewModel createAccountViewModel;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private ProgressBar progressBar;
    private Button createAccountBtn;
    private TextView returnToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize UI components
        editTextEmail = findViewById(R.id.email_createAccount);
        editTextPassword = findViewById(R.id.password_createAccount);
        progressBar = findViewById(R.id.progressBar);
        createAccountBtn = findViewById(R.id.createAccountButton);
        returnToLogin = findViewById(R.id.returnToLogin);

        // Initialize ViewModel
        createAccountViewModel = new ViewModelProvider(this).get(CreateAccountViewModel.class);

        // Observe ViewModel changes
        createAccountViewModel.getCreateAccountResult().observe(this, createAccountResult -> {
            if (createAccountResult.isSuccess()) {
                // Navigate to the next screen
                Toast.makeText(CreateAccountActivity.this,
                        "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),
                        "Account creation failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Create account button click
        createAccountBtn.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validation logic
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(),
                        "Please enter an email address", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(),
                        "Please enter a password", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                createAccountViewModel.createAccount(email, password);
            }
        });

        // return to login page button
        returnToLogin.setOnClickListener(view ->  {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
