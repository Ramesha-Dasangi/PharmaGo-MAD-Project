package com.nibm.pharmagomadproject.customer.models;

public class Pharmacy {

    private String id;
    private String name;
    private String ownerName;
    private String phone;
    private String address;
    private String email;
    private double rating;
    private String ownerId;
    private boolean isApproved;
    private double latitude;
    private double longitude;

    // Runtime-only field (not stored in Firestore)
    private double distanceKm = -1;

    public Pharmacy(){}

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getId(){ return id; }
    public void setId(String id){ this.id=id; }
    public String getName(){ return name; }
    public String getOwnerName(){ return ownerName; }
    public String getPhone(){ return phone; }
    public String getAddress(){ return address; }
    public String getEmail(){ return email; }
    public double getRating(){ return rating; }
    public boolean isApproved(){ return isApproved; }
}