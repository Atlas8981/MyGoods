package com.example.mygoods.Firewall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Activity.HomeActivity;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class User_PreferenceActivity extends AppCompatActivity {

    private CheckBox ccar,mmotobike,bbicycle,ddesktop,ttable,cchair_sofa;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__preference);

        ccar = findViewById(R.id.carcheckBox);
        mmotobike = findViewById(R.id.motobikecheckBox);
        bbicycle = findViewById(R.id.bicyclecheckBox);
        ddesktop = findViewById(R.id.desktopcheckBox);
        ttable = findViewById(R.id.tablecheckBox);
        cchair_sofa = findViewById(R.id.chairsofacheckBox);

//        skipButton = findViewById(R.id.skipButton);
//
//        skipButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                launchHomeActivity();
//            }
//        });
    }

    public void checkingthebox(){

        List<String> tempPreferences = new ArrayList<>();
        if (ccar.isChecked()) {
            tempPreferences.add("car");
        }
        if (mmotobike.isChecked()) {
            tempPreferences.add("motobike");
        }
        if (bbicycle.isChecked()) {
            tempPreferences.add("bicycle");
        }
        if (ddesktop.isChecked()) {
            tempPreferences.add("desktop");
        }
        if (ttable.isChecked()) {
            tempPreferences.add("table");
        }
        if (cchair_sofa.isChecked()) {
            tempPreferences.add("chair");
        }

        DocumentReference documentReference = firestore.collection("users").document(auth.getUid());

        documentReference.update("preferenceid", tempPreferences).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                launchHomeActivity();
            }
        });
    }

    public void submitBtn (View V) {
        checkingthebox();
    }

    private void launchHomeActivity(){
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}