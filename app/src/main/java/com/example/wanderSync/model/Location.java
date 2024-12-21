package com.example.wanderSync.model;

public class Location {
    private String documentId;
    private String locationName;

    public Location(String documentId, String locationName) {
        this.documentId = documentId;
        this.locationName = locationName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getLocationName() {
        return locationName;
    }

    @Override
    public String toString() {
        return locationName; // will be displayed in the spinner
    }
}
