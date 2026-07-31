package com.nibm.pharmagomadproject.customer.models;

import java.util.Map;
import com.google.firebase.Timestamp;
import java.util.List;

public class Order {
    private String    orderId;
    private String    customerId;
    private String    pharmacyId;
    private String    pharmacyName;
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
    public List<Map<String,Object>> getItems() { return items; }
    public double    getTotal()        { return total; }
    public String    getStatus()       { return status; }
    public String    getPaymentMethod(){ return paymentMethod; }
    public String    getAddress()      { return address; }
    public Timestamp getCreatedAt()    { return createdAt; }

    public void setOrderId(String v)      { this.orderId = v; }
    public void setStatus(String v)       { this.status = v; }
    public void setPharmacyName(String v) { this.pharmacyName = v; }
    public void setTotal(double v)        { this.total = v; }

    // Status display helper
    public String getStatusDisplay() {
        if (status == null) return "Pending";
        switch (status) {
            case "pending":                   return "Order confirmed";
            case "awaiting_approval":          return "Awaiting approval";
            case "approved_pending_payment":  return "Approved (Pay Now)";
            case "processing":                return "Processing";
            case "picked_up":                 return "Picked up";
            case "out_for_delivery":          return "Out for delivery";
            case "delivered":                 return "Delivered";
            case "cancelled":                 return "Cancelled";
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
            case "approved_pending_payment": return com.nibm.pharmagomadproject.R.color.pg_primary;
            case "awaiting_approval":        return com.nibm.pharmagomadproject.R.color.pg_sub;
            default:                         return android.R.color.darker_gray;
        }
    }
}
