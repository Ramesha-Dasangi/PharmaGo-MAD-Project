package com.nibm.pharmagomadproject.pharmacyowner.inventory;

public class StockHistory {
    private String historyId;
    private String medicineId;
    private String medicineName;
    private int oldStock;
    private int newStock;
    private int changeQuantity;
    private String reason;
    private String updatedBy;
    private long timestamp;

    public StockHistory() {
    }

    public StockHistory(String historyId, String medicineId, String medicineName, 
                        int oldStock, int newStock, int changeQuantity, 
                        String reason, String updatedBy, long timestamp) {
        this.historyId = historyId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.oldStock = oldStock;
        this.newStock = newStock;
        this.changeQuantity = changeQuantity;
        this.reason = reason;
        this.updatedBy = updatedBy;
        this.timestamp = timestamp;
    }

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public int getOldStock() { return oldStock; }
    public void setOldStock(int oldStock) { this.oldStock = oldStock; }

    public int getNewStock() { return newStock; }
    public void setNewStock(int newStock) { this.newStock = newStock; }

    public int getChangeQuantity() { return changeQuantity; }
    public void setChangeQuantity(int changeQuantity) { this.changeQuantity = changeQuantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
