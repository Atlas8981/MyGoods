package com.example.mygoods.Model;

public class PopularCategory {

    private String categoryName;
    private int categoryImageRes;

    public PopularCategory(){

    }

    public PopularCategory(String categoryName, int categoryImageRes) {
        this.categoryName = categoryName;
        this.categoryImageRes = categoryImageRes;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryImageRes() {
        return categoryImageRes;
    }

    public void setCategoryImageRes(int categoryImageRes) {
        this.categoryImageRes = categoryImageRes;
    }
}
