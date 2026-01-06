package com.example.smartcampuscompanion;

import java.util.Date; // <-- Import Date

public class HorizontalEventModel {
    private String title;
    private String imageUrl;
    private String eventId;

    // --- ADD THESE DATE FIELDS ---
    private Date date1; // Start date of the event
    private Date date2; // End date of the event (optional)
    // --- END OF ADDITION ---


    public HorizontalEventModel() {} // Required for Firestore

    // Getters and Setters for existing fields
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }


    // --- ADD GETTERS AND SETTERS FOR DATE FIELDS ---
    public Date getDate1() { return date1; }
    public void setDate1(Date date1) { this.date1 = date1; }

    public Date getDate2() { return date2; }
    public void setDate2(Date date2) { this.date2 = date2; }
    // --- END OF ADDITION ---
}
