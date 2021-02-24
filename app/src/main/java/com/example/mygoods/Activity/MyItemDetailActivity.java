package com.example.mygoods.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.mygoods.Adapters.ImageAdapter;
import com.example.mygoods.Adapters.RecyclerSimilarItemAdapter;
import com.example.mygoods.David.activity.SellerProfileActivity;
import com.example.mygoods.Firewall.WelcomeActivity;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyItemDetailActivity extends AppCompatActivity {

    private TextView nameText,priceText,descriptionText,addressText,categoryText,phoneText,itemIdText, userName;
    private TextView similarText;
    private Button sellerButton;
    private RecyclerView similarItemsRecyclerView;
    private ToggleButton saveButton;
    private ImageView userImage;
    private ViewPager viewPager;
    private Bundle bundle;
    private Item mitem;
    private ArrayList<User> sellers;
    private DotsIndicator dotsIndicator;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private String edit;

    private List<Item> similarItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_item_detail);
        bundle = getIntent().getExtras();
        mitem = new Item();
        initializeUI();
        putDataIntoViews();



        edit = getIntent().getExtras().getString("edit");
        if (edit.equalsIgnoreCase("yes")) {
            similarText.setVisibility(View.INVISIBLE);
        }else{
            settingUpSimilarItem();
        }

        addView();

        getSellerData();
    }

    private void getSellerData() {

        sellers = new ArrayList<>();

        db.collection("users")
                .document(mitem.getUserid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User tempUser = documentSnapshot.toObject(User.class);

                        sellers.add(tempUser);
                    }
                });
    }


    private void addView() {
        if (!edit.equalsIgnoreCase("yes")) {
//            int view = mitem.getViews() + 1;
//            mitem.setViews(view);
            Set<String> currentViewers = new HashSet<>();

            db.collection("items")
                    .document(mitem.getItemid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    Item currentItem = documentSnapshot.toObject(Item.class);

                    if (currentItem != null && currentItem.getViewers() != null) {
                        currentViewers.addAll(currentItem.getViewers());
                    }

                    currentViewers.add(auth.getUid());


                    List<String> toFirebase = new ArrayList<>();
                    toFirebase.addAll(currentViewers);

                    currentItem.setViewers(toFirebase);
                    currentItem.setViews(currentViewers.size());



                    db.collection("items").document(mitem.getItemid()).set(currentItem);
                }
            });

        }

    }

    private void settingUpSimilarItem() {

        similarItemsRecyclerView = findViewById(R.id.similarItemsRecyclerView);
        similarItems = new ArrayList<>();
        RecyclerSimilarItemAdapter recyclerAdapter = new RecyclerSimilarItemAdapter(similarItems);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        similarItemsRecyclerView.setLayoutManager(linearLayoutManager);
        similarItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        similarItemsRecyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setOnItemClickListener(onItemClickListener);



        Query query = db.collection("items")
                .whereEqualTo("subCategory",mitem.getSubCategory())
                .limit(6);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                    Item current = documentSnapshot.toObject(Item.class);
                    current.setItemid(documentSnapshot.getId());

                    if (!current.getItemid().equals(mitem.getItemid())) {
                        similarItems.add(current);
                        RecyclerSimilarItemAdapter recyclerAdapter = new RecyclerSimilarItemAdapter(similarItems);
                        similarItemsRecyclerView.setAdapter(recyclerAdapter);
                        recyclerAdapter.setOnItemClickListener(onItemClickListener);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.fillInStackTrace();
            }
        });


    }

    private RecyclerSimilarItemAdapter.OnItemClickListener onItemClickListener = new RecyclerSimilarItemAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            Intent intent = new Intent(getApplicationContext(), MyItemDetailActivity.class);
            intent.putExtra("edit", "no");
            intent.putExtra("item", similarItems.get(position));
            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_menu_item, menu);
        String edit = getIntent().getExtras().getString("edit");
        if (edit.equalsIgnoreCase("yes")) {
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.editPost) {
            Intent intent = new Intent(MyItemDetailActivity.this,EditMyItemActivity.class);
            intent.putExtra("myitem",mitem);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeUI(){
        setTitle("Item Detail");

        similarText = findViewById(R.id.similarText);
        nameText = findViewById(R.id.nameText);
        priceText = findViewById(R.id.priceText);
        descriptionText = findViewById(R.id.descriptionText);
        addressText = findViewById(R.id.addressText);
        categoryText = findViewById(R.id.categoryText);
        phoneText = findViewById(R.id.phoneText);
        itemIdText = findViewById(R.id.itemIdText);
        dotsIndicator = findViewById(R.id.dotIndicator);
        userImage = findViewById(R.id.sellerImageView);
        userName = findViewById(R.id.sellerUsername);
        viewPager = findViewById(R.id.viewPager);
        saveButton = findViewById(R.id.saveButton);
        sellerButton = findViewById(R.id.sellerProfileButton);

        if (bundle.getString("edit").equalsIgnoreCase("yes")){
            saveButton.setVisibility(View.INVISIBLE);
        }else {
            saveButton.setVisibility(View.VISIBLE);
        }

        checkIfItemSaved();



        saveButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (!auth.getCurrentUser().getEmail().isEmpty()) {
                        addSaveItemToUser(mitem.getItemid());
                    }else{
                        saveButton.setChecked(false);
                        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                        intent.putExtra("wantToSign","true");
                        startActivity(intent);
                    }
                } else {
                    unSaveItem(mitem.getItemid());
                }
            }
        });

        sellerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SellerProfileActivity.class);
                intent.putExtra("sellerData", sellers);
                startActivity(intent);
            }
        });
    }


    private void putDataIntoViews(){

        if (bundle != null) {
            mitem = (Item) bundle.get("item");

            nameText.setText(mitem.getName());
            phoneText.setText(mitem.getPhone());
//            amountText.setText("Amount: " + String.valueOf(mitem.getAmount()));
            priceText.setText("USD $ "+String.valueOf(mitem.getPrice()));
            addressText.setText(mitem.getAddress());
            descriptionText.setText(mitem.getDescription());

            if (mitem.getSubCategory()!=null && mitem.getMainCategory() !=null) {
                categoryText.setText(mitem.getMainCategory() + " / " + mitem.getSubCategory());
            }
            itemIdText.setText(mitem.getItemid());


            ImageAdapter imageAdapter = new ImageAdapter(this,mitem.getImages(),false);
            viewPager.setAdapter(imageAdapter);
            dotsIndicator.setViewPager(viewPager);



            imageAdapter.setOnClickListener(new ImageAdapter.OnClickListener() {
                @Override
                public void OnItemClick(int position) {
                    Intent intent = new Intent(MyItemDetailActivity.this, FullScreenImageActivity.class);
                    intent.putExtra("images", (Serializable) mitem.getImages());
                    intent.putExtra("position",position);
                    startActivity(intent);
                }
            });

            userName.setText("Posted By Someone");
            Glide.with(MyItemDetailActivity.this)
                    .load(R.drawable.account)
                    .into(userImage);


            putUserDate();

        }
    }

    private void putUserDate(){
        db.collection("users")
                .document(mitem.getUserid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (user.getUsername() != null) {
                                userName.setText(user.getUsername());
                            }
                            if (user.getImage() != null && user.getImage().getImageURL() != null) {

                                Glide.with(MyItemDetailActivity.this)
                                        .load(user.getImage().getImageURL())
                                        .centerCrop()
                                        .placeholder(R.drawable.account)
                                        .into(userImage);
                            }
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    private void checkIfItemSaved(){
        List<String> saveItems = new ArrayList<>();
        db.collection("users").whereEqualTo("userId",auth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            db.collection("users")
                                    .document(documentSnapshot.getId())
                                    .collection("saveItems")
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                                        saveItems.add(documentSnapshot.get("itemid").toString());
                                    }
                                    for (String itemId:saveItems) {
                                        if (mitem.getItemid().equals(itemId)){
                                            saveButton.setChecked(true);
                                        }
                                    }
                                }
                            });
                        }

                    }
                });


    }

    private void addSaveItemToUser(String itemid){

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("itemid",itemid);
        CollectionReference collectionReference;
        db.collection("users")
                .whereEqualTo("userId",auth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    db.collection("users")
                            .document(documentSnapshot.getId())
                            .collection("saveItems")
                            .document(itemid)
                            .set(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }

            }
        });
    }

    private void unSaveItem(String itemid){
        db.collection("users").whereEqualTo("userId",auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    db.collection("users")
                            .document(documentSnapshot.getId())
                            .collection("saveItems")
                            .document(itemid)
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }

            }
        });
    }

}