package com.example.smartcampuscompanion;
public class LostItem {
    // These variable names should match the fields you'll use in Firestore
    private String name;
    private String location;
    private String date;
    private String imageUrl; // To store the URL of the uploaded image

    // IMPORTANT: You need a public no-argument constructor for Firestore
    public LostItem() {
    }

    // Constructor with arguments
    public LostItem(String name, String location, String date, String imageUrl) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    // --- Getters and Setters for all fields ---
    // (Firestore needs these to create the object from a database document)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
