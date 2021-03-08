package com.example.mygoods.Firewall;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Activity.HomeActivity;
import com.example.mygoods.Firewall.SignUp.EmailVerificationActivity;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText eemail, ppassword;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Button forgotPasswordBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Sign In");


        eemail = findViewById(R.id. emaileditText);
        ppassword = findViewById(R.id. passwordeditText);
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);

        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPasswordBtn.setEnabled(false);
                moveToForgotPasswordScreen();
            }
        });

    }

    private void moveToForgotPasswordScreen() {
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void loginuser (){
        String email = eemail.getText().toString().trim();
        String password = ppassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            eemail.setError("Email is require");
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            eemail.setError("Email is incorrect format");
        }else if(TextUtils.isEmpty(password)) {
            ppassword.setError("Password is require");
        }

        auth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if(authResult.getUser().isEmailVerified()){
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                            Toast.makeText(LoginActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this,
                                    "Please verify your email first" +
                                    "\nVerification Email have been sent to "
                                    + email, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), EmailVerificationActivity.class);
                            intent.putExtra("skipToPassword","true");
                            startActivity(intent);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginBtn (View V){
        loginuser();
    }
}