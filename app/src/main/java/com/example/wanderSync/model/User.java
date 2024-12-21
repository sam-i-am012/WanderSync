package com.example.wanderSync.model;

import java.util.List;

public class User {
    private String email;
    private String userId;
    private String startDate;
    private String endDate;
    private String duration;
    private List<String> associatedDestinations;  // list of destination IDs

    public User() { } // required no arg constructor

    public User(String userId, String email, List<String> associatedDestinations) {
        this.userId = userId;
        this.email = email;
        this.associatedDestinations = associatedDestinations;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<String> getAssociatedDestinations() {
        return associatedDestinations;
    }
    public void setAssociatedDestinations(List<String> associatedDestinations) {
        this.associatedDestinations = associatedDestinations;
    }
}