package com.example.mygoods.Firewall;
import java.io.Serializable;

public class User implements Serializable {

    private String firstname,
            lastname,
            username,
            phonenumber,
            address,
            email;


    public User() {}

    public User(String firstname, String lastname, String username, String phonenumber, String address, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.phonenumber = phonenumber;
        this.address = address;
        this.email = email;
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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}