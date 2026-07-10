package com.nibm.pharmagomadproject.pharmacyowner.inventory;

public class Medicine {

    private String medicineName;
    private String category;
    private String brand;
    private String type;
    private double price;
    private int stock;
    private String expiryDate;
    private String description;
    private String imageUrl;
    private String pharmacyId;
    private long createdAt;

    // Empty constructor (Firestore වලට අවශ්‍යයි)
    public Medicine() {
    }

    public Medicine(String medicineName,
                    String category,
                    String brand,
                    String type,
                    double price,
                    int stock,
                    String expiryDate,
                    String description,
                    String imageUrl,
                    String pharmacyId,
                    long createdAt) {

        this.medicineName = medicineName;
        this.category = category;
        this.brand = brand;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.expiryDate = expiryDate;
        this.description = description;
        this.imageUrl = imageUrl;
        this.pharmacyId = pharmacyId;
        this.createdAt = createdAt;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(String pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}