package com.example.mygoods.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mygoods.Adapters.ListItemRowAdapter;
import com.example.mygoods.David.activity.ItemDetailActivity;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class CategoryListViewActivity extends AppCompatActivity {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference itemRef = firestore.collection("items");

    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private ListView myItemListView;
    private ProgressBar progressBar;
    private ListItemRowAdapter listItemRowAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Item> itemList;
    private List<String> userName;
    private Handler handler;
    private View footerView;
    boolean isLoading = false;
    boolean isOutOfData = false;
    private Query next;

    private String subCategory;
    private String mainCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_item);


        initializeUI();

        itemList = new ArrayList<Item>();
        userName = new ArrayList<>();

        getDataFromFireStore();

//        testingGetDataFromFireStore();



        myItemListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {


            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if (!swipeRefreshLayout.isRefreshing()) {
//                If scroll to the second last, Initiate function by calling thread
                    if (absListView.getLastVisiblePosition() == i2 - 1
                            && myItemListView.getCount() >= 0 && !isLoading && next != null && !isOutOfData) {
                        isLoading = true;
//                        Check if current number of item is bigger than 10
                        if (itemList.size()>=10) {

                            Thread thread = new ThreadGetMoreData();
                            thread.start();

                        }else{
                            isOutOfData=true;
                            Toast.makeText(CategoryListViewActivity.this, "No More Data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isOutOfData = false;
                itemList.clear();
                setUpItemRowAdapter();
                getDataFromFireStore();

            }
        });
    }

    private void testingGetDataFromFireStore() {
        getCategoryItemId();
    }

    private void getCategoryItemId() {
        itemRef.orderBy("date")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q: queryDocumentSnapshots){
                    System.out.println(q.toObject(Item.class).toString());
                }
            }
        });
    }

    private final Query queryStatement = itemRef.orderBy("date", Query.Direction.DESCENDING).limit(10);

    private void getDataFromFireStore(){
//        queryStatement.limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        queryStatement.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                    Item curItem = documentSnapshot.toObject(Item.class);
//                    if (curItem.getSubCategory().equalsIgnoreCase(subCategory)){
                    if (checkCategory(curItem)){
                        curItem.setItemid(documentSnapshot.getId());
                        itemList.add(curItem);

                        firestore.collection("users")
                                .document(curItem.getUserid())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                        User user = documentSnapshot.toObject(User.class);
                                        if (user != null && user.getUsername() != null) {
                                            userName.add(user.getUsername());
                                        } else {
                                            userName.add("Someone");
                                        }
                                        setUpItemRowAdapter();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                userName.add("Someone");
                                setUpItemRowAdapter();
                            }
                        });
                    }
                }

                progressBar.setVisibility(ProgressBar.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);


//                Function below was follow from the firebase documentation

//                Get Ready for the next query
                if (queryDocumentSnapshots.size()>0) {
                    DocumentSnapshot lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);

                    // Construct a new query starting at this document,
                    // get the next 10 data.

                    next = queryStatement.limit(10)
                            .startAfter(lastVisible);
                }else{
//                  if cursor cannot go further no need to query anything
//                    user can still always refresh to do the same thing

                    isOutOfData=true;
                    Toast.makeText(CategoryListViewActivity.this, "No Data", Toast.LENGTH_SHORT).show();
                }
                if (itemList.size()<10){
                    Thread thread = new ThreadGetMoreData();
                    thread.start();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                isOutOfData=true;
                Toast.makeText(CategoryListViewActivity.this, "There is no data in this category", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkCategory(Item item) {
        if (mainCategory==null || mainCategory.equals("")){
            return item.getSubCategory().equalsIgnoreCase(subCategory);
        }else {
            return item.getSubCategory().equalsIgnoreCase(subCategory)
                    && item.getMainCategory().equalsIgnoreCase(mainCategory);
        }
    }


    private void setUpItemRowAdapter(){
        listItemRowAdapter = new ListItemRowAdapter(CategoryListViewActivity.this,itemList,userName);
        myItemListView.setAdapter(listItemRowAdapter);
        listItemRowAdapter.notifyDataSetChanged();
        myItemListView.setOnItemClickListener(recyclerViewListener);
    }

    //    Thread to send message to initiate the data retrieval by calling handler
    public class  ThreadGetMoreData extends Thread{
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
            ArrayList<Item> items = getMoreData();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = handler.obtainMessage(1,items);
            handler.sendMessage(msg);
        }
    }


    //    Handler will handle with adding View of loading progressbar into BottomListView
    public class Handler extends android.os.Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0:
                    myItemListView.addFooterView(footerView);
                    break;
                case 1:
                    listItemRowAdapter.addListItemToAdapter((ArrayList<Item>)msg.obj,userName);
                    myItemListView.removeFooterView(footerView);
                    isLoading = false;
                    break;
                default:
                    break;
            }
        }
    }


    //    Function call when another 10 data needed from database
    private ArrayList<Item> getMoreData(){

        final ArrayList<Item> anotherListItem = new ArrayList<Item>();
        if (next != null) {
            next.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size()>0) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Item curItem = documentSnapshot.toObject(Item.class);
                            if (checkCategory(curItem)) {
                                curItem.setItemid(documentSnapshot.getId());

                                firestore.collection("users")
                                        .document(curItem.getUserid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                User user = documentSnapshot.toObject(User.class);
                                                if (user != null && user.getUsername() != null) {
                                                    userName.add(user.getUsername());
                                                } else {
                                                    userName.add("Someone");
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        userName.add("Someone");
                                    }
                                });

                                anotherListItem.add(curItem);
                            }
                        }


                        DocumentSnapshot lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        // Construct a new query starting at this document,

                        next = queryStatement
                                .startAfter(lastVisible)
                                .limit(10);
                    }else {
                        if (!isOutOfData) {
                            Toast.makeText(CategoryListViewActivity.this, "No More Data to Load", Toast.LENGTH_SHORT).show();
                            isOutOfData = true;
                        }
                    }
                }
            });
        }

        return anotherListItem;
    }

    private void initializeUI(){

        Bundle bundle = getIntent().getExtras();
        subCategory = bundle.getString("SubCategory");
        mainCategory = bundle.getString("MainCategory");
        mainCategory = mainCategory;
        if (mainCategory==null){
            mainCategory = "";
            setTitle(subCategory);
        }else{
            setTitle(mainCategory + "/" +subCategory);
        }



        progressBar = findViewById(R.id.progressBar);
        myItemListView = findViewById(R.id.myitemListview);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.pullToRefresh);

        swipeRefreshLayout.setColorSchemeColors(Color.argb(100,51,140,48));

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerView = layoutInflater.inflate(R.layout.footerview_myitem,null);
        handler = new Handler();

        Drawable divider = getResources().getDrawable( R.drawable.list_divider );
        myItemListView.setDivider(divider);
        myItemListView.setDividerHeight(100);
    }

    private AdapterView.OnItemClickListener recyclerViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

//            Intent intent = new Intent(CategoryListViewActivity.this, MyItemDetailActivity.class);
//            intent.putExtra("edit", "no");
//            intent.putExtra("item", itemList.get(position));
//            startActivity(intent);
            Intent intent = new Intent(CategoryListViewActivity.this, ItemDetailActivity.class);
            intent.putExtra("edit", "no");
            intent.putExtra("ItemData", itemList.get(position));
            startActivity(intent);

        }
    };
}