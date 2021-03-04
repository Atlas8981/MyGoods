package com.example.mygoods.Firewall;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailEditText;
    private Button sendEmailBtn;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.emaileditText);
        sendEmailBtn = findViewById(R.id.sendEmailButton);

        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getEditText().getText().toString().trim();

                if (checkEmail(email)) {

                    auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ForgotPasswordActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ForgotPasswordActivity.this, "Email Not Found in Database", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                }
            }
        });

        if (getIntent().getExtras() != null){
            String email = getIntent().getExtras().getString("email");
            emailEditText.getEditText().setText(email);
        }
    }

    private boolean checkEmail(String email) {
        boolean flag = true;

        if (email.isEmpty()){
            emailEditText.setError("Email is Empty");
            flag = false;
        }else{
            emailEditText.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("Email is Invalid");
            flag = false;
        }else{
            emailEditText.setError(null);
        }
        return flag;
    }
}