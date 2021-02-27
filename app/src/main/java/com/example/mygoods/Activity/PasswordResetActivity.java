package com.example.mygoods.Activity;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;

public class PasswordResetActivity extends AppCompatActivity {

    private FirebaseAuth auth =FirebaseAuth.getInstance();

    private FirebaseUser user = auth.getCurrentUser();

    private String password, confirmPassword;
    private TextInputLayout passwordEdt, confirmPasswordEdt;
    private Button savePasswordBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        setTitle("Reset Password");

        initializeUi();

        savePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                password = passwordEdt.getEditText().getText().toString().trim();
                confirmPassword = confirmPasswordEdt.getEditText().getText().toString().trim();

                if (user != null && auth.getUid() != null) {
                    if (checkViews()) {
                        savePasswordBtn.setEnabled(false);
                        updatePassword();
                    }

                }else {
                    Toast.makeText(PasswordResetActivity.this, "User is null", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean checkViews() {
        password = passwordEdt.getEditText().getText().toString().trim();
        confirmPassword = confirmPasswordEdt.getEditText().getText().toString().trim();

        boolean flag = true;

        String errorMsg = "Field is Empty";

        if (password.isEmpty()){
            passwordEdt.setError(errorMsg);
            passwordEdt.setErrorIconDrawable(null);
            flag = false;
        }else{
            passwordEdt.setError(null);
        }

        if (confirmPassword.isEmpty()){
            confirmPasswordEdt.setError(errorMsg);
            confirmPasswordEdt.setErrorIconDrawable(null);
            flag = false;
        }else{
            confirmPasswordEdt.setError(null);
        }
        if (!password.isEmpty() && !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                passwordEdt.setError("Password not Match");
                confirmPasswordEdt.setError("Password not Match");

                Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
                flag = false;
            } else {
                passwordEdt.setError(null);
                confirmPasswordEdt.setError(null);
            }
        }

        return flag;

    }

    private void updatePassword() {
        user.updatePassword(password).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PasswordResetActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                onBackPressed();
                savePasswordBtn.setEnabled(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PasswordResetActivity.this, "Password fail to update", Toast.LENGTH_SHORT).show();
                e.getCause();
                e.printStackTrace();

                savePasswordBtn.setEnabled(true);
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initializeUi() {
        passwordEdt = findViewById(R.id.passwordProfile);
        confirmPasswordEdt = findViewById(R.id.confirmPasswordProfile);
        savePasswordBtn = findViewById(R.id.confirmPasswordBtn);
    }

}