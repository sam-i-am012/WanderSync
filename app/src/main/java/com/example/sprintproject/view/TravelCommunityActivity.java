package com.example.sprintproject.view;

import android.content.Intent;
import android.os.Bundle;

import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.FirestoreSingleton;
import com.example.sprintproject.viewmodel.CommunityViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TravelCommunityActivity extends AppCompatActivity {

    private CommunityViewModel viewModel;
    private PostAdapter travelPostAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton addTravelPostButton;
    private ImageButton diningEstablishmentsButton;
    private ImageButton destinationsButton;
    private ImageButton accommodationsButton;
    private ImageButton logisticsButton;
    private final FirestoreSingleton firestore = FirestoreSingleton.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_community);

        // prepopulate database
        firestore.populateCommunityDatabase();

        initViews();

        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);

        addTravelPostButton.setOnClickListener(view -> {
            AddPostDialog addPostDialog = new AddPostDialog(
                    TravelCommunityActivity.this, viewModel);
            addPostDialog.show();
        });

        // RecyclerView setup
        recyclerView = findViewById(R.id.travelPostRecyclerView);
        travelPostAdapter = new PostAdapter(this);
        recyclerView.setAdapter(travelPostAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Observe the LiveData for updates to logs
        viewModel.getTravelPostsLiveData().observe(this, posts -> {
            // update adapter when data changes
            if (posts != null) {
                travelPostAdapter.setPosts(posts);
            }
        });

        // Navigation button logic
        navButtonsLogic();
    }

    private void initViews() {
        addTravelPostButton = findViewById(R.id.addPostButton);
        diningEstablishmentsButton = findViewById(R.id.diningEstablishmentsButton);
        destinationsButton = findViewById(R.id.destinationsButton);
        logisticsButton = findViewById(R.id.logisticsButton);
        accommodationsButton = findViewById(R.id.accommodationsButton);
    }

    private void navButtonsLogic() {
        // Handle navigation bar button presses
        diningEstablishmentsButton.setOnClickListener(view -> {
            Intent diningEstablishmentsIntent = new Intent(TravelCommunityActivity.this,
                    DiningEstablishmentsActivity.class);
            startActivity(diningEstablishmentsIntent);
        });

        destinationsButton.setOnClickListener(view -> {
            Intent destinationsIntent = new Intent(TravelCommunityActivity.this,
                    DestinationsActivity.class);
            startActivity(destinationsIntent);
        });

        logisticsButton.setOnClickListener(view -> {
            Intent logisticsIntent = new Intent(TravelCommunityActivity.this,
                    LogisticsActivity.class);
            startActivity(logisticsIntent);
        });

        accommodationsButton.setOnClickListener(view -> {
            Intent accommodationsIntent = new Intent(TravelCommunityActivity.this,
                    AccommodationsActivity.class);
            startActivity(accommodationsIntent);
        });
    }
}