package com.nibm.pharmagomadproject.customer.models;
import com.google.firebase.Timestamp;

public class Notification {
    private String    notificationId;
    private String    userId;
    private String    title;
    private String    message;
    private String    type;       // "order", "prescription", "approval", "delivery"
    private String    referenceId; // orderId / complaintId etc
    private boolean   isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(String userId, String title, String message,
                        String type, String referenceId) {
        this.userId      = userId;
        this.title       = title;
        this.message     = message;
        this.type        = type;
        this.referenceId = referenceId;
        this.isRead      = false;
    }

    public String    getNotificationId() { return notificationId; }
    public String    getUserId()         { return userId; }
    public String    getTitle()          { return title; }
    public String    getMessage()        { return message; }
    public String    getType()           { return type; }
    public String    getReferenceId()    { return referenceId; }
    public boolean   isRead()            { return isRead; }
    public Timestamp getCreatedAt()      { return createdAt; }
    public void setNotificationId(String v) { this.notificationId = v; }
    public void setRead(boolean v)          { this.isRead = v; }
    public void setCreatedAt(Timestamp v)   { this.createdAt = v; }

    public String getFormattedTime() {
        if (createdAt == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy · hh:mm a", java.util.Locale.getDefault());
        return sdf.format(createdAt.toDate());
    }
}
