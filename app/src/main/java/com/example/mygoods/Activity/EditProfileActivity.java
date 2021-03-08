package com.example.mygoods.Activity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference userRef = firestore.collection("users");
    private FirebaseUser firebaseUser = auth.getCurrentUser();

    private User currentUser;

    private TextInputLayout firstnameEdt, lastnameEdt, usernameEdt, addressEdt, phoneEdt,emailEdt;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setTitle("Edit Profile Information");

        initializeUI();

        Bundle bundle = getIntent().getExtras();
        if (bundle.get("user") != null) {
            currentUser = (User) bundle.get("user");
        }
        putDataIntoViews();



    }

    private void initializeUI() {
        firstnameEdt = findViewById(R.id.firstNameProfile);
        lastnameEdt = findViewById(R.id.lastNameProfile);
        usernameEdt = findViewById(R.id.usernameProfile);
        addressEdt = findViewById(R.id.addressProfile);
        phoneEdt = findViewById(R.id.phoneProfile);
        emailEdt = findViewById(R.id.emailProfile);
        saveBtn = findViewById(R.id.saveProfileBtn);


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkViews()) {
                    saveBtn.setEnabled(false);
                    updateUserInfo();
                }

            }
        });
    }

    private boolean checkViews(){
        boolean flag = true;


//        Drawable errorBox = getResources().getDrawable( R.drawable.custom_red_text_box );
//        Drawable goodBox = getResources().getDrawable( R.drawable.custom_text_box );
        String errorMsg = "Empty Field";

        if (firstnameEdt.getEditText().getText().toString().trim().isEmpty()){
            firstnameEdt.setError(errorMsg);
            flag = false;
        }else{
            firstnameEdt.setError(null);
        }

        if (lastnameEdt.getEditText().getText().toString().trim().isEmpty()){
            lastnameEdt.setError(errorMsg);
            flag = false;
        }else{
            lastnameEdt.setError(null);
        }

        if (addressEdt.getEditText().getText().toString().trim().isEmpty()){
            addressEdt.setError(errorMsg);
            flag = false;
        }else {
            addressEdt.setError(null);
        }

        if (usernameEdt.getEditText().getText().toString().trim().isEmpty()){
            usernameEdt.setError(errorMsg);
            flag = false;
        }else {
            usernameEdt.setError(null);
        }

        if (phoneEdt.getEditText().getText().toString().trim().isEmpty()){

            phoneEdt.setError(errorMsg);
            flag = false;
        }else if (phoneEdt.getEditText().getText().toString().length() > 15
                || phoneEdt.getEditText().getText().toString().length() < 9){
            phoneEdt.setError("Invalid Phone Number");
            flag = false;
        }else{
            phoneEdt.setError(null);
        }

        if (emailEdt.getEditText().getText().toString().trim().isEmpty()){
            emailEdt.setError(errorMsg);
            flag=false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(emailEdt.getEditText().getText().toString().trim()).matches()){
            emailEdt.setError("Invalid Email");
        }
        else {
            emailEdt.setError(null);
        }

        return flag;
    }

    private void updateUserInfo() {
        String firstname = firstnameEdt.getEditText().getText().toString().trim();
        String lastname = lastnameEdt.getEditText().getText().toString().trim();
        String address = addressEdt.getEditText().getText().toString().trim();
        String username = usernameEdt.getEditText().getText().toString().trim();
        String phone = phoneEdt.getEditText().getText().toString().trim();
        String email = emailEdt.getEditText().getText().toString().trim();

        currentUser.setFirstname(firstname);
        currentUser.setLastname(lastname);
        currentUser.setAddress(address);
        currentUser.setUsername(username);
        currentUser.setPhoneNumber(phone);
        currentUser.setEmail(email);

        if (auth.getUid() != null) {
            firebaseUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    userRef.document(auth.getUid()).set(currentUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            saveBtn.setEnabled(true);
                            Toast.makeText(EditProfileActivity.this, "Information Update Successfully", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            saveBtn.setEnabled(true);
                            Toast.makeText(EditProfileActivity.this, "Cannot Update Information in Database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    saveBtn.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Can't Update User Email", Toast.LENGTH_SHORT).show();


                }
            });

        }else {
            Toast.makeText(this, "No User Login", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void putDataIntoViews() {
        if (currentUser != null){
            firstnameEdt.getEditText().setText(currentUser.getFirstname());
            lastnameEdt.getEditText().setText(currentUser.getLastname());
            usernameEdt.getEditText().setText(currentUser.getUsername());
            addressEdt.getEditText().setText(currentUser.getAddress());
            phoneEdt.getEditText().setText(currentUser.getPhoneNumber());
            emailEdt.getEditText().setText(currentUser.getEmail());
        }
    }
}