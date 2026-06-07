package com.santediagnostics.models;

import java.time.LocalDateTime;

public class TestType {

    private int id;
    private String name;
    private String category;
    private double price;
    private int tatHours;
    private String resultFormat;
    private LocalDateTime createdAt;

    public TestType() {}

    public TestType(int id, String name, String category, double price, int tatHours, String resultFormat) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.tatHours = tatHours;
        this.resultFormat = resultFormat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getTatHours() { return tatHours; }
    public void setTatHours(int tatHours) { this.tatHours = tatHours; }

    public String getResultFormat() { return resultFormat; }
    public void setResultFormat(String resultFormat) { this.resultFormat = resultFormat; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + category + ") - ₦" + price;
    }
}