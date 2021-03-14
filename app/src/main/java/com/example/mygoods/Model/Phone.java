package com.example.mygoods.Model;

public class Phone {
    private String phoneBrand;
    private String phoneModel;

    public Phone() {}

    public Phone(String phoneBrand, String phoneModel) {
        this.phoneBrand = phoneBrand;
        this.phoneModel = phoneModel;
    }

    public String getPhoneBrand() {
        return phoneBrand;
    }

    public void setPhoneBrand(String phoneBrand) {
        this.phoneBrand = phoneBrand;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "phoneBrand='" + phoneBrand + '\'' +
                ", phoneModel='" + phoneModel + '\'' +
                '}';
    }
}
