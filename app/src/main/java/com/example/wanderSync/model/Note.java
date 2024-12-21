package com.example.wanderSync.model;

import com.google.firebase.Timestamp;

public class Note {
    private String noteContent;
    private String userId;
    private Timestamp timestamp;

    public Note(String noteContent, String userId) {
        this.noteContent = noteContent;
        this.userId = userId;
        this.timestamp = Timestamp.now(); // auto set timestamp to current time
    }

    public Note() { }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestampMillis() {
        return timestamp != null ? timestamp.getSeconds() * 1000 + timestamp.getNanoseconds()
                / 1000000 : 0;
    }
}
