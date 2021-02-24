package com.example.mygoods.Firewall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Activity.HomeActivity;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Button guestButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setTitle("Welcome");

        guestButton = findViewById(R.id.guestButton);


        if (getIntent().getExtras() !=null){
            guestButton.setVisibility(View.INVISIBLE);
        }
    }


    private boolean confirmBack = false;
    @Override
    public void onBackPressed() {
        if (getIntent().getExtras() != null){
            super.onBackPressed();
        }else {
            if (!confirmBack) {
                confirmBack = true;
                Toast.makeText(getApplicationContext(), "Press Back again to exit", Toast.LENGTH_SHORT).show();
            } else {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    confirmBack = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
            if (getIntent().getExtras() == null) {
                if (auth.getUid() != null) {
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
    }

    public void signupBtn (View V) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void loginBtn (View V) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void guestBtn (View V) {
        auth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                Toast.makeText(WelcomeActivity.this, authResult.getUser().getUid(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}