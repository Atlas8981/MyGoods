package com.example.mygoods.Model;

public class Category {

    private int iconName;
    private String categoryTitle;

    public Category(int iconName, String categoryTitle) {
        this.iconName = iconName;
        this.categoryTitle = categoryTitle;
    }

    public int getIconName() {
        return iconName;
    }

    public void setIconName(int iconName) {
        this.iconName = iconName;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }
}
