package com.example.wanderSync.view.dining;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wanderSync.model.databaseModel.Dining;
import com.example.wanderSync.model.utils.ReservationValidator;
import com.example.wandersync.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class DiningsAdapter extends RecyclerView.Adapter<DiningsAdapter.DiningViewHolder> {
    private static final int VIEW_TYPE_ACTIVE = 0;
    private static final int VIEW_TYPE_EXPIRED = 1;
    private List<Dining> dinings;

    public DiningsAdapter() {
        this.dinings = new ArrayList<>(); // Initialize with an empty list if null
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addLog(Dining reservation) {
        this.dinings.add(0, reservation); // Add new log at the start
        updateExpiredDinings();
        sortDiningsByTimeDescending();
        notifyDataSetChanged(); // Notify the adapter that data has changed
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateLogs(List<Dining> newReservations) {
        this.dinings.clear();
        this.dinings.addAll(newReservations);
        updateExpiredDinings(); // Refresh expired statuses for all items
        sortDiningsByTimeDescending();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_EXPIRED) {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_expired_reservation, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_reservation, parent, false);
        }
        return new DiningViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return dinings.get(position).isExpired() ? VIEW_TYPE_EXPIRED : VIEW_TYPE_ACTIVE;
    }

    @Override
    public void onBindViewHolder(@NonNull DiningViewHolder holder, int position) {
        Dining dining = dinings.get(position);
        holder.location.setText(dining.getLocation());
        holder.resturantName.setText(dining.getName());
        holder.website.setText(dining.getWebsite());
        holder.time.setText(dining.getTime());
    }

    @Override
    public int getItemCount() {
        return dinings != null ? dinings.size() : 0;
    }

    private void updateExpiredDinings() {
        for (Dining dining : dinings) {
            if (!ReservationValidator.isFutureTime(dining.getTime(), "h:mma")) {
                dining.setExpired(true);
            }
        }
    }

    public void setDinings(List<Dining> dinings) {
        if (dinings != null) {
            this.dinings = dinings;
            updateExpiredDinings();
        } else {
            this.dinings = new ArrayList<>(); // Avoid null pointer
        }
        sortDiningsByTimeDescending();
        notifyDataSetChanged();
    }

    // Sorting the reservation list
    public void sortDiningsByTimeDescending() {
        Collections.sort(dinings, (d1, d2) -> d2.getTime().compareTo(d1.getTime()));
        notifyDataSetChanged(); // Notify the adapter of the change
    }

    static class DiningViewHolder extends RecyclerView.ViewHolder {
        private TextView location;
        private TextView resturantName;
        private TextView website;
        private TextView time;

        DiningViewHolder(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.tvLocation);
            resturantName = itemView.findViewById(R.id.tvRestaurantName);
            website = itemView.findViewById(R.id.tvWebsite);
            time = itemView.findViewById(R.id.tvTime);
        }
    }
}
