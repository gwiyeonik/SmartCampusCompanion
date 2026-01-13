package com.example.smartcampuscompanion;

public class ScheduledEventModel {

    private String title;
    private String date;
    private String type;
    private String priority;

    public ScheduledEventModel(String title, String date, String type, String priority) {
        this.title = title;
        this.date = date;
        this.type = type;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getPriority() {
        return priority;
    }
}
