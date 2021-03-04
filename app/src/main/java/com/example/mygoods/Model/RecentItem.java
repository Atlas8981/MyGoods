package com.example.mygoods.Model;

import com.google.firebase.Timestamp;

public class RecentItem {

    private String itemId;
    private Timestamp date;

    public RecentItem(String itemId, Timestamp date) {
        this.itemId = itemId;
        this.date = date;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
