package com.example.mygoods.Firewall;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {
    private TextView verifyEmailText;
    private Button verifyEmailBtn,skipBtn;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @SuppressLint("SetTextI18n")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        verifyEmailBtn = findViewById(R.id.verifyEmailButton);
        skipBtn = findViewById(R.id.skipVerifyEmailButton);
        verifyEmailText = findViewById(R.id.verifyEmailText);

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationEmail();
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToPreference();
            }
        });



        if (getIntent().getExtras()!=null){
            if (getIntent().getExtras().getString("email")!=null){
                String email = getIntent().getExtras().getString("email");
                verifyEmailText.setText(
                        getResources().getString(R.string.emailVerificationMessageStart)
                        +email
                        + getResources().getString(R.string.emailVerificationMessageEnd));
            }
        }




        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (!firebaseAuth.getCurrentUser().isEmailVerified()){
                        sendVerificationEmail();
                    }else{
                        Toast.makeText(EmailVerificationActivity.this, "User verified", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


    }


    private void moveToPreference() {
        Intent intent = new Intent(getApplicationContext(), User_PreferenceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

    }

    private void sendVerificationEmail(){
        if (auth.getCurrentUser()!= null) {
            auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(EmailVerificationActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EmailVerificationActivity.this, "Email Does not exist", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
}