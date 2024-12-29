package com.example.wanderSync.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.manager.TravelLogManager;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.model.utils.TravelLogValidator;
import com.example.wanderSync.view.accomodations.AccommodationsActivity;
import com.example.wanderSync.view.dining.DiningEstablishmentsActivity;
import com.example.wanderSync.view.travelCommunity.TravelCommunityActivity;
import com.example.wanderSync.viewmodel.DestinationsViewModel;
import com.example.wandersync.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.wanderSync.model.Result;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DestinationsActivity extends AppCompatActivity {
    private Button calcVacationTimeButton;
    private EditText durationET;
    private EditText startDateET;
    private EditText endDateET;
    private Button calculateButton;
    private Button logTravelButton;
    private View resultLayout;
    private RecyclerView recyclerView;
    private TravelLogAdapter adapter;
    private TravelLogAdapter adapterAll;
    private DestinationsViewModel viewModel;
    private final FirestoreSingleton firestore = FirestoreSingleton.getInstance();
    private final TravelLogManager travelLogManager = new TravelLogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);

        initViews(); // helper method to initialize views

        // populate the database and fetch logs
        travelLogManager.prepopulateDatabase();

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(DestinationsViewModel.class);

        // after auth
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            viewModel.fetchTravelLogsForCurrentUser();
            viewModel.fetchLastFiveTravelLogsForCurrentUser();
        }

        // observe things from the view model
        viewModelObserver(); // helper method to observe all live data from view model
        viewModel.loadTripDays();

        logTravelButton.setOnClickListener(v -> {
            logTravelButtonLogic();
        });


        // --------------- calculate vacation time functionality ----------
        calcVacationTimeButton.setOnClickListener(v -> {
            if (startDateET.getVisibility() == View.GONE) {
                // make dialog elements visible
                for (EditText editText : Arrays.asList(startDateET, endDateET, durationET)) {
                    editText.setVisibility(View.VISIBLE);
                }
                calculateButton.setVisibility(View.VISIBLE);
            } else {
                // Hide dialog elements
                for (EditText editText : Arrays.asList(startDateET, endDateET, durationET)) {
                    editText.setVisibility(View.GONE);
                }
                calculateButton.setVisibility(View.GONE);
                resultLayout.setVisibility(View.GONE);
            }
        });

        calculateButton.setOnClickListener(view -> {
            // helper method to calculate and give a toast message if there's an error
            calculateVacationDaysHelper();
        });

        // logic for navigation buttons
        navButtonsLogic();
    }

    private void logTravelButtonLogic() {
        // Inflate dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_log_travel, null);

        // Initialize views
        EditText travelLocationET = dialogView.findViewById(R.id.dialog_travelLocation);
        EditText estimatedStartET = dialogView.findViewById(R.id.dialog_startDate);
        EditText estimatedEndET = dialogView.findViewById(R.id.dialog_endDate);
        Button submitButton = dialogView.findViewById(R.id.dialog_submitButton);

        // Create AlertDialog
        AlertDialog logTravelDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Submit button logic
        submitButton.setOnClickListener(submitView -> {
            // Create a new TravelLog object and validate it before adding
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                return;
            }

            String userId = user.getUid();
            String destination = travelLocationET.getText().toString().trim();
            String startDate = estimatedStartET.getText().toString().trim();
            String endDate = estimatedEndET.getText().toString().trim();

            // Validation logic
            if (TravelLogValidator.areFieldsEmpty(destination, startDate, endDate)) {
                Toast.makeText(getApplicationContext(), "Please fill in all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (TravelLogValidator.isDateFormatInvalid(startDate)
                    || TravelLogValidator.isDateFormatInvalid(endDate)
                    || TravelLogValidator.calculateDays(startDate, endDate) < 0) {
                Toast.makeText(getApplicationContext(), "Please enter valid dates (MM/DD/YYYY)",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            TravelLog newLog = new TravelLog(userId, destination, startDate, endDate,
                    new ArrayList<>(), new ArrayList<>());

            // Add the new log directly to the adapter and update total days
            adapter.addLog(newLog);

            // Clear the input fields
            travelLocationET.setText("");
            estimatedStartET.setText("");
            estimatedEndET.setText("");

            // Add the log to the ViewModel/database asynchronously
            viewModel.addTravelLog(newLog);

            logTravelDialog.dismiss();
        });

        logTravelDialog.show();
    }

    private void initViews() {
        calcVacationTimeButton = findViewById(R.id.calcVacationTimeButton);

        durationET = findViewById(R.id.durationET);
        calculateButton = findViewById(R.id.calculateButton);
        logTravelButton = findViewById(R.id.logTravelButton);

        startDateET = findViewById(R.id.startDateET);
        endDateET = findViewById(R.id.endDateET);


        // resetButton = findViewById(R.id.resetButton);
        resultLayout = findViewById(R.id.resultLayout);
    }

    private void calculateVacationDaysHelper() {
        String startDate = startDateET.getText().toString();
        String endDate = endDateET.getText().toString();
        String duration = durationET.getText().toString();
        String entry;
        boolean totalSuccess;

        Result missingEntry = viewModel.validateMissingEntry(startDate, endDate, duration);
        if (missingEntry.isSuccess()) {
            totalSuccess = true;
            switch (missingEntry.getMessage()) {
                case "None":
                    Toast.makeText(DestinationsActivity.this,
                            "All entries already populated", Toast.LENGTH_SHORT).show();
                    break;
                case "Start Date":
                    entry = viewModel.calculateMissingEntry(duration, endDate);
                    if (entry != null) {
                        startDateET.setText(entry);
                        startDate = startDateET.getText().toString();
                    } else {
                        totalSuccess = false;
                        Toast.makeText(DestinationsActivity.this,
                                "Start date cannot be in the past",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "End Date":
                    entry = viewModel.calculateMissingEntry(startDate, duration);
                    endDateET.setText(entry);
                    endDate = endDateET.getText().toString();
                    break;
                case "Duration":
                    Result dateRangeValid = viewModel.validateDateRange(startDate, endDate);
                    if (dateRangeValid.isSuccess()) {
                        entry = viewModel.calculateMissingEntry(startDate, endDate);
                        durationET.setText(entry);
                        duration = durationET.getText().toString();
                        break;
                    } else {
                        totalSuccess = false;
                        Toast.makeText(DestinationsActivity.this,
                                dateRangeValid.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    totalSuccess = false;
                    Toast.makeText(DestinationsActivity.this,
                            "Unexpected entry type: " + missingEntry.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            totalSuccess = false;
            Toast.makeText(DestinationsActivity.this,
                    missingEntry.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (totalSuccess) {
            // Add startDate, endDate, and duration to database
            viewModel.addDatesAndDuration(firestore.getCurrentUserId(), startDate, endDate,
                    duration);

        }
    }

    private void navButtonsLogic() {
        ImageButton diningEstablishmentsButton = findViewById(R.id.diningEstablishmentsButton);
        ImageButton accommodationsButton = findViewById(R.id.accommodationsButton);
        ImageButton logisticsButton = findViewById(R.id.logisticsButton);
        ImageButton travelCommunityButton = findViewById(R.id.travelCommunityButton);

        // Handle navigation bar button presses
        diningEstablishmentsButton.setOnClickListener(view -> {
            Intent diningEstablishmentsIntent = new Intent(DestinationsActivity.this,
                    DiningEstablishmentsActivity.class);
            startActivity(diningEstablishmentsIntent);
        });

        accommodationsButton.setOnClickListener(view -> {
            Intent accommodationsIntent = new Intent(DestinationsActivity.this,
                    AccommodationsActivity.class);
            startActivity(accommodationsIntent);
        });

        logisticsButton.setOnClickListener(view -> {
            Intent logisticsIntent = new Intent(DestinationsActivity.this,
                    LogisticsActivity.class);
            startActivity(logisticsIntent);
        });

        travelCommunityButton.setOnClickListener(view -> {
            Intent travelCommunityIntent = new Intent(DestinationsActivity.this,
                    TravelCommunityActivity.class);
            startActivity(travelCommunityIntent);
        });
    }

    private void viewModelObserver() {
        // observe getTravelLogs
        viewModel.getTravelLogs().observe(this, travelLogs -> {
            adapterAll = new TravelLogAdapter(travelLogs);
        });

        // observe getPlannedDaysLiveData
        viewModel.getPlannedDaysLiveData().observe(this, totalDays -> {
            // Update the total days text
            updateTotalDaysText(totalDays);
        });

        // observe getLastFiveTravelLogs which will only show the last five entries
        viewModel.getLastFiveTravelLogs().observe(this, travelLogs -> {
            if (adapter == null) {
                adapter = new TravelLogAdapter(travelLogs);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateLogs(travelLogs); // Add a method to update the adapter data
            }
        });
    }

    private void updateTotalDaysText(int totalDays) {
        TextView resultText = findViewById(R.id.resultText);
        resultText.setText(totalDays + "\n" + "days");
    }

}