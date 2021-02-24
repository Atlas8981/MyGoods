package com.example.mygoods.Model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {
    private String userId;
    private String firstname;
    private String lastname;
    private String username;
    private String phoneNumber;
    private String email;
    private Image image;
    private List<String> preferenceid;
    private String address;

    public User(){

    }
    public User(User user){
        this.userId = user.userId;
        this.firstname = user.firstname;
        this.lastname = user.lastname;
        this.username = user.username;
        this.phoneNumber = user.phoneNumber;
        this.email = user.email;
        this.image = user.image;
        this.preferenceid = user.preferenceid;
        this.address = user.address;
    }
    public User(String userId, String firstname, String lastname, String username, String phoneNumber, String email,Image image, List<String> preference, String address) {
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.image = image;
        this.preferenceid = preference;
        this.address = address;
    }

    public User(String userId, String firstname, String lastname, String username, String phonenumber, String email, String address) {
        this.userId = userId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.phoneNumber = phonenumber;
        this.email = email;
        this.address = address;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getPreferenceid() {
        return preferenceid;
    }

    public void setPreferenceid(List<String> preferenceid) {
        this.preferenceid = preferenceid;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", username='" + username + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", email='" + email + '\'' +
                ", image=" + image +
                ", preferenceid=" + preferenceid +
                ", address=" + address +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(firstname, user.firstname) &&
                Objects.equals(lastname, user.lastname) &&
                Objects.equals(username, user.username) &&
                Objects.equals(phoneNumber, user.phoneNumber) &&
                Objects.equals(email, user.email) &&
                Objects.equals(image, user.image) &&
                Objects.equals(preferenceid, user.preferenceid) &&
                Objects.equals(address, user.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, firstname, lastname, username, phoneNumber, email, image, preferenceid, address);
    }
}
