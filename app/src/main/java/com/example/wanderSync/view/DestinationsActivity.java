package com.example.wanderSync.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.model.TravelLogManager;
import com.example.wanderSync.model.databaseModel.TravelLog;
import com.example.wanderSync.model.TravelLogValidator;
import com.example.wanderSync.viewmodel.DestinationsViewModel;
import com.example.wandersync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.wanderSync.model.Result;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

public class DestinationsActivity extends AppCompatActivity {

    private ImageButton diningEstablishmentsButton;
    private ImageButton accommodationsButton;
    private ImageButton logisticsButton;
    private ImageButton travelCommunityButton;
    private Button calcVacationTimeButton;
    private EditText startDateET;
    private EditText endDateET;
    private EditText durationET;
    private Button calculateButton;
    private Button logTravelButton;
    private TextView travelLocationTV;
    private EditText travelLocationET;
    private TextView estimatedStartTV;
    private EditText estimatedStartET;
    private TextView estimatedEndTV;
    private EditText estimatedEndET;
    private Button cancelButton;
    private Button submitButton;
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
        travelLogManager.prepopulateDatabase();

        initViews();
        setupRecyclerView();
        initializeViewModel();
        setupAuthDependentLogic();
        observeViewModel();
        setupTravelLogButtons();
        setupVacationTimeButtons();
        navButtonsLogic(); // helper method for navigation buttons
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(DestinationsViewModel.class);
        viewModel.loadTripDays();
    }

    private void setupAuthDependentLogic() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            viewModel.fetchTravelLogsForCurrentUser();
            viewModel.fetchLastFiveTravelLogsForCurrentUser();
        }
    }

    private void observeViewModel() {
        viewModelObserver(); // Helper method to observe all LiveData from the ViewModel
    }

    private void setupTravelLogButtons() {
        logTravelButton.setOnClickListener(v -> logTravelBtnVisibility());

        cancelButton.setOnClickListener(v -> hideTravelLogInputs());

        submitButton.setOnClickListener(v -> handleSubmitTravelLog());
    }

    private void hideTravelLogInputs() {
        for (TextView textView : Arrays.asList(travelLocationTV, estimatedStartTV, estimatedEndTV)) {
            textView.setVisibility(View.GONE);
        }
        for (EditText editText : Arrays.asList(travelLocationET, estimatedStartET, estimatedEndET)) {
            editText.setVisibility(View.GONE);
        }
        cancelButton.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
    }

    private void handleSubmitTravelLog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        String destination = travelLocationET.getText().toString().trim();
        String startDate = estimatedStartET.getText().toString().trim();
        String endDate = estimatedEndET.getText().toString().trim();

        if (!validateTravelLogFields(destination, startDate, endDate)) return;

        TravelLog newLog = new TravelLog(userId, destination, startDate, endDate,
                new ArrayList<>(), new ArrayList<>());

        adapter.addLog(newLog);
        clearInputFields();
        viewModel.addTravelLog(newLog);
    }

    private boolean validateTravelLogFields(String destination, String startDate, String endDate) {
        if (TravelLogValidator.areFieldsEmpty(destination, startDate, endDate)) {
            Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TravelLogValidator.isDateFormatInvalid(startDate)
                || TravelLogValidator.isDateFormatInvalid(endDate)
                || TravelLogValidator.calculateDays(startDate, endDate) < 0) {
            Toast.makeText(getApplicationContext(), "Please enter valid dates (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setupVacationTimeButtons() {
        calcVacationTimeButton.setOnClickListener(v -> toggleVacationTimeInputs());
        calculateButton.setOnClickListener(view -> calculateVacationDaysHelper());
    }

    private void toggleVacationTimeInputs() {
        boolean isHidden = startDateET.getVisibility() == View.GONE;
        int visibility = isHidden ? View.VISIBLE : View.GONE;

        for (EditText editText : Arrays.asList(startDateET, endDateET, durationET)) {
            editText.setVisibility(visibility);
        }
        calculateButton.setVisibility(visibility);
        if (!isHidden) resultLayout.setVisibility(View.GONE);
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

    private void logTravelBtnVisibility() {
        if (travelLocationTV.getVisibility() == View.GONE) {
            // make dialog elements visible
            for (TextView textView : Arrays.asList(travelLocationTV, estimatedStartTV,
                    estimatedEndTV)) {
                textView.setVisibility(View.VISIBLE);
            }
            for (EditText editText : Arrays.asList(travelLocationET, estimatedStartET,
                    estimatedEndET)) {
                editText.setVisibility(View.VISIBLE);
            }
            cancelButton.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        } else {
            // hide the dialog elements
            for (TextView textView : Arrays.asList(travelLocationTV, estimatedStartTV,
                    estimatedEndTV)) {
                textView.setVisibility(View.GONE);
            }
            for (EditText editText : Arrays.asList(travelLocationET, estimatedStartET,
                    estimatedEndET)) {
                editText.setVisibility(View.GONE);
            }
            cancelButton.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
            // Initially set result layout to not visible
            resultLayout.setVisibility(View.VISIBLE);

        }
    }

    private void navButtonsLogic() {
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
        viewModel.getTravelLogs().observe(this, travelLogs -> adapterAll = new TravelLogAdapter(travelLogs));

        // observe getPlannedDaysLiveData
        viewModel.getPlannedDaysLiveData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer totalDays) {
                // Update the total days text
                updateTotalDaysText(totalDays);
            }
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

    // following is implemented again in travelLog validator to ensure testing is correct
    private boolean isDateFormatInvalid(String date) {
        // Regular expression to match the format YYYY-MM-DD
        String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";

        // Check if the date matches the pattern
        if (!date.matches(datePattern)) {
            return true;
        }

        // Additional validation for valid date values
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        // Check for valid month
        if (month < 1 || month > 12) {
            return true;
        }

        // Check for valid day based on the month
        switch (month) {
        case 1: case 3: case 5: case 7: case 8: case 10: case 12:
            return day < 1 || day > 31;
        case 4: case 6: case 9: case 11:
            return day < 1 || day > 30;
        case 2:
            // Leap year check
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            return day < 1 || day > (isLeapYear ? 29 : 28);
        default:
            return true;
        }
    }

    private void initViews() {
        diningEstablishmentsButton = findViewById(R.id.diningEstablishmentsButton);
        accommodationsButton = findViewById(R.id.accommodationsButton);
        logisticsButton = findViewById(R.id.logisticsButton);
        travelCommunityButton = findViewById(R.id.travelCommunityButton);
        calcVacationTimeButton = findViewById(R.id.calcVacationTimeButton);
        startDateET = findViewById(R.id.startDateET);
        endDateET = findViewById(R.id.endDateET);
        durationET = findViewById(R.id.durationET);
        calculateButton = findViewById(R.id.calculateButton);
        logTravelButton = findViewById(R.id.logTravelButton);
        travelLocationTV = findViewById(R.id.travelLocationsTV);
        travelLocationET = findViewById(R.id.travelLocationsET);
        estimatedStartTV = findViewById(R.id.estimatedStartTV);
        estimatedStartET = findViewById(R.id.estimatedStartET);
        estimatedEndTV = findViewById(R.id.estimatedEndTV);
        estimatedEndET = findViewById(R.id.estimatedEndET);
        cancelButton = findViewById(R.id.cancelButton);
        submitButton = findViewById(R.id.submitButton);
        resultLayout = findViewById(R.id.resultLayout);
    }

    private void clearInputFields() {
        travelLocationET.setText("");
        estimatedStartET.setText("");
        estimatedEndET.setText("");
    }

    @SuppressLint("NewApi")
    private int calculateDays(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        return (int) ChronoUnit.DAYS.between(start, end);
    }
}
