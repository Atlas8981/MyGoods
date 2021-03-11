package com.example.mygoods.David.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.David.SQLite.SQLiteManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SearchActivity extends AppCompatActivity {

    private ListView recentSearchListView;
    private SearchView searchBar;
    private ImageButton clearListViewButton;
    private CustomProgressDialog progressDialog;

    private SQLiteManager sqLiteManager;

    private RecentlySearchAdapter recentlySearchAdapter;
    private ArrayList<String>recentlySearchData = new ArrayList<String>();
    private ArrayList<Item> searchData = new ArrayList<>();
    private ArrayList<String> ownerID = new ArrayList<>();
    private ArrayList<String> ownerName = new ArrayList<>();
    private ArrayList<String> time  = new ArrayList<>();
    private ArrayList<Item> filteredData = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser currentUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (currentUser.isAnonymous()) {
            sqLiteManager = new SQLiteManager(SearchActivity.this);
        }
        setupViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRecentSearchData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser.isAnonymous()) {
            sqLiteManager.close();
        }
        recentlySearchData.clear();
        filteredData.clear();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser.isAnonymous()) {
            sqLiteManager.close();
        }
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
                    deleteAllAlertDialogSetup();
                }else{
                    Toast.makeText(SearchActivity.this, "No data to be deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recentSearchListView = (ListView) findViewById(R.id.recentSearchListView);
        recentlySearchAdapter = new RecentlySearchAdapter(this, R.layout.recentlysearch_listview, recentlySearchData);
        recentSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getSearchData(recentlySearchAdapter.getItem(i));
            }
        });
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

    private void getSearchData(String searchText) {
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
            default: searchByItemName(searchText.toLowerCase());
        }
    }

    private void searchBySubCat(String subCat) {
        db.collection(Constant.itemCollection).whereEqualTo(Constant.subCategoryField, subCat).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
        db.collection(Constant.itemCollection).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot doc : list) {
                        Item item = doc.toObject(Item.class);
                        filteredData.add(item);
                    }
                    if (filteredData.size() == list.size()) {
                        filterSearchData(itemName);
                    }
                }else{
                    progressDialog.hide();
                    Toast.makeText(SearchActivity.this, ("No item with the given name of: "+itemName), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Fail to get Document");
                Toast.makeText(SearchActivity.this, ("No item with the given name of: "+itemName), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSearchData(String text) {
        String st = replaceWhiteSpace(text).toLowerCase();
        Set<Character> charactersMatchCount = new HashSet<>();

        for (int f = 0; f<filteredData.size(); f++) {
            String data = replaceWhiteSpace(filteredData.get(f).getName()).toLowerCase();

            if (data.length() > st.length()) {
                for (int s = 0; s<data.length(); s++) {

                    for (int t = 0; t<st.length(); t++) {

                        if (st.charAt(t) == data.charAt(s)) {

                            charactersMatchCount.add(data.charAt(s));
                        }
                    } // End of third for loop
                } // End of second for loop

            } else {
                for (int s = 0; s<st.length(); s++) {

                    for (int t = 0; t<data.length(); t++) {

                        if (data.charAt(t) == st.charAt(s)) {

                            charactersMatchCount.add(st.charAt(s));
                        }
                    }  // End of third for loop
                } // End of second for loop
            } // End of length if else conditional check
            if (charactersMatchCount.size() == st.length() || charactersMatchCount.size() > st.length() || charactersMatchCount.size() == (st.length() - 1)) {
                searchData.add(filteredData.get(f));
            }
            charactersMatchCount.clear();

        } // End of FIRST for-loop
        if (searchData.isEmpty()) {
            progressDialog.hide();
            filteredData.clear();
            searchData.clear();
            Toast.makeText(SearchActivity.this, ("No Result"), Toast.LENGTH_SHORT).show();
        }else{
            addRecentSearchData(text);
        }
    }

    private String replaceWhiteSpace(String text) {
        return text.replace(" ", "");
    }

    private void getRecentSearchData() {
        recentlySearchData.clear();
        if (currentUser.isAnonymous()) {
            //TODO: Read from local database
            sqLiteManager.open();
            Cursor cursor = sqLiteManager.fetch(Constant.recentSearchTable);
            if (cursor.getCount() != 0 && cursor != null) {
                do{
                    String getItemID = cursor.getString(cursor.getColumnIndex("item_id"));
                    recentlySearchData.add(getItemID);
                }while (cursor.moveToNext());
                recentlySearchAdapter.notifyDataSetChanged();
            }else{
                return;
            }
        } else {
            db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("recentSearch").orderBy(Constant.dateField, Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        if (!list.isEmpty()) {
                            for(DocumentSnapshot doc : list) {
                                recentlySearchData.add(doc.get("itemId").toString());
                            }
                            recentlySearchAdapter.notifyDataSetChanged();
                        } else {
                            return;
                        }
                    }
                }
            });
        }
    }

    private void deleteSingleRecentSearchData(String item, int pos) {
        if (currentUser.isAnonymous()) {
            sqLiteManager.delete(Constant.recentSearchTable, item);
            recentlySearchData.remove(pos);
            recentlySearchAdapter.notifyDataSetChanged();
        } else {
            db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("recentSearch").document(
                    item
            ).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    recentlySearchData.remove(pos);
                    recentlySearchAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SearchActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteAllRecentSearchData() {
        if (currentUser.isAnonymous()) {
            sqLiteManager.deleteAllRows(Constant.recentSearchTable);
            recentlySearchData.clear();
        } else {
            for (int i = 0; i<recentlySearchData.size(); i++) {
                db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("recentSearch").document(
                        recentlySearchData.get(i)
                ).delete();
            }
        }
        recentlySearchAdapter.notifyDataSetChanged();
    }

    private void addRecentSearchData(String searchText) {

        if (currentUser.isAnonymous()) {
            sqLiteManager.insert(Constant.recentSearchTable, searchText);
            generateTimeAndSellerName();
        } else {
            DocumentReference ref = db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection(Constant.recentSearchCollection).document(searchText);

            Map<String, Object> docData = new HashMap<>();
            docData.put("date",new Timestamp(new Date()));
            docData.put("itemId", searchText);

            ref.set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    generateTimeAndSellerName();
                }
            });
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
                    if (documentSnapshot.getString(Constant.usernameField) != null) {
                        ownerName.add(documentSnapshot.getString(Constant.usernameField));
                    }
                    if (num == (ownerID.size()-1)){
                        progressDialog.hide();
                        moveToNewsFeedActivity();
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
        if (currentUser.isAnonymous()) {
            sqLiteManager.close();
        }
        startActivity(intent);
    }

    private void deleteAllAlertDialogSetup() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SearchActivity.this);
        builder1.setMessage("Are you sure you want to delete all the search history?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAllRecentSearchData();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private static class ViewHolder{
        TextView recentlySearchItemTextView;
        ImageButton clearListViewButton;
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
                viewHolder.clearListViewButton        = (ImageButton)cView.findViewById(R.id.recentSearchItemDeleteButton);
                cView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) cView.getTag();
            }
            viewHolder.recentlySearchItemTextView.setText(dataObjects.get(pos));
            viewHolder.clearListViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteSingleRecentSearchData(recentlySearchData.get(pos), pos);
                }
            });

            return cView;
        }
    }
}