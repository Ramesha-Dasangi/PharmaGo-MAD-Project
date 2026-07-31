package com.nibm.pharmagomadproject.customer.models;

public class Cart {
    private String medicineId;
    private String medicineName;
    private String brandName;
    private String pharmacyId;
    private String pharmacyName;
    private double price;
    private int    quantity;
    private String medicineType;     // "OTC", "Prescription", "Rx", etc.

    public Cart() {}

    public Cart(String medicineId, String medicineName, String brandName, String pharmacyId,
                String pharmacyName, double price, int quantity) {
        this.medicineId   = medicineId;
        this.medicineName = medicineName;
        this.brandName    = brandName;
        this.pharmacyId   = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.price        = price;
        this.quantity     = quantity;
    }

    public String getMedicineId()           { return medicineId; }
    public String getMedicineName()         { return medicineName; }
    public String getBrandName()            { return brandName; }
    public String getPharmacyId()           { return pharmacyId; }
    public String getPharmacyName()         { return pharmacyName; }
    public double getPrice()                { return price; }
    public int    getQuantity()             { return quantity; }
    public void   setQuantity(int q)        { this.quantity = q; }
    public double getSubtotal()             { return price * quantity; }
    public String getMedicineType()         { return medicineType; }
    public void   setMedicineType(String t) { this.medicineType = t; }

    //Returns true if this item requires a prescription
    public boolean isRx() {
        return "Prescription".equalsIgnoreCase(medicineType)
                || "Rx".equalsIgnoreCase(medicineType);
    }
}