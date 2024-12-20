package com.example.sprintproject.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;
    private List<Post> posts;

    public PostAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addPost(Post post) {
        this.posts.add(0, post); // Add new post at the start
        notifyDataSetChanged(); // Notify the adapter that data has changed
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePosts(List<Post> newPosts) {
        this.posts.clear();
        this.posts.addAll(newPosts);
        notifyDataSetChanged(); // Notify that data has been updated
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.tvUsername.setText(post.getPostUsername());
        holder.tvDestination.setText(post.getPostDestination());
        holder.tvDuration.setText(post.getPostStartDate() + " - " + post.getPostEndDate());

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostDetailsActivity.class);
            intent.putExtra("POST_DATA", post);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public void setPosts(List<Post> posts) {
        if (posts != null) {
            this.posts = posts;
        } else {
            this.posts = new ArrayList<>(); // Avoid null pointer
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername;
        private TextView tvDestination;
        private TextView tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
