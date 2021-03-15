package com.example.mygoods.Activity.AboutMe;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.R;

public class AboutOurAppActivity extends AppCompatActivity {

    private TextView aboutAppText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_our_app);

        setTitle("About MyGoods");

        aboutAppText = findViewById(R.id.aboutText);


        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            String version = pInfo.versionName;
            description = description + "\n\nVersion : " + version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        aboutAppText.setText(description);
    }

    private static String description = "MyGoods  will be focusing on making a better digital marketplace for users to " +
            "buy and sell used products. This application hope to help our sellers to sell their " +
            "products freely and easily with great profits while buyers able to find the products " +
            "they need quickly and easily. Moreover, this application is going to have a dynamic " +
            "homepage that adapt to the user preferences, huge number of categories, great " +
            "search algorithm with interactive user interface, and all the basic functions to satisfy " +
            "users’ needs. Some features are being exclude due to time constraint. Those features " +
            "include auction, adaptive user preference, and daily deals function that show the best " +
            "deal of the day. Overall, all the current features that going to be included on " +
            "deployment will help buyers and sellers get closer to each other virtually while they " +
            "are physically far apart.";

}