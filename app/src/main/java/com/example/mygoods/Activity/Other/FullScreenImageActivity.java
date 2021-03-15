package com.example.mygoods.Activity.Other;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.mygoods.Adapters.ImageAdapter;
import com.example.mygoods.Model.Image;
import com.example.mygoods.R;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class FullScreenImageActivity extends AppCompatActivity {

    private ViewPager fullScreenViewPager;
    private DotsIndicator fullScreenDotsIndicator;
    private int position;
    private ImageButton cancelButton;

    private SlidrInterface slidrInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.VERTICAL)
                .build();

        slidrInterface = Slidr.attach(this,config);

        Bundle bundle = getIntent().getExtras();


//        List<Image> images = (List<Image>) bundle.get("images");
        if (bundle.get("images") != null) {

            List<Image> images = new ArrayList<>((List<Image>) bundle.get("images"));

            fullScreenViewPager = findViewById(R.id.fullScreenViewPager);
            fullScreenDotsIndicator = findViewById(R.id.fullScreenDotIndicator);
            cancelButton = findViewById(R.id.cancelButton);

            ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext(), images, true);
            fullScreenViewPager.setAdapter(imageAdapter);

            if (bundle.get("position") != null) {
                position = (int) bundle.get("position");
                fullScreenViewPager.setCurrentItem(position);
            }

            fullScreenDotsIndicator.setViewPager(fullScreenViewPager);


        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}