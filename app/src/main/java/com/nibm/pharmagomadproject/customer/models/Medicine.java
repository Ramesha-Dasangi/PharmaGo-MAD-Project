package com.nibm.pharmagomadproject.customer.models;

public class Medicine {

    private String id;
    private String brandName;
    private String medicineName;
    private String category;
    private String type;
    private double price;
    private int stock;
    private String pharmacy;

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


    public String getId() {
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
}