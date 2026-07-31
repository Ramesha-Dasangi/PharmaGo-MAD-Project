package com.nibm.pharmagomadproject.pharmacyowner;

public class NotificationModel {

    private String title;
    private String description;
    private String time;
    private String type;
    private boolean read;
    private String ownerId;
    private long timestamp;

    // Firestore Empty Constructor
    public NotificationModel() {
    }

    public NotificationModel(String title,
                             String description,
                             String time,
                             String type,
                             boolean isRead,
                             String ownerId,
                             long timestamp) {

        this.title = title;
        this.description = description;
        this.time = time;
        this.type = type;
        this.read = isRead;
        this.ownerId = ownerId;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String notificationId;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public boolean isRead(){
        return read;
    }

    public void setRead(boolean read){
        this.read = read;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}