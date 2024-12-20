package com.example.sprintproject.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.sprintproject.R;
import com.example.sprintproject.model.Invitation;
import com.example.sprintproject.viewmodel.LogisticsViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.List;

public class LogisticsActivity extends AppCompatActivity {
    private LogisticsViewModel viewModel;
    private PieChart pieChart;
    private int currentPlannedDays = 0;
    private int currentAllocatedDays = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistics);

        // initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LogisticsViewModel.class);


        // Observe  toast message live data
        viewModel.getToastMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(LogisticsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // observe the planned days
        viewModel.getPlannedDaysLiveData().observe(this, plannedDays -> currentPlannedDays = plannedDays);

        // observe allocated days
        viewModel.getAllocatedLiveData().observe(this, allocatedDays -> currentAllocatedDays = allocatedDays);

        // observe the invitation live data
        viewModel.getInvitationLiveData().observe(this, invitation -> {
            if (invitation != null) {
                showInvitationDialog(invitation);
            }
        });

        // getting pie chart button working
        Button viewDataBtn = findViewById(R.id.viewDataButton);
        pieChart = findViewById(R.id.pieChart);
        pieChart.setVisibility(View.GONE);
        viewDataBtn.setOnClickListener(view -> {
            // check if chart is currently visible
            if (pieChart.getVisibility() == View.VISIBLE) {
                pieChart.setVisibility(View.GONE); // hide if visible
            } else {
                // ff not visible, show the pie chart
                visualizeTripDays(currentPlannedDays, currentAllocatedDays);
            }
        });

        // view collaborator/notes button
        Button viewCollabAndNotesBtn = findViewById(R.id.viewCollabsAndNotes);
        viewCollabAndNotesBtn.setOnClickListener(v -> {
            Intent collabAndNotes = new Intent(LogisticsActivity.this,
                    CollabNotesActivity.class);
            startActivity(collabAndNotes);
        });

        navBarButtons(); // helper method below for the nav bar
    }

    private void navBarButtons() {
        ImageButton diningEstablishmentsButton = findViewById(R.id.diningEstablishmentsButton);
        ImageButton destinationsButton = findViewById(R.id.destinationsButton);
        ImageButton accommodationsButton = findViewById(R.id.accommodationsButton);
        ImageButton travelCommunityButton = findViewById(R.id.travelCommunityButton);


        diningEstablishmentsButton.setOnClickListener(view -> {
            Intent diningEstablishmentsIntent = new Intent(LogisticsActivity.this,
                    DiningEstablishmentsActivity.class);
            startActivity(diningEstablishmentsIntent);
        });

        destinationsButton.setOnClickListener(view -> {
            Intent destinationsIntent = new Intent(LogisticsActivity.this,
                    DestinationsActivity.class);
            startActivity(destinationsIntent);
        });

        accommodationsButton.setOnClickListener(view -> {
            Intent accommodationsIntent = new Intent(LogisticsActivity.this,
                    AccommodationsActivity.class);
            startActivity(accommodationsIntent);
        });

        travelCommunityButton.setOnClickListener(view -> {
            Intent travelCommunityIntent = new Intent(LogisticsActivity.this,
                    TravelCommunityActivity.class);
            startActivity(travelCommunityIntent);
        });
    }

    private void visualizeTripDays(int plannedDays, int allottedDays) {
        // Create pie chart entries
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(plannedDays, "Planned Days"));
        entries.add(new PieEntry(allottedDays - plannedDays, "Remaining Allotted Days"));

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "Trip Days");

        // customize pie chart data numbers
        dataSet.setColors(Color.parseColor("#78979c"), Color.parseColor("#b0bdbf"), Color.BLACK);
        dataSet.setSliceSpace(2f);  // Set space between slices
        dataSet.setValueTextSize(24f);  // Set text size for values inside slices
        dataSet.setValueTextColor(Color.BLACK);  // Set value text color


        // Customize pie chart appearance
        pieChart.setHoleRadius(40f);  // Hole in the middle
        pieChart.setTransparentCircleRadius(45f);  // Transparent circle around hole
        pieChart.setCenterText("Trip Days");
        pieChart.setCenterTextSize(16f);  // Text size for center text
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(10f, 0f, 10f, 8f); // to avoid clipping


        // Set label color for each entry (Planned Days, Remaining Allotted Days)
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(15f);

        pieChart.getLegend().setEnabled(false);

        // Animate the chart
        pieChart.animateY(1000);  // Animation for showing chart

        // Create pie data and set it to chart
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();  // Refresh chart

        // Make chart visible
        pieChart.setVisibility(View.VISIBLE);
    }

    private void showInvitationDialog(Invitation invitation) {
        new AlertDialog.Builder(this)
                .setTitle("Trip Invitation")
                .setMessage("You have been invited to a trip to " + invitation.getTripLocation()
                + " with " + invitation.getInvitingUserEmail())
                .setPositiveButton("Accept", (dialog, which) ->
                        viewModel.acceptInvitation(invitation))
                .setNegativeButton("Reject", (dialog, which) ->
                        viewModel.updateInvitationStatus(invitation.getInvitationId(), "rejected"))
                .setCancelable(false) // prevent closing dialog without action
                .show();
    }
}
