package com.example.smartcampuscompanion;

public class LostFoundItem {
    private String imageUrl;
    private String name;
    private String location;
    private String date;
    private String documentId;
    private boolean isUrgent;
    private String itemType; // "lost" or "found"

    // Firestore requires a no-argument constructor
    public LostFoundItem() {}

    public LostFoundItem(String imageUrl, String name, String location, String date, boolean isUrgent, String itemType) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.location = location;
        this.date = date;
        this.isUrgent = isUrgent;
        this.itemType = itemType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
