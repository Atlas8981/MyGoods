package com.example.mygoods.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mygoods.David.activity.ItemDetailActivity;
import com.example.mygoods.David.activity.SearchActivity;
import com.example.mygoods.Firewall.WelcomeActivity;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;
import com.example.mygoods.fragments.AboutMeFragment;
import com.example.mygoods.fragments.AddFragment;
import com.example.mygoods.fragments.CategoryFragment;
import com.example.mygoods.fragments.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements HomeFragment.HomeFragmentInterface {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        if (!isNetworkConnected()){
            Intent intent = new Intent(getApplicationContext(), InternetScreenActivity.class);
            startActivity(intent);
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private boolean confirmBack = false;

    @Override
    public void onBackPressed() {
        if (!confirmBack){
            confirmBack = true;
            Toast.makeText(getApplicationContext(), "Press Back again to exit", Toast.LENGTH_SHORT).show();
        }else{
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                confirmBack = false;
            }
        }, 2000);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;

            switch (item.getItemId()){
                case R.id.navigation_home:
                    selectedFragment = new HomeFragment();
                    setTitle("Home");
                    break;
                case R.id.navigation_category:
                    selectedFragment = new CategoryFragment();
                    setTitle("Category");
                    break;
                case R.id.navigation_add: {
                    if (auth.getCurrentUser()!=null){
                        if (!auth.getCurrentUser().isAnonymous()) {
                            selectedFragment = new AddFragment();
                            setTitle("Add Item");
                        }else{
                            moveToWelcomeActivity();
                        }
                    }else{
                        moveToWelcomeActivity();
                    }
                } break;
                case R.id.navigation_aboutMe: {
                    if (auth.getCurrentUser()!=null){
                        if (!auth.getCurrentUser().isAnonymous()) {
                            selectedFragment = new AboutMeFragment();
                            setTitle("About Me");
                        }else{
                            moveToWelcomeActivity();
                        }
                    }else{
                        moveToWelcomeActivity();
                    }
                }break;
            }

            if (selectedFragment != null && bottomNavigationView.getSelectedItemId() != item.getItemId()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
            }
            return true;
        }
    };
    private void moveToWelcomeActivity(){
        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
        intent.putExtra("wantToSign","true");
        startActivity(intent);
    }


    @Override
    public void onViewAllButtonClickListener(Class destinationActivity, ArrayList<String> data, String title, String userID) {
        Intent intent = new Intent();
        intent.setClass(this, destinationActivity);
        intent.putExtra("dataFromHomeActivity", data);
        intent.putExtra("titleFromHomeActivity", title);
        intent.putExtra("userIDFromHomeActivity", userID);
        startActivity(intent);
    }


    public void onCollectionViewItemClickListener(int pos, ArrayList<Item> itemData) {
        Intent intent = new Intent();
        intent.setClass(this, ItemDetailActivity.class);
        intent.putExtra("ItemData", itemData.get(pos));
        startActivity(intent);

    }

    // Handle press on search button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.searchButton) {
            Intent intent = new Intent();
            intent.setClass(this, SearchActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}