package com.example.mygoods.Model;

import android.widget.TextView;

import androidx.cardview.widget.CardView;

import de.hdodenhof.circleimageview.CircleImageView;

public class PopularCategoryView {
    public CardView cardView;
    public TextView popularCategoryText;
    public CircleImageView popularCategoryImage;

    public PopularCategoryView() {
    }
    public PopularCategoryView(CardView cardView, TextView popularCategoryText, CircleImageView popularCategoryImage) {
        this.cardView = cardView;
        this.popularCategoryText = popularCategoryText;
        this.popularCategoryImage = popularCategoryImage;
    }

}
