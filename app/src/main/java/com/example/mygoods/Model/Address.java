package com.example.mygoods.Model;

import java.io.Serializable;

public class Address implements Serializable {
    private String district;
    private String streetName;
    private String province;
    private String khan;
    private String houseNumber;


    public Address() {
//        this.district = "";
//        this.streetName = "";
//        this.province = "";
//        this.khan = "";
//        this.houseNumber = "";
    }

    public Address(String district, String streetName, String province, String khan, String houseNumber) {
        this.district = district;
        this.streetName = streetName;
        this.province = province;
        this.khan = khan;
        this.houseNumber = houseNumber;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getKhan() {
        return khan;
    }

    public void setKhan(String khan) {
        this.khan = khan;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    @Override
    public String toString() {
        return houseNumber + ", " + streetName + ", " + district + ", " + khan + ", " + province;
    }
}
