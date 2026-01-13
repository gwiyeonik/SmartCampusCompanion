package com.example.smartcampuscompanion;

public class TimetableModel {
    private String documentId;
    private String subject;
    private String day;
    private String time;
    private String location;

    // Empty constructor required for Firebase
    public TimetableModel() {}

    public TimetableModel(String documentId, String subject, String day, String time, String location) {
        this.documentId = documentId;
        this.subject = subject;
        this.day = day;
        this.time = time;
        this.location = location;
    }

    // --- GETTERS ---
    public String getDocumentId() { return documentId; }

    // This is the specific method the compiler is complaining about:
    public String getSubject() { return subject; }

    public String getDay() { return day; }
    public String getTime() { return time; }
    public String getLocation() { return location; }

    // --- SETTERS (Required if you are using Firebase's .toObject() method) ---
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDay(String day) { this.day = day; }
    public void setTime(String time) { this.time = time; }
    public void setLocation(String location) { this.location = location; }
}