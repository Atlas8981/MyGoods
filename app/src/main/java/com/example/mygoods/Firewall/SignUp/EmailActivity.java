package com.example.mygoods.Firewall.SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public class EmailActivity extends AppCompatActivity {

    private String email,password;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private Button sendEmailBtn;
    private TextInputLayout emailEdt;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        setTitle("Email");

        emailEdt = findViewById(R.id.emailEditText);
        sendEmailBtn = findViewById(R.id.sendEmailButton);
        progressBar = findViewById(R.id.emailProgress);
        progressBar.setVisibility(View.INVISIBLE);

        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                email = emailEdt.getEditText().getText().toString().trim();
                password = UUID.randomUUID().toString();

                if (checkEmail(email)){
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    if (auth.getCurrentUser()!=null) {
                        auth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                authResult.getUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EmailActivity.this, "Verification Email Sent to " + email, Toast.LENGTH_SHORT).show();
                                        auth.signOut();
                                        moveToEmailVerification();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(EmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(EmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                Toast.makeText(EmailActivity.this, "Email Exists", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private boolean checkEmail(String email) {
        boolean flag = true;

        if (email.isEmpty()){
            emailEdt.setError("Email is Empty");
            flag = false;
        }else{
            emailEdt.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEdt.setError("Email is Invalid");
            flag = false;
        }else{
            emailEdt.setError(null);
        }
        return flag;
    }

    private void moveToEmailVerification() {
        progressBar.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(this,EmailVerificationActivity.class);
        intent.putExtra("email",email);
        intent.putExtra("password",password);
        startActivity(intent);
        finish();
    }

}