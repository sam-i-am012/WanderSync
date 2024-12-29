
package com.example.wanderSync.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wandersync.R;
import com.example.wanderSync.model.databaseModel.TravelLog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TravelLogAdapter extends RecyclerView.Adapter<TravelLogAdapter.ViewHolder> {

    private List<TravelLog> travelLogs;

    public TravelLogAdapter(List<TravelLog> travelLogs) {
        this.travelLogs = travelLogs;
    }

    private int calculateDays(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        return (int) ChronoUnit.DAYS.between(start, end);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void addLog(TravelLog log) {
        this.travelLogs.add(0, log); // Add new log at the start
        if (this.travelLogs.size() > 5) { // to make sure there's only 5 logs or less
            this.travelLogs.remove(this.travelLogs.size() - 1); // Remove the oldest log
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateLogs(List<TravelLog> newLogs) {
        this.travelLogs.clear();
        this.travelLogs.addAll(newLogs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_travel_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the TravelLog object for this position
        TravelLog log = travelLogs.get(position);
        holder.destinationTextView.setText(log.getDestination());

        // Calculate and display days
        int days = calculateDays(log.getStartDate(), log.getEndDate());
        holder.daysTextView.setText(String.format("%d days planned", days));
    }

    @Override
    public int getItemCount() {
        return travelLogs.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView destinationTextView;
        private TextView daysTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            // Bind the views from the item layout
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            daysTextView = itemView.findViewById(R.id.daysTextView);
        }
    }
}