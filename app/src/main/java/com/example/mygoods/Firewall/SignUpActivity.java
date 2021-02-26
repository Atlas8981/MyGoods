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

import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText ffirstname, llastname, uusername, pphonenumber,aaddress,eemail,ppassword,cconfirmpassword;
    private Button signUpBtn;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");

        ffirstname = findViewById(R.id.firstnameEditText);
        llastname = findViewById(R.id.lastnameEditText);
        uusername = findViewById(R.id.usernameEditText);
        pphonenumber = findViewById(R.id.phonenumberEditText);
        aaddress = findViewById(R.id.addressEditText);
        eemail = findViewById(R.id.emaileditText);
        ppassword= findViewById(R.id.passwordeditText);
        cconfirmpassword = findViewById(R.id.confirmpasswordEditText);
        signUpBtn = findViewById(R.id.signUpButton);

        progressBar = findViewById(R.id.signUpProgressBar3);
    }

    public void signUpBtn(View v) {

        progressBar.setVisibility(View.VISIBLE);
        signUpBtn.setEnabled(false);
//        convert to String
        String firstname = ffirstname.getText().toString().trim();
        String lastname = llastname.getText().toString().trim();
        String username = uusername.getText().toString().trim();
        String phonenumber = pphonenumber.getText().toString().trim();
        String address = aaddress.getText().toString().trim();
        String email = eemail.getText().toString().trim();
        String password = ppassword.getText().toString().trim();
        String confirmpassword = cconfirmpassword.getText().toString().trim();

//        validation
        if (TextUtils.isEmpty(firstname)) {
            ffirstname.setError("Firstname is required");
            return;
        } else if (TextUtils.isEmpty(lastname)) {
            llastname.setError("Lastname is required");
            return;
        } else if (TextUtils.isEmpty(username)) {
            uusername.setError("Username is reqsduired");
            return;
        } else if (TextUtils.isEmpty(phonenumber)){
            pphonenumber.setError("PhoneNumber is required");
            return;
        } else if (TextUtils.isEmpty(email)) {
            eemail.setError("Email is required");
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            eemail.setError("Invalid email input");
            return;
        }else if (TextUtils.isEmpty(password)) {
            ppassword.setError("Password is required");
            return;
        } else if (TextUtils.isEmpty(confirmpassword)) {
            cconfirmpassword.setError("Confirmpassword is required");
            return;
        } else if (!confirmpassword.equals(password)) {
            cconfirmpassword.setError("Confirm Passoword is not match");
            return;
        }

        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
//                User user = new User(firstname,lastname,username,phonenumber,address,email);

                com.example.mygoods.Model.User user = new com.example.mygoods.Model.User(
                        authResult.getUser().getUid(),
                        firstname,
                        lastname,
                        username,
                        phonenumber,
                        email,
                        address);

                DocumentReference documentReference = firestore.collection("users").document(auth.getUid());
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getApplicationContext(), User_PreferenceActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

//                        Intent intent = new Intent(getApplicationContext(), EmailVerificationActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        intent.putExtra("email",email);
//                        startActivity(intent);
//                        finish();

                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SignUpActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signUpBtn.setEnabled(true);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SignUpActivity.this, "Error" , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                signUpBtn.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(SignUpActivity.this, "Error" , Toast.LENGTH_SHORT).show();
            }
        });

    }



}