package com.example.mygoods.Firewall.SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Activity.Home.HomeActivity;
import com.example.mygoods.Adapters.GridViewAdapter;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;


public class User_PreferenceActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private GridView preferenceGridView;

    private List<String> subCategories ;

    private GridViewAdapter gridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preference);
        setTitle("User Preferences");

        populatePreference();


        preferenceGridView = findViewById(R.id.preferenceGrid);
        gridViewAdapter = new GridViewAdapter(getApplicationContext(), subCategories);
        preferenceGridView.setAdapter(gridViewAdapter);

    }

    private void populatePreference() {
        subCategories = new ArrayList<>();

        subCategories.addAll(Arrays.asList(getResources().getStringArray(R.array.electronic)));
        subCategories.addAll(Arrays.asList(getResources().getStringArray(R.array.vehicle)));
        subCategories.addAll(Arrays.asList(getResources().getStringArray(R.array.furiture)));

        subCategories.removeIf(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.toLowerCase().contains("other");
            }
        });

    }

    public void checkingthebox(){

        List<String> selectedPreferences = new ArrayList<>(gridViewAdapter.getCheckedItems());

        if (gridViewAdapter!=null) {
            if(selectedPreferences.size()>=1 && selectedPreferences.size()<=5 ){
                if (auth.getUid()!=null) {
                    DocumentReference documentReference = firestore.collection("users").document(auth.getUid());
                    documentReference.update("preferenceid", selectedPreferences)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    launchHomeActivity();
                                }
                            });
                }else{
                    Toast.makeText(this, "No Account", Toast.LENGTH_SHORT).show();
                }

            }else if (selectedPreferences.size()==0){
                Toast.makeText(this, "Please Select At Least 1", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Cannot Select More than 5", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onBackPressed() {

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