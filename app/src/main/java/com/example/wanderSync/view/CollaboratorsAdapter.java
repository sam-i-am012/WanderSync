package com.example.wanderSync.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.wandersync.R;
import java.util.List;

public class CollaboratorsAdapter extends RecyclerView.Adapter<CollaboratorsAdapter.ViewHolder> {
    private List<String> collaboratorEmails;

    public CollaboratorsAdapter(List<String> collaboratorEmails) {
        this.collaboratorEmails = collaboratorEmails;
    }

    public void updateCollaborators(List<String> newCollaboratorEmails) {
        this.collaboratorEmails = newCollaboratorEmails;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collaborator, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String email = collaboratorEmails.get(position);
        holder.emailTextView.setText(email);
    }

    @Override
    public int getItemCount() {
        return collaboratorEmails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView emailTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.collab_email);
        }
    }
}
