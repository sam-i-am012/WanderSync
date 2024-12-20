package com.example.sprintproject.model;

public class Invitation {
    private String invitingUserId;
    private String invitingUserEmail;
    private String invitedUserId;
    private String tripLocation;
    private String invitationId;
    private String status;  // "pending", "accepted", "rejected"
    private Object timestamp;  // firebase timestamp

    // Getters and Setters
    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    public String getInvitingUserEmail() {
        return invitingUserEmail;
    }

    public void setInvitingUserEmail(String invitingUserEmail) {
        this.invitingUserEmail = invitingUserEmail;
    }

    public String getInvitingUserId() {
        return invitingUserId;
    }
    public void setInvitingUserId(String invitingUserId) {
        this.invitingUserId = invitingUserId;
    }

    public String getInvitedUserId() {
        return invitedUserId;
    }
    public void setInvitedUserId(String invitedUserId) {
        this.invitedUserId = invitedUserId;
    }

    public String getTripLocation() {
        return tripLocation;
    }
    public void setTripLocation(String tripLocation) {
        this.tripLocation = tripLocation;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Object getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
