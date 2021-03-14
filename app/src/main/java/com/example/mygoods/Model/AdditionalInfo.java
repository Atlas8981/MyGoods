package com.example.mygoods.Model;

import java.util.Objects;

public class AdditionalInfo {
    private Car car;
    private Phone phone;
    private String motoType;
    private String computerParts;
    private String condition;
    private String bikeType;

    public AdditionalInfo() {}

    public AdditionalInfo(AdditionalInfo additionalInfo) {
        this.car = additionalInfo.getCar();
        this.phone = additionalInfo.getPhone();
        this.motoType = additionalInfo.getMotoType();
        this.computerParts = additionalInfo.getComputerParts();
        this.condition = additionalInfo.getCondition();
        this.bikeType = additionalInfo.getBikeType();
    }

    public AdditionalInfo(Car car, Phone phone, String motoType, String computerParts, String condition, String bikeType) {
        this.car = car;
        this.phone = phone;
        this.motoType = motoType;
        this.computerParts = computerParts;
        this.condition = condition;
        this.bikeType = bikeType;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public String getMotoType() {
        return motoType;
    }

    public void setMotoType(String motoType) {
        this.motoType = motoType;
    }

    public String getComputerParts() {
        return computerParts;
    }

    public void setComputerParts(String computerParts) {
        this.computerParts = computerParts;
    }

    @Override
    public String toString() {
        return "AdditionalInfo{" +
                "car=" + car +
                ", phone=" + phone +
                ", motoType='" + motoType + '\'' +
                ", computerParts='" + computerParts + '\'' +
                ", condition='" + condition + '\'' +
                ", bikeType='" + bikeType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdditionalInfo)) return false;
        AdditionalInfo info = (AdditionalInfo) o;
        return Objects.equals(car, info.car) &&
                Objects.equals(phone, info.phone) &&
                Objects.equals(motoType, info.motoType) &&
                Objects.equals(computerParts, info.computerParts) &&
                Objects.equals(condition, info.condition) &&
                Objects.equals(bikeType, info.bikeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(car, phone, motoType, computerParts, condition, bikeType);
    }
}
