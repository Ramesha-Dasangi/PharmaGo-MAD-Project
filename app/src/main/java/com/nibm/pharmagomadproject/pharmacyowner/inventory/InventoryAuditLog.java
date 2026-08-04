package com.nibm.pharmagomadproject.pharmacyowner.inventory;

public class InventoryAuditLog {
    private String logId;
    private String action; // ADD, UPDATE, DELETE
    private String medicineId;
    private String medicineName;
    private String details;
    private String performedBy;
    private long timestamp;

    public InventoryAuditLog() {
    }

    public InventoryAuditLog(String logId, String action, String medicineId, 
                              String medicineName, String details, 
                              String performedBy, long timestamp) {
        this.logId = logId;
        this.action = action;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.details = details;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
