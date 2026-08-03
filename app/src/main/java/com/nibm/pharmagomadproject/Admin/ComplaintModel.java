package com.nibm.pharmagomadproject.Admin;

public class ComplaintModel {
    private String complaintId;
    private String orderId;
    private String customerId;
    private String type; // "rider" or "pharmacy"
    private String targetName;
    private String category;
    private String description;
    private String status; // "pending" or "resolved"
    private long createdAt;

    public ComplaintModel() {
        // Empty constructor needed for Firestore
    }

    public ComplaintModel(String complaintId, String orderId, String customerId, String type, String targetName, String category, String description, String status, long createdAt) {
        this.complaintId = complaintId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.type = type;
        this.targetName = targetName;
        this.category = category;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
