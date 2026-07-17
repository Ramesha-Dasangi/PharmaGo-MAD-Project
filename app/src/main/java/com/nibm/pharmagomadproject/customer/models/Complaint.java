package com.nibm.pharmagomadproject.customer.models;
import com.google.firebase.Timestamp;

public class Complaint {
    private String    complaintId;
    private String    customerId;
    private String    orderId;
    private String    targetType;    // "pharmacy" or "rider"
    private String    targetId;
    private String    targetName;
    private String    reason;
    private String    description;
    private String    status;        // "pending", "resolved"
    private Timestamp createdAt;

    public Complaint() {}

    public Complaint(String customerId, String orderId, String targetType,
                     String targetId, String targetName,
                     String reason, String description) {
        this.customerId  = customerId;
        this.orderId     = orderId;
        this.targetType  = targetType;
        this.targetId    = targetId;
        this.targetName  = targetName;
        this.reason      = reason;
        this.description = description;
        this.status      = "pending";
    }

    public String    getComplaintId() { return complaintId; }
    public String    getCustomerId()  { return customerId; }
    public String    getOrderId()     { return orderId; }
    public String    getTargetType()  { return targetType; }
    public String    getTargetId()    { return targetId; }
    public String    getTargetName()  { return targetName; }
    public String    getReason()      { return reason; }
    public String    getDescription() { return description; }
    public String    getStatus()      { return status; }
    public Timestamp getCreatedAt()   { return createdAt; }
    public void setComplaintId(String v) { this.complaintId = v;
    }

}
