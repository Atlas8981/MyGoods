package com.example.mygoods.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Firewall.WelcomeActivity;
import com.example.mygoods.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if (auth.getUid() == null) {
                    Intent mainIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
                    startActivity(mainIntent);
                }else {
                    Intent mainIntent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(mainIntent);
                }
                finish();
            }
        }, 2000);
    }
}