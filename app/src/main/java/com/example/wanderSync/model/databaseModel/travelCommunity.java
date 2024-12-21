package com.example.wanderSync.model.databaseModel;

import java.io.Serializable;

public class travelCommunity implements Serializable {
    private String username;
    private String destination;
    private String startDate;
    private String endDate;
    private String accommodations;
    private String diningReservations;
    private String notes;
    public travelCommunity(String username, String destination, String startDate, String endDate,
                           String accommodations, String diningReservations, String notes) {
        this.username = username;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.accommodations = accommodations;
        this.diningReservations = diningReservations;
        this.notes = notes;
    }

    public travelCommunity() {
    }

    public String getPostUsername() {
        return username;
    }

    public void setPostUsername(String username) {
        this.username = username;
    }

    public String getPostDestination() {
        return destination;
    }

    public void setPostDestination(String destination) {
        this.destination = destination;
    }

    public String getPostStartDate() {
        return startDate;
    }

    public void setPostStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getPostEndDate() {
        return endDate;
    }

    public void setPostEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPostAccommodations() {
        return accommodations;
    }

    public void setPostAccommodations(String accommodations) {
        this.accommodations = accommodations;
    }

    public String getDiningReservations() {
        return diningReservations;
    }

    public void setDiningReservations(String diningReservations) {
        this.diningReservations = diningReservations;
    }

    public String getPostNotes() {
        return notes;
    }

    public void setPostNotes(String notes) {
        this.notes = notes;
    }
}
