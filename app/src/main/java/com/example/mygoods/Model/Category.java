package com.example.mygoods.Model;

public class Category {

    private int iconName;
    private String subCategoryTitle;

    public Category(int iconName, String subCategoryTitle) {
        this.iconName = iconName;
        this.subCategoryTitle = subCategoryTitle;
    }

    public int getIconName() {
        return iconName;
    }

    public void setIconName(int iconName) {
        this.iconName = iconName;
    }

    public String getSubCategoryTitle() {
        return subCategoryTitle;
    }

    public void setSubCategoryTitle(String subCategoryTitle) {
        this.subCategoryTitle = subCategoryTitle;
    }
}
