package com.example.mygoods.Model;



import java.io.Serializable;
import java.util.Date;
import java.util.List;


public class Item implements Serializable , Comparable<Item>{

    private String itemid;
    private String name;
    private String address;
    private String subCategory;
    private String mainCategory;
    private String description;
    private String userid;
    private String phone;
    private List<Image> images;
    private int amount;
    private double price;
    private List<String> viewers;
    private int views;
    private Date date;

    //    Without empty constructor fireStore won't work
    public Item (){}

//    Without Item Id,View = 0
//    Basically for Add Fragement

    public Item(String name, String address,List<Image> images, String subCategory,String mainCategory, String description, String userid, String phone, double price,Date date) {
        this.name = name;
        this.address = address;
        this.images = images;
        this.subCategory =subCategory;
        this.mainCategory = mainCategory;
        this.description = description;
        this.userid = userid;
        this.price = price;
        this.phone = phone;
        this.date = date;
        this.views = 0;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public List<String> getViewers() {
        return viewers;
    }

    public void setViewers(List<String> viewers) {
        this.viewers = viewers;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public String toString() {
        return "Item{" +
                "itemid='" + itemid + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", mainCategory='" + mainCategory + '\'' +
                ", description='" + description + '\'' +
                ", userid='" + userid + '\'' +
                ", phone='" + phone + '\'' +
                ", images=" + images +
                ", amount=" + amount +
                ", price=" + price +
                ", views=" + views +
                ", date=" + date +
                '}';
    }

    @Override
    public int compareTo(Item o) {
        int compareView=((Item)o).getViews();
        return compareView-this.views;
    }
}


