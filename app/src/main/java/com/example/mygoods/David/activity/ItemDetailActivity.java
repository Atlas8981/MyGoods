package com.example.mygoods.David.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.mygoods.Activity.FullScreenImageActivity;
import com.example.mygoods.David.SQLite.SQLiteManager;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.David.others.ViewPagerAdapter;
import com.example.mygoods.David.others.collectionview.ItemDetail.SimilarItemCollectionView;
import com.example.mygoods.Firewall.WelcomeActivity;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
//    private Intent intent = getIntent();
    private SQLiteManager sqLiteManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    private Item item;
    private ArrayList<Item> similarItemData = new ArrayList<>();
    private ArrayList<Item> filterSimilarItemData = new ArrayList<>();
    private ArrayList<String> imageUrl = new ArrayList<String>();
    private ArrayList<User> users = new ArrayList<>();

    private boolean isSaved = false;

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
//    private Button addToSaveButton;
    private ToggleButton addToSaveButton;
    private ViewPagerAdapter viewPagerAdapter;
    private SimilarItemCollectionView similarItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        setupViews();
        setData();

        if (item.getItemid() != null) {
            addView();
            addToRecentView();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser.isAnonymous()) {
            if (sqLiteManager !=null){
                sqLiteManager.close();
            }
        }
    }



    private void addView() {

        Set<String> currentViewers = new HashSet<>();

        db.collection("items")
                .document(item.getItemid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Item currentItem = documentSnapshot.toObject(Item.class);

                if (currentItem != null && currentItem.getViewers() != null) {
                    currentViewers.addAll(currentItem.getViewers());
                }

                currentViewers.add(mAuth.getUid());


                List<String> toFirebase = new ArrayList<>(currentViewers);

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

        itemName = (TextView) findViewById(R.id.itemDetailItemName);
        itemPrice = (TextView) findViewById(R.id.itemDetailPrice);
        itemViewCount = (TextView) findViewById(R.id.itemDetailViewCount);
        itemDescription = (TextView) findViewById(R.id.itemDetailDescription);
        sellerImage = (ImageView) findViewById(R.id.itemDetailSellerImage);
        sellerName = (TextView) findViewById(R.id.itemDetailSellerName);
        sellerPhone = (TextView) findViewById(R.id.itemDetailSellerPhoneNumber);
        sellerAddress = (TextView) findViewById(R.id.itemDetailSellerAddress);
        viewSellerProfileButton = (Button) findViewById(R.id.itemDetailViewSellerProfileButton);
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
//        addToSaveButton = (Button) findViewById(R.id.itemDetailAddToSaveButton);
//        addToSaveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //TODO: Add Item as user's favorite
//                addToSaveItem();
//            }
//        });

        addToSaveButton = findViewById(R.id.saveItemButton);

        CheckIfItemSaved();

        addToSaveButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (mAuth.getCurrentUser()!=null){
                        if (!mAuth.getCurrentUser().isAnonymous()) {
                            addToSaveItem();

                        }else{
                            addToSaveButton.setChecked(false);
                            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                            intent.putExtra("wantToSign","true");
                            startActivity(intent);
                        }
                    }else{
                        addToSaveButton.setChecked(false);
                        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                        intent.putExtra("wantToSign","true");
                        startActivity(intent);
                    }
                } else {
                    unSaveItem();
                }
            }
        });


        // Setup horizontal RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.similarItemCollectionView);
        recyclerView.setLayoutManager(layoutManager);
        similarItemAdapter = new SimilarItemCollectionView(this, similarItemData, this);
        recyclerView.setAdapter(similarItemAdapter);
    }

    @Override
    public void onItemClickListener(int pos) {
        Intent intent = new Intent();
        intent.setClass(this, ItemDetailActivity.class);
        intent.putExtra("ItemData", similarItemData.get(pos));
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
        sellerPhone.setText("Tel: " + item.getPhone());
        sellerAddress.setText("Address: " + item.getAddress());
        getSellerProfile();
        viewPagerAdapter.notifyDataSetChanged();
        getSimilarItems();
//        TODO: Testing New Similar Item with Price (Percentage), Category, Name
//        testNewSimilarItem();
    }

    private void testNewSimilarItem() {
//        testing priceRange of +10%
        int percentage = 25;
        float priceRange = (float) (item.getPrice() + (item.getPrice()*percentage/100));

//        Get Similar Item with a certain price point with the same name
        db.collection(Constant.itemCollection)
                .whereGreaterThanOrEqualTo("price",item.getPrice())
                .whereLessThanOrEqualTo("price",priceRange)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        similarItemData.addAll(queryDocumentSnapshots.toObjects(Item.class));
                        similarItemData.remove(item);
                        similarItemAdapter.notifyDataSetChanged();
                        if (similarItemData.size()<7){
//                            Query for more data similar
                            getMoreSimilarItem();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
                Toast.makeText(ItemDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMoreSimilarItem() {
        db.collection(Constant.itemCollection)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            Item tempItem = documentSnapshot.toObject(Item.class);
                            if (tempItem!= null){
                                if (tempItem.getName().toLowerCase().contains(item.getName().toLowerCase())){

                                }
                            }
                        }
                        similarItemData.remove(item);
                        similarItemAdapter.notifyDataSetChanged();

                        if (similarItemData.size()<7){
//                            Query for more data similar

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
                Toast.makeText(ItemDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getSimilarItems() {
        float avgPrice = (float) (item.getPrice() + 50);

        db.collection(Constant.itemCollection)
                .whereGreaterThan(Constant.priceField, item.getPrice())
                .whereLessThan(Constant.priceField, avgPrice)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int count = 0;
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                ArrayList<Item> similarItemData = new ArrayList<>();
                if (!list.isEmpty()) {
                    for(DocumentSnapshot doc : list) {
                        count += 1;
                        Item curItem = doc.toObject(Item.class);
                        if (curItem != null) {
                            similarItemData.add(curItem);
                            if (count == list.size()){
                                for(int i = 0; i<similarItemData.size(); i++) {
                                    if (similarItemData.get(i).getSubCategory().equals(item.getSubCategory()) &&
                                            similarItemData.get(i).getMainCategory().equals(item.getMainCategory())) {
                                        filterSimilarItemData.add(similarItemData.get(i));
                                    }
                                }
                                filterSimilarItem(filterSimilarItemData, item.getName());
                            }
                        }
                    }
                } else {
                    return;
                }
            }
        });
    }

    private String replaceWhiteSpace(String text) {
        return text.replace(" ", "");
    }

    private void filterSimilarItem(ArrayList<Item> rawData, String currentItemName) {
        Set<Character> charactersMatchCount = new HashSet<>();
        String itemName = replaceWhiteSpace(currentItemName).toLowerCase();

        for (int f = 0; f<rawData.size(); f++) {
            String data = replaceWhiteSpace(rawData.get(f).getName()).toLowerCase();

            if (data.length() > itemName.length()) {
                for (int s = 0; s<data.length(); s++) {

                    for (int t = 0; t<itemName.length(); t++) {

                        if (itemName.charAt(t) == data.charAt(s)) {

                            charactersMatchCount.add(data.charAt(s));
                        }
                    } // End of third for loop
                } // End of second for loop

            } else {
                for (int s = 0; s<itemName.length(); s++) {

                    for (int t = 0; t<data.length(); t++) {

                        if (data.charAt(t) == itemName.charAt(s)) {

                            charactersMatchCount.add(itemName.charAt(s));
                        }
                    }  // End of third for loop
                } // End of second for loop
            } // End of length if else conditional check
            if (charactersMatchCount.size() == itemName.length() || charactersMatchCount.size() > itemName.length() || charactersMatchCount.size() == (itemName.length() - 1)) {
                similarItemData.add(rawData.get(f));
                filterSimilarItemData.remove(f);
            }
            charactersMatchCount.clear();
        } // End of FIRST for-loop

        if (similarItemData.isEmpty()) {
//            Collections.copy(similarItemData, filterSimilarItemData);
            similarItemData.addAll(filterSimilarItemData);
            similarItemAdapter.notifyDataSetChanged();
        }else if (similarItemData.size() < 3) {
            if (!filterSimilarItemData.isEmpty()) {
                for (int i = 0; i<filterSimilarItemData.size(); i++) {
                    similarItemData.add(filterSimilarItemData.get(i));
                }
            }
            similarItemAdapter.notifyDataSetChanged();
        }
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
                    if (tempUser.getImage() != null && tempUser.getImage().getImageURL() != null) {
                        Glide.with(ItemDetailActivity.this)
                                .load(tempUser.getImage().getImageURL())
                                .placeholder(R.drawable.account)
                                .centerCrop()
                                .into(sellerImage);
                    }else {
                        Glide.with(ItemDetailActivity.this)
                                .load(R.drawable.account)
                                .centerCrop()
                                .into(sellerImage);
                    }


                    sellerName.setText(tempUser.getUsername());

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

        if (currentUser.isAnonymous()) {
            //TODO: Save to local database
            sqLiteManager = new SQLiteManager(ItemDetailActivity.this);
            sqLiteManager.open();
            sqLiteManager.insert(Constant.recentViewTable,item.getItemid()); // Insert current itemID + date of view

        } else {

            DocumentReference ref = db.collection(Constant.userCollection)
                    .document(currentUser.getUid())
                    .collection("recentView")
                    .document(item.getItemid());

            Map<String, Object> recentViewItem = new HashMap<>();
            recentViewItem.put("itemID", item.getItemid());
            recentViewItem.put("date", new Timestamp(new Date()));
            ref.set(recentViewItem);
        }

        //TODO: Change documentPath to adaptive user id
//        DocumentReference ref = db
//                .collection(Constant.userCollection)
//                .document(currentUser.getUid())
//                .collection("recentView")
//                .document(item.getItemid());
//        Map<String, Object> recentViewItem = new HashMap<>();
//        recentViewItem.put("itemID", item.getItemid());
//        recentViewItem.put("date", new Timestamp(new Date()));
//
//        ref.set(recentViewItem).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    private void addToSaveItem() {
        //TODO: Change documentPath to adaptive user id
        DocumentReference ref = db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("saveItems").document(item.getItemid());
        Map<String, Object> saveItem = new HashMap<>();
        saveItem.put("itemid", item.getItemid());
        saveItem.put("date", new Date());
        ref.set(saveItem).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (!isSaved){
                    Toast.makeText(ItemDetailActivity.this, "Added to Save Items!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ItemDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unSaveItem(){
        db.collection("users").whereEqualTo("userId",mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    db.collection("users")
                            .document(documentSnapshot.getId())
                            .collection("saveItems")
                            .document(item.getItemid())
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            isSaved = false;
                            Toast.makeText(ItemDetailActivity.this, "Item remove from save item", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    private void CheckIfItemSaved(){
        List<String> saveItems = new ArrayList<>();
        db.collection("users").whereEqualTo("userId",mAuth.getUid())
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
                                        if (item.getItemid().equals(itemId)) {
                                            addToSaveButton.setChecked(true);
                                            isSaved = true;
                                        }
                                    }
                                }
                            });
                        }

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
        intent.putExtra("position",viewPager.getCurrentItem());
        startActivity(intent);
    }
} // End of class