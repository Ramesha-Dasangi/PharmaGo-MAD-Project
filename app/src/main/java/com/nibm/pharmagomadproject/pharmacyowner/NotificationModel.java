package com.nibm.pharmagomadproject.pharmacyowner;

public class NotificationModel {

    private String title;
    private String description;
    private String time;
    private int color;

    public NotificationModel(String title, String description, String time, int color) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public int getColor() {
        return color;
    }

}