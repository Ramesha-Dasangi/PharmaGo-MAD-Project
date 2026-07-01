package com.nibm.pharmagomadproject.pharmacyowner;

public class OrderModel {

    private String orderId;
    private String customerName;
    private String itemCount;
    private String time;
    private String amount;
    private String type;     // RX Required / OTC
    private String status;   // New / Processing / Completed

    public OrderModel(String orderId,
                      String customerName,
                      String itemCount,
                      String time,
                      String amount,
                      String type,
                      String status) {

        this.orderId = orderId;
        this.customerName = customerName;
        this.itemCount = itemCount;
        this.time = time;
        this.amount = amount;
        this.type = type;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getItemCount() {
        return itemCount;
    }

    public String getTime() {
        return time;
    }

    public String getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

}