package com.nibm.pharmagomadproject.pharmacyowner;


public class InventoryModel {

    private String medicineName;
    private String category;
    private double price;
    private int stock;
    private int maxStock;


    // Empty constructor (Firestore සඳහා)
    public InventoryModel() {

    }


    // Existing constructor
    public InventoryModel(String medicineName,
                          String category,
                          double price,
                          int stock) {

        this.medicineName = medicineName;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.maxStock = 100;
    }


    // ADD THIS constructor
    public InventoryModel(String medicineName,
                          String category,
                          String price,
                          int stock,
                          int maxStock) {

        this.medicineName = medicineName;
        this.category = category;

        // "Rs.40" වගේ String එක Double කරන්න
        this.price = Double.parseDouble(
                price.replace("Rs.", "")
        );

        this.stock = stock;
        this.maxStock = maxStock;
    }



    public String getMedicineName() {
        return medicineName;
    }


    public String getCategory() {
        return category;
    }


    public double getPrice() {
        return price;
    }


    public int getStock() {
        return stock;
    }


    public int getMaxStock() {
        return maxStock;
    }


    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }


    public void setCategory(String category) {
        this.category = category;
    }


    public void setPrice(double price) {
        this.price = price;
    }


    public void setStock(int stock) {
        this.stock = stock;
    }


    public void setMaxStock(int maxStock) {
        this.maxStock = maxStock;
    }

}