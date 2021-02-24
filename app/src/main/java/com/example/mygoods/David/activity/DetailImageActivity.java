package com.example.mygoods.David.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.mygoods.David.others.ViewPagerAdapter;
import com.example.mygoods.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class DetailImageActivity extends AppCompatActivity implements ViewPagerAdapter.OnViewPagerItemClick {

    private ImageButton dismissButton;
    private ImageView fullSizeImageView;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<String> imageUrl;
    private boolean isSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_image);

        setupViews();
    }

    private void setupViews() {
        dismissButton = (ImageButton) findViewById(R.id.dismissButton);
        dismissButton.setVisibility(View.GONE);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        // Views pager (Images slider)
        imageUrl = (ArrayList<String>) getIntent().getSerializableExtra("ItemDetailImageURL");
        viewPager = (ViewPager) findViewById(R.id.fullSizeViewPager);
        viewPagerAdapter = new ViewPagerAdapter(this, imageUrl, this);
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.fullSizeViewPagerTabLayout);
        tabLayout.setupWithViewPager(viewPager, true);
    }

    @Override
    public void onViewPagerItemClickListener() {
        if (isSelected == false) {
            dismissButton.setVisibility(View.VISIBLE);
            isSelected = true;
        }else {
            dismissButton.setVisibility(View.GONE);
            isSelected = false;
        }

    }
}