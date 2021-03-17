package com.example.mygoods.Firewall.SignUp;

import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private String newPassword, confirmPassword;
    private String email,prePassword;

    private TextInputLayout passwordEdt, confirmPasswordEdt;
    private Button savePasswordBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        initializeUi();

        savePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkViews()) {
                    auth.signInWithEmailAndPassword(email, prePassword)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    if (authResult.getUser().isEmailVerified()){
                                        updatePassword(authResult,newPassword);
                                    }else{
                                        auth.signOut();
                                        Toast.makeText(PasswordActivity.this, "Please Verify Your Email First", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    private boolean checkViews() {
        newPassword = passwordEdt.getEditText().getText().toString().trim();
        confirmPassword = confirmPasswordEdt.getEditText().getText().toString().trim();

        boolean flag = true;

        String errorMsg = "Field is Empty";

        if (newPassword.isEmpty()){
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
        if (!newPassword.isEmpty() && !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
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

    private void updatePassword(AuthResult authResult,String password) {

        if (authResult.getUser()!=null) {
            authResult.getUser().updatePassword(password).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    moveToPersonalInformation();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void moveToPersonalInformation(){
        Intent intent = new Intent(this,PersonalInformationActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeUi() {
        passwordEdt = findViewById(R.id.passwordProfile);
        confirmPasswordEdt = findViewById(R.id.confirmPasswordProfile);
        savePasswordBtn = findViewById(R.id.confirmPasswordBtn);

        if (getIntent().getExtras()!=null){
            if (getIntent().getExtras().getString("email")!=null){
                email = getIntent().getExtras().getString("email");
            }
            if (getIntent().getExtras().getString("password")!=null){
                prePassword = getIntent().getExtras().getString("password");
            }
        }
    }
}