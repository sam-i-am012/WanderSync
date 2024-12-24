package com.example.wanderSync.view;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.example.wandersync.R;
import com.example.wanderSync.model.databaseModel.Dining;
import com.example.wanderSync.model.FirestoreSingleton;
import com.example.wanderSync.viewmodel.DiningViewModel;

import java.util.List;

public class AddReservationDialog extends Dialog {
    private DiningViewModel diningViewModel;
    private LifecycleOwner lifecycleOwner;
    private TextView location;
    private TextView restaurantName;
    private TextView time;
    private TextView website;
    private final FirestoreSingleton firestore = FirestoreSingleton.getInstance();
    private final DiningsAdapter diningsAdapter = new DiningsAdapter();
    private String selectedLocation;

    public AddReservationDialog(Context context, DiningViewModel diningViewModel,
                                String selectedLocation) {
        super(context);  // Calls Dialog constructor
        this.diningViewModel = diningViewModel;
        this.lifecycleOwner = (LifecycleOwner) context;
        this.selectedLocation = selectedLocation;
    }

    @Override
    public void show() {
        super.show();  // Calls the show method of the Dialog class to display the dialog
        setContentView(R.layout.dialog_add_reservation);

        // Set background drawable
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_bg);

        // Find views
        EditText nameET = findViewById(R.id.etName);
        EditText timeET = findViewById(R.id.etTime);
        EditText locationET = findViewById(R.id.etLocation);
        EditText websiteET = findViewById(R.id.etWebsite);
        Button addReservationButton = findViewById(R.id.btnAddReservationDialog);

        // Handle add reservation button click
        addReservationButton.setOnClickListener(view -> {
            String name = nameET.getText().toString().trim();
            String timeEntry = timeET.getText().toString().trim();
            String locationEntry = locationET.getText().toString().trim();
            String websiteEntry = websiteET.getText().toString().trim();


            diningViewModel.validateNewReservation(name, timeEntry, locationEntry, websiteEntry);

            // Observe reservation result
            diningViewModel.getResValidationResult().observe(lifecycleOwner, result -> {
                Toast.makeText(getContext(), result.getMessage(),
                        Toast.LENGTH_SHORT).show();
                if (result.isSuccess()) {
                    Dining dining = new Dining(locationEntry, websiteEntry, name, timeEntry,
                            firestore.getCurrentUserId(), selectedLocation);

                    // Add reservation to database
                    diningViewModel.addDining(dining);

                    // Add reservation to recycler
                    diningViewModel.addLog(dining);

                    // Update recycler when reservation is added
                    diningViewModel.getDiningLogs().observe(lifecycleOwner,
                            new Observer<List<Dining>>() {
                                @Override
                                public void onChanged(List<Dining> dinings) {
                                    diningsAdapter.updateLogs(dinings); // Update w/ the new list
                                }
                            });
                    clearInputFields();
                }
                diningViewModel.resetResult();
            });
            dismiss();
        });

    }

    private void clearInputFields() {
        if (location != null) {
            location.setText("");
        }
        if (restaurantName != null) {
            restaurantName.setText("");
        }
        if (time != null) {
            time.setText("");
        }
        if (website != null) {
            website.setText("");
        }
    }
}

