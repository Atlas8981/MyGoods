package com.example.mygoods.Firewall.SignUp;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailVerificationActivity extends AppCompatActivity {
    private TextView verifyEmailText;
    private Button verifyEmailBtn,doneVerificationBtn;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private String email,password;
    @SuppressLint("SetTextI18n")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        verifyEmailBtn = findViewById(R.id.verifyEmailButton);
        verifyEmailText = findViewById(R.id.verifyEmailText);
        doneVerificationBtn = findViewById(R.id.doneVerificationButton);

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationEmail();
            }
        });

        doneVerificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getIntent().getExtras()!=null){
                    if (getIntent().getExtras().get("skipToPassword") == "true"){
                        Intent intent = new Intent(getApplicationContext(), PersonalInformationActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        moveToPassword();
                    }
                }else{
                    moveToPassword();
                }
            }
        });

        if (getIntent().getExtras()!=null){
            if (getIntent().getExtras().getString("email")!=null){

                email = getIntent().getExtras().getString("email");

                verifyEmailText.setText(
                        getResources().getString(R.string.emailVerificationMessageStart)
                        + " " + email + " "
                        + getResources().getString(R.string.emailVerificationMessageEnd));
            }
            if (getIntent().getExtras().getString("password")!=null){
                password = getIntent().getExtras().getString("password");
            }
        }


    }

    private void moveToPassword() {
        Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
        intent.putExtra("email",email);
        intent.putExtra("password",password);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

    }

    private void sendVerificationEmail(){
       auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
           @Override
           public void onSuccess(AuthResult authResult) {
               authResult.getUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       auth.signOut();
                       Toast.makeText(EmailVerificationActivity.this, "Verification Email Resent", Toast.LENGTH_SHORT).show();
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       auth.signOut();
                       Toast.makeText(EmailVerificationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(EmailVerificationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
           }
       });




    }
}