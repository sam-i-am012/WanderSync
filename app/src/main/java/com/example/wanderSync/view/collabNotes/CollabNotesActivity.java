package com.example.wanderSync.view.collabNotes;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wanderSync.view.DestinationsActivity;
import com.example.wanderSync.view.dining.DiningEstablishmentsActivity;
import com.example.wanderSync.view.LogisticsActivity;
import com.example.wanderSync.view.travelCommunity.TravelCommunityActivity;
import com.example.wanderSync.view.accomodations.AccommodationsActivity;
import com.example.wandersync.R;
import com.example.wanderSync.model.Location;
import com.example.wanderSync.model.databaseModel.User;
import com.example.wanderSync.viewmodel.CollabNotesViewModel;
import java.util.ArrayList;
import java.util.List;

public class CollabNotesActivity extends AppCompatActivity {
    private CollabNotesViewModel viewModel;
    private CollaboratorsAdapter collaboratorsAdapter;
    private Spinner locationSpinner;
    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;

    private String selectedLocation;
    private String selectedLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collab_notes);

        viewModel = new ViewModelProvider(this).get(CollabNotesViewModel.class);

        // Observe the toast message live data
        viewModel.getToastMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(CollabNotesActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // initialize RecyclerView for notes display
        notesRecyclerView = findViewById(R.id.recyclerViewNotes);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(new ArrayList<>());
        notesRecyclerView.setAdapter(notesAdapter);




        RecyclerView collaboratorsRecyclerView = findViewById(R.id.collaboratorsRecyclerView);
        collaboratorsAdapter = new CollaboratorsAdapter(new ArrayList<>());
        collaboratorsRecyclerView.setAdapter(collaboratorsAdapter);
        collaboratorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        ImageButton diningEstablishmentsButton = findViewById(R.id.diningEstablishmentsButton);
        ImageButton destinationsButton = findViewById(R.id.destinationsButton);
        ImageButton accommodationsButton = findViewById(R.id.accommodationsButton);
        ImageButton travelCommunityButton = findViewById(R.id.travelCommunityButton);
        ImageButton addUsersButton = findViewById(R.id.addUsersBtn);
        ImageButton addNoteBtn = findViewById(R.id.addNoteBtn);
        ImageButton backBtn = findViewById(R.id.back);
        locationSpinner = findViewById(R.id.locationSpinner);

        locationSpinner.setVisibility(View.VISIBLE);

        // populate the spinner with locations after it's made visible
        populateLocationSpinner(locationSpinner);


        // on location that is selected
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Location selected = (Location) parent.getItemAtPosition(position);
                selectedLocation = selected.getLocationName();
                selectedLocationId = selected.getDocumentId();
                fetchCollaboratorsForLocation(selectedLocation, selectedLocationId);

                viewModel.getNotesForTravelLog(selectedLocation, selectedLocationId)
                        .observe(CollabNotesActivity.this, notes -> {
                            if (notes != null) {
                                notesAdapter.updateNotes(notes);
                                Log.d("Notes", "Fetched notes: " + notes.size());
                            } else {
                                notesAdapter.updateNotes(new ArrayList<>());
                                Log.d("Notes", "No notes found.");
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // empty method
            }
        });


        diningEstablishmentsButton.setOnClickListener(view -> {
            Intent diningEstablishmentsIntent = new Intent(CollabNotesActivity.this,
                    DiningEstablishmentsActivity.class);
            startActivity(diningEstablishmentsIntent);
        });

        destinationsButton.setOnClickListener(view -> {
            Intent destinationsIntent = new Intent(CollabNotesActivity.this,
                    DestinationsActivity.class);
            startActivity(destinationsIntent);
        });

        accommodationsButton.setOnClickListener(view -> {
            Intent accommodationsIntent = new Intent(CollabNotesActivity.this,
                    AccommodationsActivity.class);
            startActivity(accommodationsIntent);
        });

        travelCommunityButton.setOnClickListener(view -> {
            Intent travelCommunityIntent = new Intent(CollabNotesActivity.this,
                    TravelCommunityActivity.class);
            startActivity(travelCommunityIntent);
        });

        // Add users button
        addUsersButton.setOnClickListener(view -> showAddUserDialog());

        // add note button
        addNoteBtn.setOnClickListener(view -> showAddNoteDialog());

        // go back button
        backBtn.setOnClickListener(view -> {
            Intent mainActivityLogistics = new Intent(CollabNotesActivity.this,
                    LogisticsActivity.class);
            startActivity(mainActivityLogistics);
        });
    }


    // for the pop up dialog for add note
    private void showAddNoteDialog() {
        final EditText noteEditText = new EditText(this);
        noteEditText.setHint("Enter your note");

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Note")
                .setView(noteEditText)
                .setPositiveButton("Add", (dialog, which) -> {
                    String noteContent = noteEditText.getText().toString().trim();

                    if (!noteContent.isEmpty()) {
                        viewModel.addNoteToTravelLog(selectedLocation, selectedLocationId,
                                noteContent);

                        Toast.makeText(this, "Note sent", Toast.LENGTH_SHORT).show();

                        // manually set the selection back to the previous location
                        int position = locationSpinner.getSelectedItemPosition();
                        locationSpinner.setSelection(position);


                    } else {
                        Toast.makeText(this, "Note cannot be empty",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // for the pop up dialog for add collaborator
    private void showAddUserDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.invite_user_popup, null);

        final EditText emailInput = dialogView.findViewById(R.id.emailInput);
        final Spinner collaboratorSpinner = dialogView.findViewById(R.id.locationSpinner);

        // Observe locations from the ViewModel and populate the spinner
        viewModel.getUserLocations().observe(this, locations -> {
            ArrayAdapter<Location> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, locations);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            collaboratorSpinner.setAdapter(adapter);
        });

        // Build the dialog
        new AlertDialog.Builder(this)
                .setTitle("Invite User")
                .setMessage("Enter the email and select a location:")
                .setView(dialogView)
                .setPositiveButton("Invite", (dialog, whichButton) -> {
                    String email = emailInput.getText().toString();
                    Location collabLocationSpinner = (Location) collaboratorSpinner.getSelectedItem();

                    if (!email.isEmpty() && collabLocationSpinner != null) {
                        // Call ViewModel to handle invitation logic
                        viewModel.inviteUserToTrip(email, collabLocationSpinner.getLocationName());
                    } else {
                        Toast.makeText(CollabNotesActivity.this,
                                "Please enter a valid email and select a location",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void populateLocationSpinner(Spinner locationSpinner) {
        viewModel.getUserLocations().observe(this, locations -> {
            ArrayAdapter<Location> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, locations);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            locationSpinner.setAdapter(adapter);

            // after population, set initial selected location
            if (selectedLocation != null) {
                for (Location location : locations) {
                    if (selectedLocation.equals(location.getLocationName())) {
                        int position = adapter.getPosition(location);
                        locationSpinner.setSelection(position);
                        break;
                    }
                }
            }
        });
    }

    private void fetchCollaboratorsForLocation(String location, String documentId) {
        viewModel.getCollaboratorsForLocation(location, documentId).observe(this, collaborators -> {
            if (collaborators != null && !collaborators.isEmpty()) {
                List<String> collaboratorEmails = new ArrayList<>();
                for (User user : collaborators) {
                    collaboratorEmails.add(user.getEmail());
                }
                collaboratorsAdapter.updateCollaborators(collaboratorEmails);
                Log.d("CollabNotesActivity", "Fetched emails: " + collaboratorEmails);
            } else {
                Log.d("CollabNotesActivity", "No collaborators found for location: " + location);
            }
        });
    }
}