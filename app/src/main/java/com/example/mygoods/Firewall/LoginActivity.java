package com.example.mygoods.Firewall;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Activity.Home.HomeActivity;
import com.example.mygoods.Firewall.SignUp.EmailVerificationActivity;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout eemail, ppassword;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private Button forgotPasswordBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Log In");

        progressBar = findViewById(R.id.progressBar2);
        eemail = findViewById(R.id.emaileditText);
        ppassword = findViewById(R.id.passwordeditText);
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

    public void loginuser() {
//        SQLiteManager sqLiteManager = new SQLiteManager(LoginActivity.this);
//        sqLiteManager.open();
        progressBar.setVisibility(View.VISIBLE);
        String email = eemail.getEditText().getText().toString().trim();
        String password = ppassword.getEditText().getText().toString().trim();

        if (checkViews(email, password)) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (authResult.getUser().isEmailVerified()) {
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                sqLiteManager.dropTable();
                                startActivity(intent);
                                finish();

                                Toast.makeText(LoginActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                            } else {

                                //                            Event if user have account but account not verify

                                Toast.makeText(LoginActivity.this,
                                        "Please verify your email first" +
                                                "\nVerification Email have been sent to "
                                                + email, Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                Intent intent = new Intent(getApplicationContext(), EmailVerificationActivity.class);
                                //                            Skip to password mean that user have a password of their own and they can forgot password / Email is real
                                //                            So they don't need to enter password
                                intent.putExtra("skipToPassword", "true");
                                intent.putExtra("email", email);
                                intent.putExtra("password", password);
                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private boolean checkViews(String email, String password) {
        boolean flag = true;
        if (TextUtils.isEmpty(email)) {
            eemail.setError("Email is require");
            eemail.setErrorIconDrawable(null);
            progressBar.setVisibility(View.INVISIBLE);
            flag = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            eemail.setError("Email is incorrect format");
            eemail.setErrorIconDrawable(null);
            progressBar.setVisibility(View.INVISIBLE);
            flag = false;
        }
        if (TextUtils.isEmpty(password)) {
            ppassword.setError("Password is require");
            ppassword.setErrorIconDrawable(null);
            progressBar.setVisibility(View.INVISIBLE);
            flag = false;
        }

        return flag;
    }


    public void loginBtn(View V) {
        loginuser();
    }

}