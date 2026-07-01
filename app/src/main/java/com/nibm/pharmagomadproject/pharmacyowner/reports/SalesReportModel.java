package com.nibm.pharmagomadproject.pharmacyowner.reports;

public class SalesReportModel {

    private String day;
    private int value;

    public SalesReportModel(String day, int value) {
        this.day = day;
        this.value = value;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
