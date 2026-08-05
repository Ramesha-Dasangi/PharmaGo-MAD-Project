package com.nibm.pharmagomadproject.customer.models;

import java.util.Map;
import com.google.firebase.Timestamp;
import java.util.List;

public class Order {
    private String    orderId;
    private String    customerId;
    private String    pharmacyId;
    private String    pharmacyName;
    private String    riderId;
    private List<Map<String,Object>> items;
    private double    total;
    private String    status;       // pending, processing, picked_up, out_for_delivery, delivered, cancelled
    private String    paymentMethod;
    private String    address;
    private Timestamp createdAt;

    public Order() {}

    public String    getOrderId()      { return orderId; }
    public String    getCustomerId()   { return customerId; }
    public String    getPharmacyId()   { return pharmacyId; }
    public String    getPharmacyName() { return pharmacyName; }
    public String    getPharmacyNamesDisplay() {
        if (items != null && !items.isEmpty()) {
            java.util.Set<String> set = new java.util.LinkedHashSet<>();
            for (Map<String, Object> item : items) {
                if (item != null) {
                    Object pName = item.get("pharmacyName");
                    if (pName != null && !pName.toString().trim().isEmpty()) {
                        set.add(pName.toString().trim());
                    }
                }
            }
            if (!set.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String name : set) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(name);
                }
                return sb.toString();
            }
        }
        return pharmacyName != null && !pharmacyName.isEmpty() ? pharmacyName : "Pharmacy";
    }
    public String    getRiderId()      { return riderId; }
    public List<Map<String,Object>> getItems() { return items; }
    public double    getTotal()        { return total; }
    public String    getStatus()       { return status; }
    public String    getPaymentMethod(){ return paymentMethod; }
    public String    getAddress()      { return address; }
    public Timestamp getCreatedAt()    { return createdAt; }

    public void setOrderId(String v)      { this.orderId = v; }
    public void setStatus(String v)       { this.status = v; }
    public void setPharmacyName(String v) { this.pharmacyName = v; }
    public void setRiderId(String v)      { this.riderId = v; }
    public void setTotal(double v)        { this.total = v; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
    public void setItems(List<Map<String,Object>> v) { this.items = v; }

    // Status display helper
    public String getStatusDisplay() {
        if (status == null) return "Pending";
        switch (status) {
            case "pending":                   return "Order confirmed";
            case "awaiting_approval":          return "Awaiting approval";
            case "approved_pending_payment":  return "Approved (Pay Now)";
            case "partially_approved":        return "Partially approved";
            case "partially_rejected":        return "Partially rejected";
            case "processing":                return "Processing";
            case "picked_up":                 return "Picked up";
            case "out_for_delivery":          return "Out for delivery";
            case "delivered":                 return "Delivered";
            case "cancelled":                 return "Cancelled";
            case "rejected":                  return "Rejected";
            default:                          return status;
        }
    }

    // Status tag color helper
    public int getStatusColorRes() {
        if (status == null) return android.R.color.darker_gray;
        switch (status) {
            case "out_for_delivery":         return com.nibm.pharmagomadproject.R.color.pg_primary;
            case "delivered":                return com.nibm.pharmagomadproject.R.color.pg_primary;
            case "cancelled":                return com.nibm.pharmagomadproject.R.color.pg_accent;
            case "rejected":                 return com.nibm.pharmagomadproject.R.color.pg_accent;
            case "approved_pending_payment": return com.nibm.pharmagomadproject.R.color.pg_primary;
            case "partially_approved":       return com.nibm.pharmagomadproject.R.color.pg_primary;
            case "partially_rejected":       return com.nibm.pharmagomadproject.R.color.pg_accent;
            case "awaiting_approval":        return com.nibm.pharmagomadproject.R.color.pg_sub;
            default:                         return android.R.color.darker_gray;
        }
    }
}
