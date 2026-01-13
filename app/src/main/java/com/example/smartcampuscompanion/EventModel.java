package com.example.smartcampuscompanion;

import java.util.Date;

public class EventModel {
    private String title;
    private String imageUrl;
    private String eventId;
    private String date1; // Stays as String to match Firebase "14 November 2025"
    private String date2;
    private String googleForm;

    // These are for the app's internal logic (Calendar/Filtering)
    private Date date1Obj;
    private Date date2Obj;

    public EventModel() {}

    // --- Standard Getters and Setters ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getDate1() { return date1; }
    public void setDate1(String date1) { this.date1 = date1; }

    public String getDate2() { return date2; }
    public void setDate2(String date2) { this.date2 = date2; }

    public String getGoogleForm() { return googleForm; }
    public void setGoogleForm(String googleForm) { this.googleForm = googleForm; }

    // --- Added for Calendar logic ---
    public Date getDate1Obj() { return date1Obj; }
    public void setDate1Obj(Date date1Obj) { this.date1Obj = date1Obj; }

    public Date getDate2Obj() { return date2Obj; }
    public void setDate2Obj(Date date2Obj) { this.date2Obj = date2Obj; }
}