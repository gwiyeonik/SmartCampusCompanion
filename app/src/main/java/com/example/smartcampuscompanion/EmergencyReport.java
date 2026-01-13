package com.example.smartcampuscompanion;

public class EmergencyReport {

    private String type;
    private String description;
    private String status;
    private String time;
    private String userId;
    private String id;
    // 🔹 GPS
    private double latitude;
    private double longitude;

    // Required empty constructor
    public EmergencyReport() {}

    public EmergencyReport(String type, String description, String status,
                           String time, String userId,
                           double latitude, double longitude) {

        this.type = type;
        this.description = description;
        this.status = status;
        this.time = time;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters & setters
    public String getId() {return id;}
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getTime() { return time; }
    public String getUserId() { return userId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(String id) {this.id = id;}
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
