package com.nibm.pharmagomadproject.customer.models;

public class Medicine {

    private String id;
    private String brandName;
    private String medicineName;
    private String category;
    private String type;
    private double price;
    private int stock;
    private String pharmacy;     // pharmacy display name
    private String pharmacyId;   // pharmacy owner's uid (matches "pharmacyId" field in medicines docs)
    private String imageUrl;

    public Medicine() {
        // Required for Firebase
    }


    public Medicine(String id,
                    String brandName,
                    String medicineName,
                    String category,
                    String type,
                    double price,
                    int stock,
                    String pharmacy) {

        this.id = id;
        this.brandName = brandName;
        this.medicineName = medicineName;
        this.category = category;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.pharmacy = pharmacy;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    /** Convenience alias matching the cart/detail activity API. */
    public String getMedicineId() {
        return id;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getPharmacy() {
        return pharmacy;
    }

    public String getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(String pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
}