package com.example.sprintproject.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.LoginViewModel;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI components
        editTextEmail = findViewById(R.id.email_login);
        editTextPassword = findViewById(R.id.password_login);
        progressBar = findViewById(R.id.progressBar);
        Button loginBtn = findViewById(R.id.loginButton);
        ImageButton quitButton = findViewById(R.id.exitButton);
        TextView createAccount = findViewById(R.id.accountCreationPage);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            progressBar.setVisibility(View.GONE); // always hide the progress bar
            Toast.makeText(LoginActivity.this, loginResult.getMessage(), Toast.LENGTH_SHORT).show();
            if (loginResult.isSuccess()) {
                startActivity(new Intent(getApplicationContext(), LogisticsActivity.class));
                finish();
            }
        });

        // login button
        loginBtn.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // display progress bar
            progressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(email, password);
        });

        // quit button
        quitButton.setOnClickListener(view -> {
            finish();
            System.exit(0);
        });

        // go to create account screen
        createAccount.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            finish();
        });
    }
}