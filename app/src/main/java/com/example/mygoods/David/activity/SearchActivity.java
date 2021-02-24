package com.example.mygoods.David.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.David.others.Constant;
import com.example.mygoods.David.others.CustomProgressDialog;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchActivity extends AppCompatActivity {

    private ImageButton clearListViewButton;
    private ListView recentSearchListView;
    private SearchView searchBar;
    private CustomProgressDialog progressDialog;

    private RecentlySearchAdapter recentlySearchAdapter;
    private ArrayList<String>recentlySearchData = new ArrayList<String>();
    private ArrayList<String> recentlySearchDataDocumentID = new ArrayList<>();
    private ArrayList<Item> searchData = new ArrayList<>();
    private ArrayList<String> ownerID = new ArrayList<>();
    private ArrayList<String> ownerName = new ArrayList<>();
    private ArrayList<String> time  = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupViews();
        getRecentSearchData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getRecentSearchData();
        searchData.clear();
        ownerID.clear();
        ownerName.clear();
        time.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    private void setupViews() {
        progressDialog = new CustomProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        searchBar = (SearchView) findViewById(R.id.recentSearchSearchBar);
        clearListViewButton = (ImageButton) findViewById(R.id.clearRecentSearchButton);
        clearListViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recentlySearchData.isEmpty()) {
                    //deleteRecentSearchItem();
                    deleteAllRecentSearchItem();
                }else{
                    Toast.makeText(SearchActivity.this, "No data to be deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recentSearchListView = (ListView) findViewById(R.id.recentSearchListView);
        recentlySearchAdapter = new RecentlySearchAdapter(this, R.layout.recentlysearch_listview, recentlySearchData);
        recentSearchListView.setAdapter(recentlySearchAdapter);

        searchBar.setQueryHint("Search...");
        searchBar.setIconified(false);
        searchBar.setIconifiedByDefault(false);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getSearchData(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void addRecentSearchData(String searchText) {
        DocumentReference ref = db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection(Constant.recentSearchCollection).document();

        Map<String, Object> docData = new HashMap<>();
        docData.put("id", ref.getId());
        docData.put("date",new Timestamp(new Date()));
        docData.put("item", searchText);

        ref.set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                generateTimeAndSellerName();
            }
        });
    }

    private void getSearchData(String searchText) {
        //TODO: E/WindowManager: android.view.WindowLeaked: Activity com.david.mygoods.activity.SearchActivity has leaked window DecorView@f6e14ab[SearchActivity] that was originally added here
        progressDialog.show();
        switch (searchText.toLowerCase()) {
            case "phone": searchBySubCat(searchText.toLowerCase());
                break;
            case "desktop":
            case "pc":
                searchBySubCat("desktop");
                break;
            case "laptop": searchBySubCat(searchText.toLowerCase());
                break;
            case "table":
            case "desk":
                searchBySubCat("table & desk");
                break;
            case "chair":
            case "sofa":
                searchBySubCat("chair & sofa");
                break;
            case "household": searchBySubCat("household items");
                break;
            case "car": searchBySubCat(searchText.toLowerCase());
                break;
            case "motorbike":
            case "motor":
                searchBySubCat(" motorbike");
                break;
            case "bike": searchBySubCat(searchText.toLowerCase());
                break;
            default: searchByItemName(searchText);
        }
    }

    private void searchBySubCat(String subCat) {
        db.collection(Constant.itemCollection)
                .whereEqualTo(Constant.subCategoryField, subCat)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot doc : list) {
                        Item item = doc.toObject(Item.class);
                        searchData.add(item);
                    }
                    if (searchData.size() == list.size()) {
                        addRecentSearchData(subCat);
                    }
                }else{
                    progressDialog.hide();
                    Toast.makeText(SearchActivity.this, ("There are no item with the name: "+subCat), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchByItemName(String itemName) {

        db.collection(Constant.itemCollection)
                .whereEqualTo(Constant.itemNameField, itemName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot doc : list) {
                        Item item = doc.toObject(Item.class);
                        searchData.add(item);
                    }
                    if (searchData.size() == list.size()) {
                        addRecentSearchData(itemName);
                    }
                }else{
                    progressDialog.hide();
                    Toast.makeText(SearchActivity.this, ("No item with the name: "+itemName), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getRecentSearchData() {
        db.collection(Constant.userCollection)
                .document(currentUser.getUid())
                .collection("recentSearch")
                .orderBy(Constant.dateField, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot doc : list) {
                        recentlySearchDataDocumentID.add(doc.get("id").toString());
                        recentlySearchData.add(doc.get("item").toString());
                    }
                    recentlySearchAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void deleteRecentSearchItem() {
        //TODO: Pop up dialog ask whether user want to delete all
        db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("recentSearch").document(
                recentlySearchDataDocumentID.get(0)
        ).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                recentlySearchData.remove(0);
                recentlySearchDataDocumentID.remove(0);
                recentlySearchAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SearchActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAllRecentSearchItem() {
        for (int i = 0; i<recentlySearchDataDocumentID.size(); i++) {
            deleteRecentSearchItem();
        }
    }

    private void generateTimeAndSellerName() {
        for (int i = 0; i<searchData.size(); i++) {
            ownerID.add(searchData.get(i).getUserid());
        }

        for (int o = 0; o<ownerID.size(); o++) {
            // Convert data
            String duration = calculateDate(searchData.get(o).getDate());
            time.add(duration);

            int num = o;
            db.collection(Constant.userCollection).document(ownerID.get(o)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //searchData.get(num).setOwner(documentSnapshot.getString(Constant.usernameField));
                    ownerName.add(documentSnapshot.getString(Constant.usernameField));
                    if (num == (ownerID.size()-1)){
                        progressDialog.hide();
                        moveToNewsFeedActivity();
                    }else{
                        System.out.println("Please Waitttttttttt");
                    }
                }
            });
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

    private void moveToNewsFeedActivity() {
        Intent intent = new Intent();
        intent.setClass(this, NewsFeedActivity.class);
        intent.putExtra("SearchData", searchData);
        intent.putExtra("SearchDataItemDuration", time);
        intent.putExtra("SearchDataItemOwnerName", ownerName);
        startActivity(intent);
    }

    private static class ViewHolder{
        TextView recentlySearchItemTextView;
    }

    private class RecentlySearchAdapter extends ArrayAdapter<String> {

        private Context mContext;
        private int mResource;
        private ArrayList<String> dataObjects;

        public RecentlySearchAdapter(Context context, int resource, ArrayList<String>dataObjects) {
            super(context, resource, dataObjects);
            this.mContext = context;
            this.mResource = resource;
            this.dataObjects = dataObjects;
        }

        public View getView(int pos, View cView, ViewGroup parent){
            ViewHolder viewHolder = new ViewHolder();
            if(cView == null){
                LayoutInflater inflater = LayoutInflater.from(mContext);
                cView = inflater.inflate(R.layout.recentlysearch_listview,parent,false);
                viewHolder.recentlySearchItemTextView = (TextView)cView.findViewById(R.id.recentlySearchItemTextView);
                cView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) cView.getTag();
            }

            viewHolder.recentlySearchItemTextView.setText(dataObjects.get(pos));

            return cView;
        }
    }
}