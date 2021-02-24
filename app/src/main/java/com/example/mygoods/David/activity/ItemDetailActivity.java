package com.example.mygoods.David.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.mygoods.Activity.FullScreenImageActivity;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.David.others.ViewPagerAdapter;
import com.example.mygoods.David.others.collectionview.ItemDetail.SimilarItemCollectionView;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemDetailActivity extends AppCompatActivity implements SimilarItemCollectionView.SimilarItemOnClickListener, Serializable, ViewPagerAdapter.OnViewPagerItemClick {

    private ViewPager viewPager;
    private Intent intent = getIntent();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    private Item item;
    private ArrayList<Item> similarItemArrayAdapter = new ArrayList<>();
    private ArrayList<String> imageUrl = new ArrayList<String>();
    private ArrayList<User> users = new ArrayList<>();
    private String ownerID;

    //Views
    private TextView itemName;
    private TextView itemPrice;
    private TextView itemViewCount;
    private TextView itemDescription;
    private TextView sellerName;
    private TextView sellerPhone;
    private TextView sellerAddress;
    private ImageView sellerImage;
    private Button viewSellerProfileButton;
    private Button addToSaveButton;
    private ViewPagerAdapter viewPagerAdapter;
    private SimilarItemCollectionView similarItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        setupViews();
        setData();
        addView();

//        updateViewCount();

        addToRecentView();
    }

    private void addView() {

        Set<String> currentViewers = new HashSet<>();

        db.collection("items").document(item.getItemid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Item currentItem = documentSnapshot.toObject(Item.class);

                if (currentItem != null && currentItem.getViewers() != null) {
                    currentViewers.addAll(currentItem.getViewers());
                }

                currentViewers.add(mAuth.getUid());


                List<String> toFirebase = new ArrayList<>();
                toFirebase.addAll(currentViewers);

                currentItem.setViewers(toFirebase);
                currentItem.setViews(currentViewers.size());



                db.collection("items").document(item.getItemid()).set(currentItem);
            }
        });



    }

    private void setupViews(){
        item = (Item) getIntent().getSerializableExtra("ItemData"); // get object

        // Views pager (Images slider)
        viewPager = (ViewPager) findViewById(R.id.imageViewPager);
        viewPagerAdapter = new ViewPagerAdapter(this, (ArrayList<String>) imageUrl, this);
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);

        itemName = (TextView)findViewById(R.id.itemDetailItemName);
        itemPrice = (TextView)findViewById(R.id.itemDetailPrice);
        itemViewCount = (TextView)findViewById(R.id.itemDetailViewCount);
        itemDescription = (TextView)findViewById(R.id.itemDetailDescription);
        sellerImage = (ImageView)findViewById(R.id.itemDetailSellerImage);
        sellerName = (TextView)findViewById(R.id.itemDetailSellerName);
        sellerPhone = (TextView)findViewById(R.id.itemDetailSellerPhoneNumber);
        sellerAddress = (TextView)findViewById(R.id.itemDetailSellerAddress);
        viewSellerProfileButton = (Button)findViewById(R.id.itemDetailViewSellerProfileButton);
        viewSellerProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Move to seller profile activity
                if (users.get(0) != null) {
                    moveToSellerProfileActivity();
                }else{
                    Toast.makeText(ItemDetailActivity.this, "Seller Information unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        });
        addToSaveButton = (Button)findViewById(R.id.itemDetailAddToSaveButton);
        addToSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Add Item as user's favorite
                addToSaveItem();
            }
        });

        // Setup horizontal RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.similarItemCollectionView);
        recyclerView.setLayoutManager(layoutManager);
        similarItemAdapter = new SimilarItemCollectionView(this, similarItemArrayAdapter, this);
        recyclerView.setAdapter(similarItemAdapter);
    }

    @Override
    public void onItemClickListener(int pos) {
        Intent intent = new Intent();
        intent.setClass(this, ItemDetailActivity.class);
        intent.putExtra("ItemData", similarItemArrayAdapter.get(pos));
        startActivity(intent);
    }

    private void setData(){
        itemName.setText(item.getName());
        itemPrice.setText("USD: "+item.getPrice());
        itemViewCount.setText("Views: "+item.getViews());
        itemDescription.setText(item.getDescription());
        for (int i = 0; i<item.getImages().size(); i++){
            imageUrl.add(item.getImages().get(i).getImageURL());
        }
        getSellerProfile();
        viewPagerAdapter.notifyDataSetChanged();
        getSimilarItems();
    }

    private void getSimilarItems() {
        float avgPrice = (float) (item.getPrice() + 50);
        db.collection(Constant.itemCollection).whereGreaterThan(Constant.priceField, item.getPrice()).whereLessThan(Constant.priceField, avgPrice).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int count = 0;
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                ArrayList<Item> similarItemData = new ArrayList<>();
                for(DocumentSnapshot doc : list) {
                    count += 1;
                    Item item = doc.toObject(Item.class);
                    similarItemData.add(item);
                    if (count == list.size()){
                        for(int i = 0; i<similarItemData.size(); i++) {
                            if (similarItemData.get(i).getSubCategory().equals(ItemDetailActivity.this.item.getSubCategory())) {
                                similarItemArrayAdapter.add(similarItemData.get(i));
                            }
                        }
                        similarItemAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void getSellerProfile() {

        db.collection(Constant.userCollection)
                .document(item.getUserid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User tempUser = documentSnapshot.toObject(User.class);
                users.add(tempUser);

                if (tempUser != null) {
                    Glide.with(ItemDetailActivity.this)
                            .load(tempUser.getImage().getImageURL())
                            .centerCrop()
                            .into(sellerImage);


                    sellerName.setText(tempUser.getUsername());
                    sellerPhone.setText("Tel: " + tempUser.getPhoneNumber());
                    sellerAddress.setText("Address: " + tempUser.getAddress());
                }
            }
        });
    }

    private void updateViewCount() {
        //TODO: Update view with guest's id also
        FirebaseUser currentUser;
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Set<String> userID = new HashSet<String>();

        // 1. Get all the viewer size
        int size = item.getViewers().size();
        // 2. Loop and add all the ID into set
        for (int i = 0; i<size; i++) {
            userID.add(item.getViewers().get(i));
        }

        // 3. When finish, add currentUID into Set
        userID.add(currentUser.getUid().toString());
        ArrayList<String> id = new ArrayList<>(userID);

        // 4. Update data with size to FireStore
        DocumentReference ref = db.collection(Constant.itemCollection).document(item.getItemid());
        ref.update(Constant.viewerField, id);
        ref.update(Constant.viewField, (id.size()-1));
    }

    private void addToRecentView() {
        //TODO: Change documentPath to adaptive user id
        DocumentReference ref = db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("recentView").document(item.getItemid());
        Map<String, Object> recentViewItem = new HashMap<>();
        recentViewItem.put("itemID", item.getItemid());
        recentViewItem.put("date", new Timestamp(new Date()));
        ref.set(recentViewItem);
    }

    private void addToSaveItem() {
        //TODO: Change documentPath to adaptive user id
        DocumentReference ref = db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("saveItems").document(item.getItemid());
        Map<String, Object> saveItem = new HashMap<>();
        saveItem.put("itemid", item.getItemid());
        ref.set(saveItem).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ItemDetailActivity.this, "Added to Favorite!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ItemDetailActivity.this, "Cannot Add to Favorite!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void moveToSellerProfileActivity() {
        Intent intent = new Intent();
        intent.setClass(this, SellerProfileActivity.class);
        intent.putExtra("sellerData", users);
        startActivity(intent);
    }

    @Override
    public void onViewPagerItemClickListener() {
//        Intent intent = new Intent();
//        intent.setClass(this, DetailImageActivity.class);
//        intent.putExtra("ItemDetailImageURL", imageUrl);
//        startActivity(intent);

        Intent intent = new Intent();
        intent.setClass(this, FullScreenImageActivity.class);
        intent.putExtra("images", (Serializable) item.getImages());
        startActivity(intent);
    }
} // End of class