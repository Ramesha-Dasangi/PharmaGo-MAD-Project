package com.nibm.pharmagomadproject.pharmacyowner;

public class InventoryModel {

    private String medicineName;
    private String category;
    private String price;
    private int stock;
    private int maxStock;

    public InventoryModel(String medicineName,
                          String category,
                          String price,
                          int stock,
                          int maxStock) {

        this.medicineName = medicineName;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.maxStock = maxStock;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getMaxStock() {
        return maxStock;
    }
}