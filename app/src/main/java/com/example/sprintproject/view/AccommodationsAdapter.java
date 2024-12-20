package com.example.sprintproject.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sprintproject.R;
import com.example.sprintproject.model.Accommodation;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AccommodationsAdapter extends
        RecyclerView.Adapter<AccommodationsAdapter.AccommodationViewHolder> {

    private List<Accommodation> accommodations;

    public AccommodationsAdapter() {
        this.accommodations = new ArrayList<>(); // Initialize with an empty list if null
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addLog(Accommodation accommodation) {
        this.accommodations.add(0, accommodation); // Add new log at the start
        notifyDataSetChanged(); // Notify the adapter that data has changed
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateLogs(List<Accommodation> newLogs) {
        this.accommodations.clear();
        this.accommodations.addAll(newLogs);
        notifyDataSetChanged(); // Notify that data has been updated
    }

    @NonNull
    @Override
    public AccommodationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accommodation,
                parent, false);
        return new AccommodationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccommodationViewHolder holder, int position) {
        Accommodation accommodation = accommodations.get(position);

        // Set location
        holder.locationText.setText(accommodation.getLocation());

        // Set hotel name
        if (getCurrentDateString().compareTo(accommodation.getCheckOutTime()) < 0) {
            holder.hotelNameText.setText(accommodation.getHotel());
        } else {
            holder.hotelNameText.setText(accommodation.getHotel() + " (Expired)");
        }

        // Set check-in and check-out times
        holder.checkInOutText.setText("Check-in: " + accommodation.getCheckInTime()
                + ", Check-out: " + accommodation.getCheckOutTime());

        // Set number of rooms
        holder.numRoomsText.setText("Number of Rooms: " + accommodation.getNumRooms());

        // Set room type label text (if available)
        if (accommodation.getRoomType() != null && !accommodation.getRoomType().isEmpty()) {
            holder.roomTypeLabel.setText(accommodation.getRoomType());
            holder.roomTypeLabel.setVisibility(View.VISIBLE);
        } else {
            holder.roomTypeLabel.setVisibility(View.GONE); // Hide if no room type is provided
        }
    }

    private static String getCurrentDateString() {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Format the current date to the desired string format
        return currentDate.format(formatter);
    }

    @Override
    public int getItemCount() {
        return accommodations != null ? accommodations.size() : 0;
    }

    public void setAccommodations(List<Accommodation> accommodations) {
        if (accommodations != null) {
            this.accommodations = accommodations;
        } else {
            this.accommodations = new ArrayList<>(); // Avoid null pointer
        }
        notifyDataSetChanged();
    }

    public static class AccommodationViewHolder extends RecyclerView.ViewHolder {
        private TextView locationText;
        private TextView hotelNameText;
        private TextView checkInOutText;
        private TextView numRoomsText;
        private TextView roomTypeLabel;

        public AccommodationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationText = itemView.findViewById(R.id.locationText);
            hotelNameText = itemView.findViewById(R.id.hotelNameText);
            checkInOutText = itemView.findViewById(R.id.checkInOutText);
            numRoomsText = itemView.findViewById(R.id.numRoomsText);
            roomTypeLabel = itemView.findViewById(R.id.roomTypeLabel);
        }
    }
}
