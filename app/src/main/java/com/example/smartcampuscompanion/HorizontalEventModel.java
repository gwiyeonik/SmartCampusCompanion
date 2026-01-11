package com.example.smartcampuscompanion;

import java.util.Objects;

public class HorizontalEventModel {
    private String title;
    private String imageUrl;
    private String eventId;
    private String date1; // Changed to String to match your Firestore screenshot
    private String date2; // Changed to String to match your Firestore screenshot

    // Required empty constructor for Firestore
    public HorizontalEventModel() {}

    public HorizontalEventModel(String title, String imageUrl, String eventId, String date1, String date2) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.eventId = eventId;
        this.date1 = date1;
        this.date2 = date2;
    }

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

    /**
     * This method is VERY important.
     * It allows list.contains(event) to work by comparing event IDs
     * instead of memory addresses.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HorizontalEventModel that = (HorizontalEventModel) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}