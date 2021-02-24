package com.example.mygoods.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mygoods.Adapters.ListMyItemRowAdapter;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MyItemActivity extends AppCompatActivity {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference itemRef = firestore.collection("items");
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private ListView myItemListView;
    private ProgressBar progressBar;
    private ListMyItemRowAdapter listMyItemRowAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Item> itemList;
    private Handler handler;
    private View footerView;
    boolean isLoading = false;
    boolean isOutOfData = false;

    private Query queryStatement;
    private Query next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_item);

        initializeUI();

        itemList = new ArrayList<Item>();

        getDataFromFireStore();
        

//        myItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//            }
//        });

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
                            Toast.makeText(MyItemActivity.this, "No Data", Toast.LENGTH_SHORT).show();
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

        queryStatement = itemRef
                .whereEqualTo("userid",auth.getUid())
                .limit(10)
                ;
        queryStatement.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                    Item curItem = documentSnapshot.toObject(Item.class);
                    curItem.setItemid(documentSnapshot.getId());
                    itemList.add(curItem);
                    listMyItemRowAdapter = new ListMyItemRowAdapter(MyItemActivity.this,itemList,true);
                    myItemListView.setAdapter(listMyItemRowAdapter);
                    myItemListView.setOnItemClickListener(listViewListener);
                    listMyItemRowAdapter.setOnItemClickListener(listViewAdapterClickListener);
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
//                    itemRef.whereEqualTo("userid", FirebaseAuth.getInstance().getUid())
                    next = queryStatement
                            .startAfter(lastVisible);
                }else{
//                  if cursor cannot go further no need to query anything
//                    user can still always refresh to do the same thing
                    isOutOfData=true;
                    Toast.makeText(MyItemActivity.this, "No Data", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MyItemActivity.this, "Get Data Failure", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
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
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Item curItem = documentSnapshot.toObject(Item.class);
                            curItem.setItemid(documentSnapshot.getId());
                            anotherListItem.add(curItem);
                        }


                        DocumentSnapshot lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        // Construct a new query starting at this document,

                        next = queryStatement
                                .startAfter(lastVisible);
                    }else {
                        if (!isOutOfData) {
                            Toast.makeText(MyItemActivity.this, "No More Data to Load", Toast.LENGTH_SHORT).show();
                            isOutOfData = true;
                        }
                    }
                }
            });
        }

        return anotherListItem;
    }

    private void initializeUI(){
        setTitle("My Item(s)");

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

            Intent intent = new Intent(MyItemActivity.this, MyItemDetailActivity.class);
            intent.putExtra("edit", "yes");
            intent.putExtra("item", itemList.get(position));
            startActivity(intent);
        }
    };

    private ListMyItemRowAdapter.OnItemClickListener listViewAdapterClickListener = new ListMyItemRowAdapter.OnItemClickListener() {
        @Override
        public void onDeleteBtnClick(int position) {
            activateDelete(position);
        }

        @Override
        public void onEditBtnClick(int position) {
            Intent intent = new Intent(MyItemActivity.this, EditMyItemActivity.class);
            intent.putExtra("myitem", itemList.get(position));
            startActivity(intent);
        }
    };
 
    private ProgressDialog progressDialog;
    //    Delete Specific Data from firebase Firestore
    private void deleteDataInFirestore(int position){

        itemRef.document(itemList.get(position).getItemid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    for (int i =0;i<itemList.get(position).getImages().size();i++) {
                        firebaseStorage.getReference()
                                .child("images/" +
                                        itemList.get(position)
                                                .getImages()
                                                .get(i)
                                                .getImageName()
                                ).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MyItemActivity.this, "images Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    Toast.makeText(MyItemActivity.this, "Data Deleted", Toast.LENGTH_SHORT).show();
                    itemList.remove(position);
                    listMyItemRowAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(MyItemActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }
    private void activateDelete(int position){
        progressDialog = new ProgressDialog(MyItemActivity.this);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyItemActivity.this);
        alertDialog.setTitle("Are you sure you want to delete this data ?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.setTitle("Deleting...");
                progressDialog.show();
                deleteDataInFirestore(position);

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void refreshViewAndData(){
        isOutOfData = false;
        itemList.clear();
        listMyItemRowAdapter = new ListMyItemRowAdapter(MyItemActivity.this,itemList,true);
        myItemListView.setAdapter(listMyItemRowAdapter);

        getDataFromFireStore();
    }







}