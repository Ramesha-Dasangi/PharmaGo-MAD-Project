package com.nibm.pharmagomadproject.pharmacyowner;


public class InventoryModel {


    private String medicineId;
    private String medicineName;
    private String category;
    private double price;
    private int stock;
    private int maxStock;



    // Empty constructor Firestore
    public InventoryModel(){

    }



    public InventoryModel(String medicineId,
                          String medicineName,
                          String category,
                          double price,
                          int stock){


        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.maxStock = 100;

    }




    public String getMedicineId(){

        return medicineId;

    }


    public String getMedicineName(){

        return medicineName;

    }



    public String getCategory(){

        return category;

    }



    public double getPrice(){

        return price;

    }



    public int getStock(){

        return stock;

    }



    public int getMaxStock(){

        return maxStock;

    }



    public void setMedicineId(String medicineId){

        this.medicineId = medicineId;

    }



    public void setMedicineName(String medicineName){

        this.medicineName = medicineName;

    }



    public void setCategory(String category){

        this.category = category;

    }



    public void setPrice(double price){

        this.price = price;

    }



    public void setStock(int stock){

        this.stock = stock;

    }



    public void setMaxStock(int maxStock){

        this.maxStock = maxStock;

    }

    private boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}