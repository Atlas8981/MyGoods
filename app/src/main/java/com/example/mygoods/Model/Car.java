package com.example.mygoods.Model;

public class Car {
    private String brand;
    private String model;
    private String category;
    private String year;

    public Car() {
        this.brand = "";
        this.model = "";
        this.category = "";
        this.year = "";
    }

    public Car(String brand, String model, String category, String year) {
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.year = year;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return brand + ", " + model + ", " + category + ", " + year;
    }
}
