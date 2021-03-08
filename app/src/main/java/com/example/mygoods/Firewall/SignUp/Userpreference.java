package com.example.mygoods.Firewall.SignUp;

import java.util.List;

public class Userpreference {

    private List<String> preferences;

    public Userpreference() {}

    public Userpreference(List<String> preferences) {
        this.preferences = preferences;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
}
