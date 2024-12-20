package com.example.sprintproject.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sprintproject.R;
import com.example.sprintproject.model.Post;

public class PostDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        TextView tvDestination = findViewById(R.id.tvDestination);
        TextView tvStartDate = findViewById(R.id.tvStartDate);
        TextView tvEndDate = findViewById(R.id.tvEndDate);
        TextView tvAccommodations = findViewById(R.id.tvAccommodations);
        TextView tvDiningReservations = findViewById(R.id.tvDiningReservations);
        TextView tvNotes = findViewById(R.id.tvNotes);

        Post post = (Post) getIntent().getSerializableExtra("POST_DATA");

        tvDestination.setText("Destination: " + post.getPostDestination());
        tvStartDate.setText("Start date: " + post.getPostStartDate());
        tvEndDate.setText("End date: " + post.getPostEndDate());
        tvAccommodations.setText("Accommodations: " + post.getPostAccommodations());
        tvDiningReservations.setText("Dining reservations: " + post.getDiningReservations());
        tvNotes.setText("Notes: " + post.getPostNotes());

        // Find the root view and set an OnClickListener on it
        View rootView = findViewById(R.id.root_view);  // Replace with your actual root view ID
        rootView.setOnClickListener(v -> {
            // When the screen is clicked, start TravelCommunityActivity
            Intent intent = new Intent(PostDetailsActivity.this, TravelCommunityActivity.class);
            startActivity(intent);
            finish();  // Optionally, finish this activity so that it's removed from the stack
        });
    }
}
