package com.example.mygoods.David.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.David.others.CustomProgressDialog;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NewsFeedActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ListView baseListView;
    private CustomAdapter customAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CustomProgressDialog progressDialog;

    private Bundle dataBundle;
    private Intent newsFeedIntent;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int deletedItem = 0;
    private int noTopViewItem = 0;
    private int noOwner = 0;
    private String getCategory;
    private String intentFromHome;
    private String intentFromCategory;
    private String currentUserID;
    //private Set<ItemData> rawData = new HashSet<>();
    private ArrayList<Item> newsFeedData = new ArrayList<Item>();
    private ArrayList<String> ownerID = new ArrayList<String>();
    private ArrayList<String> ownerName = new ArrayList<>();
    private ArrayList<String> time = new ArrayList<String>();
    private ArrayList<String> preferences;
    private ArrayList<String> recentlyViewItemID = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);

        setToolBarTitle();
        setupViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    private void setupViews() {
        // Progress Dialog
        progressDialog = new CustomProgressDialog(NewsFeedActivity.this);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        progressDialog.show(); // Run progress dialog
        // ListView
        baseListView = (ListView) findViewById(R.id.baseListView);
        baseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                moveToDetailActivity(i);
            }
        });

        // setAdapter
        customAdapter = new CustomAdapter(this, R.layout.custom_newsfeed, newsFeedData);
        baseListView.setAdapter(customAdapter);

        // swipeLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: Disable user interaction when refresh
                if (intentFromHome != null) {
                    if (intentFromHome.equals(Constant.intentFromTrending)) {
                        prepareForRefresh();
                        getTrendingItem();
                    } else if (intentFromHome.equals(Constant.intentFromRecommendation)) {
                        prepareForRefresh();
                        getRecommendationItem();
                    } else {
                        prepareForRefresh();
                        getRecentViewItemID();
                    }
                }else if (intentFromCategory != null) {
                    prepareForRefresh();
                    getSubCategoryData();
                }else{
                    //TODO: Disable swipe to refresh
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                }

            }
        });
    }

    private void setToolBarTitle() {
        newsFeedIntent = getIntent();
        intentFromHome = newsFeedIntent.getStringExtra(Constant.titleIntentFromHome);
        intentFromCategory = newsFeedIntent.getStringExtra(Constant.intentFromSubCat);
        currentUserID = newsFeedIntent.getStringExtra(Constant.userIDFromHomeActivity);

        if (intentFromHome != null) {
            if (intentFromHome.equals(Constant.intentFromTrending)) {
                getTrendingItem();
            } else if (intentFromHome.equals(Constant.intentFromRecommendation)) {
                getRecommendationItem();
            } else {
                getRecentViewItemID();
            }
            NewsFeedActivity.this.setTitle(intentFromHome);

        }else if (intentFromCategory != null){
            // Get items for sub category
            NewsFeedActivity.this.setTitle(intentFromCategory);
            getSubCategoryData();
        }else{
            NewsFeedActivity.this.setTitle("Results");
            ArrayList<Item> searchData = (ArrayList<Item>) getIntent().getSerializableExtra("SearchData");
            time = (ArrayList<String>) getIntent().getSerializableExtra("SearchDataItemDuration");
            ownerName = (ArrayList<String>) getIntent().getSerializableExtra("SearchDataItemOwnerName");
            newsFeedData = searchData;
            Collections.sort(newsFeedData);
            if (progressDialog != null) {
                progressDialog.hide();
            }else{
                final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    progressDialog.hide();
                }
            }, 500);
            }

        }
    }

    private void getSubCategoryData(){
        dataBundle = getIntent().getExtras();
        getCategory = dataBundle.getString(Constant.intentFromSubCat).toLowerCase();
        String getMainCategory = dataBundle.getString(Constant.mainCatTitle).toLowerCase();

        db.collection(Constant.itemCollection).orderBy(Constant.dateField, Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    int notMatchCount = 0;

                    for(DocumentSnapshot doc : list) {
                        //TODO: Check if MainCat equal each other
                        if(doc.get(Constant.subCategoryField).equals(getCategory) && doc.get(Constant.mainCategoryField).equals(getMainCategory)) {
                            Item item = doc.toObject(Item.class);



                            newsFeedData.add(item);
                        }else{
                            notMatchCount += 1;
                            if (notMatchCount == list.size()) {
                                progressDialog.hide();
                                Toast.makeText(NewsFeedActivity.this, "There are no data at the moment, try again later!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    generateTimeAndSellerName();
                }
            }
        });
    }

    //////////////////////////////
    private void getTrendingItem() {
        //TODO: Decide how many views up
        int views = 0;
        db.collection(Constant.itemCollection)
                .whereGreaterThan(Constant.viewField, views)
                .orderBy(Constant.viewField, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                if (!list.isEmpty()) {
                    for(DocumentSnapshot doc : list) {
                        Item trending = doc.toObject(Item.class);

                        trending.setItemid(doc.getId());

                        newsFeedData.add(trending);
                        if (newsFeedData.size() == list.size()) {
                            generateTimeAndSellerName();
                        }
                    }
                }else{
                    progressDialog.hide();
                    Toast.makeText(NewsFeedActivity.this, "There are no data at the moment, try again later!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getRecentViewItemID() {
        db.collection(Constant.userCollection).document(currentUserID).collection("recentView").orderBy("date", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        recentlyViewItemID.add(document.getId());
                        if (recentlyViewItemID.size() == task.getResult().size()) {
                            getRecentViewItem();
                        }
                    }
                }else{
                    Toast.makeText(NewsFeedActivity.this, "Error getting document", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                }
            }
        });
    }

    private void getRecentViewItem() {

        //TODO: Item not sort by date at this part

        for (int i = 0; i<recentlyViewItemID.size(); i++) {
            int notCount = recentlyViewItemID.size();
            db.collection(Constant.itemCollection)
                    .document(recentlyViewItemID.get(i))
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Item item = documentSnapshot.toObject(Item.class);
                    if (item != null) {
                        newsFeedData.add(item);
                        if (newsFeedData.size() == (recentlyViewItemID.size() - deletedItem)) {
                            System.out.println("NF RECENTLY VIEWWWWWWWWWWWWWWWWWWWW");
                            generateTimeAndSellerName();
                        }
                    }else{
                        deletedItem += 1;
                    }
                }
            });
        }
    }

    private void getRecommendationItem() {
        preferences = (ArrayList<String>) getIntent().getSerializableExtra(Constant.dataIntentFromHome);

        System.out.println(preferences.size());

        if (preferences.size()>0) {
            for (int i = 0; i < preferences.size(); i++) {
                int count = i;
                System.out.println(Constant.capitalize(preferences.get(i)));
                db.collection(Constant.itemCollection)
                        .whereEqualTo(Constant.subCategoryField, Constant.capitalize(preferences.get(i)))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                // Check if list is empty, else start finding top view item of that category

                                if (!list.isEmpty()) {
                                    ArrayList<Item> rawRecommendationData = new ArrayList<>();
                                    int documentSize = 0;
                                    // Get all the data first before sorting for top view item
                                    for (DocumentSnapshot doc : list) {
                                        documentSize += 1;
                                        Item trending = doc.toObject(Item.class);

                                        rawRecommendationData.add(trending);
                                        if (documentSize == list.size()) {
                                            Collections.sort(rawRecommendationData); // Sort for top view item and add it into newsFeedData
                                            newsFeedData.add(rawRecommendationData.get(0));
                                            // Handle Firebase Async: generateTimeAndSellerName() will be called after newsFeedData stop getting any data input
                                            if (newsFeedData.size() == (5 - noTopViewItem)) {
                                                generateTimeAndSellerName();
                                            }
                                        }
                                    }
                                } else {
                                    progressDialog.hide();
                                    Toast.makeText(NewsFeedActivity.this, "No Data ?", Toast.LENGTH_SHORT).show();
                                    noTopViewItem += 1; // Count the number of category that does not has item so that we can use this variable to handle firebase async
                                }
                            }
                        });
            }
        }

    }
    //////////////////////////////

    private void moveToDetailActivity(int pos){
        Intent intent = new Intent();
        intent.setClass(this, ItemDetailActivity.class);
        intent.putExtra("ItemData", newsFeedData.get(pos));
        startActivity(intent);
    }

    private void prepareForRefresh() {

        recentlyViewItemID.clear();
        newsFeedData.clear();
        time.clear();
        ownerID.clear();
        ownerName.clear();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void generateTimeAndSellerName() {
        if (!newsFeedData.isEmpty()) {
            for (int i = 0; i<newsFeedData.size(); i++) {
                ownerID.add(newsFeedData.get(i).getUserid());
            }

            for (int o = 0; o<ownerID.size(); o++) {
                // Convert date
                String duration = calculateDate(newsFeedData.get(o).getDate());
                time.add(duration);

                db.collection(Constant.userCollection)
                        .document(ownerID.get(o))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        String owner;
//                        String owner = documentSnapshot.getString(Constant.usernameField);
                        if (user != null) {

                            owner = user.getUsername();
                        }else{
                            owner = "Someone";

                        }

                        if (owner != null) {
                            ownerName.add(owner);
                            if (ownerName.size() == (ownerID.size() - noOwner)) {
                                progressDialog.hide();
                                customAdapter.notifyDataSetChanged();
                            }
                        }else{
                            ownerName.add("Someone");
                            noOwner += 1;
                        }
                    }
                });
            }
        }else{
            System.out.println("CANNOT GENERATEEEEEEEEE");
            System.out.println("NEWSFEED SIZEEEEE: "+newsFeedData.size());
        }
    }

    private String calculateDate(Date itemDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dateOfItem = itemDate;
        dateFormat.format(dateOfItem);

        Date currentDate = new Date();
        String timeEnd = " minute(s) ";
        long date = (currentDate.getTime() - dateOfItem.getTime()) / 60000;
        if (date > 0) {
            if (date >= 60) {
                date = date / 60;
                timeEnd = " hour(s) ";
                if (date >= 24) {
                    date = date/24;
                    timeEnd = " day(s) ";
                }
            }
        } else {
            date = 1;
        }
        return date + timeEnd;
    }

    //******************
    //Override method from SwipeRefreshLayout.OnRefreshListener class
    //******************
    @Override
    public void onRefresh() {

    }

    private static class ViewHolder{
        ImageView itemImage;
        TextView itemName;
        TextView itemPrice;
        TextView itemOwner;
        TextView itemDuration;
        TextView itemViewCount;
    }

    public class CustomAdapter extends ArrayAdapter<Item>{

        private Context mContext;
        private int mResource;
        private ArrayList<Item> dataObjects;

        public CustomAdapter(Context context, int resource, ArrayList<Item>dataObjects) {
            super(context, resource, dataObjects);
            this.mContext = context;
            this.mResource = resource;
            this.dataObjects = dataObjects;
        }

        public View getView(int pos, View cView, ViewGroup parent){
            ViewHolder viewHolder = new ViewHolder();
            if(cView == null){
                LayoutInflater inflater = LayoutInflater.from(mContext);
                cView = inflater.inflate(R.layout.custom_newsfeed,parent,false);
                viewHolder.itemName = (TextView)cView.findViewById(R.id.titleTextView);
                viewHolder.itemPrice = (TextView)cView.findViewById(R.id.priceTextView);
                viewHolder.itemOwner = (TextView)cView.findViewById(R.id.postedByTextView);
                viewHolder.itemDuration = (TextView)cView.findViewById(R.id.durationTextView);
                viewHolder.itemViewCount = (TextView)cView.findViewById(R.id.viewCountTextView);
                viewHolder.itemImage = (ImageView)cView.findViewById(R.id.itemImageView);
                cView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) cView.getTag();
            }

            viewHolder.itemName.setText(dataObjects.get(pos).getName());
            viewHolder.itemPrice.setText("USD "+dataObjects.get(pos).getPrice());

//            The problem is that if an item have no owner then it will crash
//            System.out.println("Object Size : " + dataObjects.size());
//            System.out.println("Username array size : " + ownerName.size());

//            System.out.println("Item Name : " + dataObjects.get(pos).getName() + " posted by " + ownerName.get(pos));

            if (pos<ownerName.size()) {
                viewHolder.itemOwner.setText("Posted by: " + ownerName.get(pos));
            }

            viewHolder.itemDuration.setText(time.get(pos));
            viewHolder.itemViewCount.setText("View: "+dataObjects.get(pos).getViews());
            viewHolder.itemImage.setImageResource(R.drawable.plastic);
            Glide.with(mContext).load(dataObjects.get(pos).getImages().get(0).getImageURL()).centerCrop().placeholder(R.drawable.loading).into(viewHolder.itemImage);

            return cView;
        }
    }
} // End of class