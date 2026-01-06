package com.example.smartcampuscompanion;

public class EventModel {
    private String title;
    private String imageUrl;
    private String eventId;
    private String date1;
    private String date2;
    private String registrationLink; // Matches CreateEventActivity field
    private String googleForm;

    public EventModel() {} // Required for Firestore

    // Getters and Setters
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

    public String getRegistrationLink() { return registrationLink; }
    public void setRegistrationLink(String registrationLink) { this.registrationLink = registrationLink; }

    public String getGoogleForm() { return googleForm; }
    public void setGoogleForm(String googleForm) { this.googleForm = googleForm; }
}