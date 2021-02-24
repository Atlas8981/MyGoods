package com.example.mygoods.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.example.mygoods.Adapters.ListMyItemRowAdapter;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MySaveItemActivity extends AppCompatActivity {
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference itemsRef = firestore.collection("items");
    private CollectionReference usersRef = firestore.collection("users");

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference().child("profile");

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private ListView myItemListView;
    private ProgressBar progressBar;
    private ListMyItemRowAdapter listMyItemRowAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Item> itemList;
    private Handler handler;
    private View footerView;
    boolean isLoading = false;
    boolean isOutOfData = false;
    private Query next;

    private String userDocumentId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_item);
        initializeUI();

        itemList = new ArrayList<Item>();

        getDataFromFireStore();

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
                            Toast.makeText(MySaveItemActivity.this, "No Data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshViewAndData();
            }
        });

    }

    private void getDataFromFireStore(){
//        This commented function is for querying the specific item of the user
//        whereEqualTo("userid", FirebaseAuth.getInstance().getUid())
//        itemRef.whereEqualTo("userid", FirebaseAuth.getInstance().getUid()).limit(10)

//        find the right document id of Auth.getUid
//        This work when document id is not auth.getUId

        usersRef.whereEqualTo("userId",auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    userDocumentId = documentSnapshot.getId();
                    getItemIdInSaveItem();
                }
            }
        });

    }

    private void getItemIdInSaveItem(){
        usersRef.document(userDocumentId)
                .collection("saveItems")
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    itemsRef.document(documentSnapshot.getId())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Item curItem = documentSnapshot.toObject(Item.class);
                            curItem.setItemid(documentSnapshot.getId());
                            itemList.add(curItem);
                            listMyItemRowAdapter = new ListMyItemRowAdapter(MySaveItemActivity.this,itemList,false);
                            myItemListView.setAdapter(listMyItemRowAdapter);
                            myItemListView.setOnItemClickListener(listViewListener);
                            listMyItemRowAdapter.setOnItemClickListener(listViewAdapterClickListener);

                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            swipeRefreshLayout.setRefreshing(false);

                        }

                    });



                }
                // Function below was follow from the firebase documentation

                // Get Ready for the next query
                if (queryDocumentSnapshots.size()>0) {
                    DocumentSnapshot lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);

                    // Construct a new query starting at this document,
                    // get the next 10 data.
                    next = usersRef.document(userDocumentId)
                            .collection("saveItems")
                            .startAfter(lastVisible)
                            .limit(10);
                }else{
//                  if cursor cannot go further no need to query anything
//                  user can still always refresh to do the same thing
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    isOutOfData=true;
                    Toast.makeText(MySaveItemActivity.this, "No Data", Toast.LENGTH_SHORT).show();
                }
            }
        });


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
                    listMyItemRowAdapter.addListItemToAdapter((ArrayList<Item>)msg.obj);
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
//                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                            Item curItem = documentSnapshot.toObject(Item.class);
//                            curItem.setItemid(documentSnapshot.getId());
//                            anotherListItem.add(curItem);
//                        }

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            itemsRef.document(documentSnapshot.getId())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Item curItem = documentSnapshot.toObject(Item.class);
                                            curItem.setItemid(documentSnapshot.getId());
                                            anotherListItem.add(curItem);
                                        }
                                    });



                        }


                        DocumentSnapshot lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        // Construct a new query starting at this document,

                        next = usersRef.document(userDocumentId)
                                .collection("saveItems")
                                .startAfter(lastVisible)
                                .limit(10);
                    }else {
                        if (!isOutOfData) {
                            Toast.makeText(MySaveItemActivity.this, "No More Data to Load", Toast.LENGTH_SHORT).show();
                            isOutOfData = true;
                        }
                    }
                }
            });
        }

        return anotherListItem;
    }

    private void initializeUI(){
        setTitle("My Save Item(s)");

        progressBar = findViewById(R.id.progressBar);
        myItemListView = findViewById(R.id.myitemListview);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.pullToRefresh);

        swipeRefreshLayout.setColorSchemeColors(Color.argb(100,51,140,48));

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerView = layoutInflater.inflate(R.layout.footerview_myitem,null);
        handler = new Handler();

    }

    private AdapterView.OnItemClickListener listViewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            Intent intent = new Intent(MySaveItemActivity.this, MyItemDetailActivity.class);
            intent.putExtra("edit", "no");
            intent.putExtra("item", itemList.get(position));
            startActivity(intent);
        }
    };

    private ListMyItemRowAdapter.OnItemClickListener listViewAdapterClickListener = new ListMyItemRowAdapter.OnItemClickListener() {
        @Override
        public void onDeleteBtnClick(int position) {

        }

        @Override
        public void onEditBtnClick(int position) {
            Intent intent = new Intent(MySaveItemActivity.this, EditMyItemActivity.class);
            intent.putExtra("myitem", itemList.get(position));
            startActivity(intent);
        }
    };



    private void refreshViewAndData(){
        isOutOfData = false;
        itemList.clear();
        listMyItemRowAdapter = new ListMyItemRowAdapter(MySaveItemActivity.this,itemList,false);
        myItemListView.setAdapter(listMyItemRowAdapter);

        getDataFromFireStore();
    }

}