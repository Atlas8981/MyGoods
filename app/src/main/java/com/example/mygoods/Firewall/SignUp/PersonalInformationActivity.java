package com.example.mygoods.Firewall.SignUp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.David.SQLite.SQLiteManager;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.Model.RecentItem;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PersonalInformationActivity extends AppCompatActivity {

    private TextInputEditText ffirstname, llastname, uusername, pphonenumber,aaddress,eemail,ppassword,cconfirmpassword;
    private Button signUpBtn;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private ProgressBar progressBar;

    private DocumentReference documentReference;
    private WriteBatch batch = firestore.batch();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        setTitle("Personal Information");

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

    @Override
    public void onBackPressed() {

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
//        String email = eemail.getText().toString().trim();
//        String password = ppassword.getText().toString().trim();
//        String confirmpassword = cconfirmpassword.getText().toString().trim();

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
        } else if (TextUtils.isEmpty(phonenumber)) {
            pphonenumber.setError("PhoneNumber is required");
            return;
        }
//        } else if (TextUtils.isEmpty(email)) {
//            eemail.setError("Email is required");
//            return;
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//            eemail.setError("Invalid email input");
//            return;
//        }else if (TextUtils.isEmpty(password)) {
//            ppassword.setError("Password is required");
//            return;
//        } else if (TextUtils.isEmpty(confirmpassword)) {
//            cconfirmpassword.setError("Confirmpassword is required");
//            return;
//        } else if (!confirmpassword.equals(password)) {
//            cconfirmpassword.setError("Confirm Passoword is not match");
//            return;
//        }

        User signUpUser = new User(auth.getUid(), firstname, lastname, username, phonenumber, auth.getCurrentUser().getEmail(), address);
        DocumentReference documentReference = firestore.collection("users").document(auth.getUid());
        documentReference.set(signUpUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                    progressBar.setVisibility(View.INVISIBLE);
//                    Toast.makeText(PersonalInformationActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
//
//                    moveToPreference();
                uploadLocalDataToFirestore(signUpUser);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                signUpBtn.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(PersonalInformationActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveToPreference() {
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(PersonalInformationActivity.this, "Welcome", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), User_PreferenceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void uploadLocalDataToFirestore(User user){
        documentReference = firestore.collection("users").document(auth.getUid());

//        User user = new User(auth.getUid(), "Paulo", "Dybala", "dybala10", "012297777", "leomessi@gmail.com", "Phnom Penh");

        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SQLiteManager sqLiteManager = new SQLiteManager(PersonalInformationActivity.this);
                sqLiteManager.open();
                Cursor recentViewItemData   = sqLiteManager.fetch(Constant.recentViewTable); // get all recentView item id + date
                Cursor recentSearchItemData = sqLiteManager.fetch(Constant.recentSearchTable); // get all recentSearch item name + date

                // Get all recentView itemID
                if (recentViewItemData.getCount() != 0 && recentViewItemData != null) {
                    do{
                        String getItemID = recentViewItemData.getString(recentViewItemData.getColumnIndex("item_id"));
                        long getDate = Long.parseLong(recentViewItemData.getString(recentViewItemData.getColumnIndex("date")));
                        getFormatDate(getDate);

                        // Write batch to FireStore
                        RecentItem recentViewItem = new RecentItem(getItemID, new Timestamp(getDateFromString(getDate)));
                        documentReference = firestore.collection("users").document(auth.getUid().toString()).collection("recentView").document(getItemID);
                        batch.set(documentReference, recentViewItem);
                    }while (recentViewItemData.moveToNext());
                }

                // Get all recentSearch data
                if (recentSearchItemData.getCount() != 0 && recentSearchItemData != null) {
                    do{
                        String getSearchItemName = recentSearchItemData.getString(recentSearchItemData.getColumnIndex("item_id"));
                        long getDate = Long.parseLong(recentSearchItemData.getString(recentSearchItemData.getColumnIndex("date")));

                        // Write batch to FireStore
                        RecentItem recentSearch = new RecentItem(getSearchItemName, new Timestamp(getDateFromString(getDate))); // put searchItemName + date into an object
                        documentReference = firestore.collection("users").document(auth.getUid().toString()).collection("recentSearch").document(getSearchItemName);
                        batch.set(documentReference, recentSearch);
                    }while (recentSearchItemData.moveToNext());

                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //TODO: Move to PreferenceActivity + Drop table
                            sqLiteManager.dropTable();
                            moveToPreference();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PersonalInformationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //TODO: Move to PreferenceActivity + Drop table
                            sqLiteManager.dropTable();
                            moveToPreference();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PersonalInformationActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public String getFormatDate(long dateTime) {
        String formatedDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        Date mDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        formatedDate = sdf.format(mDate);
        return formatedDate;
    }

    // Method use to convert current date to String
    private  Date getDateFromString(long date){
//        final SimpleDateFormat originalFormat     = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS.SSS");
        final SimpleDateFormat targetFormat       = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        String oldDate = getFormatDate(date);
        try {
            Date newDate = targetFormat.parse(oldDate);
            return newDate ;
        } catch (java.text.ParseException e){
            return null ;
        }
    }

}