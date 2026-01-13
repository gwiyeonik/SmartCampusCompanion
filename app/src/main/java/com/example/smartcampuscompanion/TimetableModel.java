package com.example.smartcampuscompanion;

public class TimetableModel {

    private String documentId;

    private String subject;
    private String day;
    private String time;
    private String location;

    // 🔒 GPS (hidden from UI)
    private double latitude;
    private double longitude;

    // 🔹 REQUIRED empty constructor (Firestore)
    public TimetableModel() {}

    // 🔹 Getters & Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // 🔹 GPS (USED INTERNALLY ONLY)
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
