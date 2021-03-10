package com.example.mygoods.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygoods.David.SQLite.SQLiteManager;
import com.example.mygoods.David.activity.NewsFeedActivity;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.David.others.collectionview.Home.PreferenceCollectionView;
import com.example.mygoods.David.others.collectionview.Home.RecentViewCollectionView;
import com.example.mygoods.David.others.collectionview.Home.TrendingCollectionView;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class HomeFragment extends Fragment implements TrendingCollectionView.TrendingOnItemClick, RecentViewCollectionView.RecentViewOnItemClick, PreferenceCollectionView.PreferenceOnItemClick {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context homeFragmentContext;
    private View v;
    private HomeFragmentInterface callback;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    private ArrayList<String> preferences;
    private ArrayList<Item> trendingData = new ArrayList<Item>();
    private ArrayList<Item> recentlyViewData = new ArrayList<Item>();
    private ArrayList<Item> recommendationData = new ArrayList<Item>();
    private ArrayList<String> itemID = new ArrayList<>();
    private TrendingCollectionView trendingAdapter;
    private RecentViewCollectionView recentViewAdapter;
    private PreferenceCollectionView prefAdapter;
    private String currentUserID;
    private int deletedItem = 0;
    private int noTopViewItem = 0;
    private SQLiteManager sqLiteManager; // ADD THIS

    private Button trendingViewAllButton;
    private Button recentlyViewViewAllButton;
    private Button recommendationViewAllButton;


    public interface HomeFragmentInterface {
        public void onViewAllButtonClickListener(Class destinationActivity, ArrayList<String> data, String title, String userID);
        public void onCollectionViewItemClickListener(int pos, ArrayList<Item> itemData);
    }

    @Override
    public void onPreferenceItemClickListener(int pos) {
        callback.onCollectionViewItemClickListener(pos, recommendationData);
    }

    @Override
    public void onRecentViewItemClickListener(int pos) {
        callback.onCollectionViewItemClickListener(pos, recentlyViewData);
    }

    @Override
    public void onTrendingItemClickListener(int pos) {
        callback.onCollectionViewItemClickListener(pos, trendingData);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        homeFragmentContext = context;
        if (context instanceof HomeFragmentInterface) {
            callback = (HomeFragmentInterface) context; // attach listener to hosting activity
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement HomeFragment.OnCategorySelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_home, container, false);


        setupFirebase();
        viewAllButtonAction();

        return v;
    }

    // ADD THIS
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sqLiteManager!=null){
            sqLiteManager.close();
        }
    }

    private void setupFirebase() {

        if (mAuth.getCurrentUser()!=null){
            if (mAuth.getCurrentUser().isAnonymous()) {
                currentUserID = currentUser.getUid();

                sqLiteManager = new SQLiteManager(homeFragmentContext);
                sqLiteManager.open();
                setupTrendingCollectionView();
                setupRecentlyViewedCollectionView();
            } else {
                currentUserID = currentUser.getUid();
                setupTrendingCollectionView();
                setupRecentlyViewedCollectionView();
                setupRecommendationCollectionView();
            }
        }else{
            mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    currentUser = mAuth.getCurrentUser();
                    setupFirebase();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(homeFragmentContext, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
        }
    }

    private void viewAllButtonAction() {
        trendingViewAllButton = (Button) v.findViewById(R.id.trendingViewAllButton);
        trendingViewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onViewAllButtonClickListener(NewsFeedActivity.class, null, "Trending", currentUserID);
            }
        });

        recentlyViewViewAllButton = (Button) v.findViewById(R.id.recentlyViewViewAllButton);
        recentlyViewViewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onViewAllButtonClickListener(NewsFeedActivity.class, null, "Recently Viewed", currentUserID);
            }
        });

        recommendationViewAllButton = (Button) v.findViewById(R.id.recommendationViewAllButton);
        recommendationViewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onViewAllButtonClickListener(NewsFeedActivity.class, preferences, "Recommendation", currentUserID);
            }
        });
    }



    private void setupTrendingCollectionView() {
        getTrendingItem();
        // Setup horizontal RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(homeFragmentContext, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = v.findViewById(R.id.trendingCollectionView);
        recyclerView.setLayoutManager(layoutManager);
        trendingAdapter = new TrendingCollectionView(homeFragmentContext, trendingData, this);
        recyclerView.setAdapter(trendingAdapter);
    }

    private void getTrendingItem() {
        //TODO: Decide how many views up
        int count = 0;
        db.collection(Constant.itemCollection).whereGreaterThan(Constant.viewField, 0).orderBy(Constant.viewField, Query.Direction.DESCENDING).limit(7).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot doc : list) {
                    Item trending = doc.toObject(Item.class);

                    if (trending!=null){
                        trending.setItemid(doc.getId());
                    }

                    trendingData.add(trending);
                    if (trendingData.size() == list.size()) {
                        trendingAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void setupRecentlyViewedCollectionView() {

        getRecentViewItemID();


        // Setup horizontal RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(homeFragmentContext, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = v.findViewById(R.id.recentlyViewCollectionView);
        recyclerView.setLayoutManager(layoutManager);
        recentViewAdapter = new RecentViewCollectionView(homeFragmentContext, recentlyViewData, this);
        recyclerView.setAdapter(recentViewAdapter);
    }

    private void getRecentViewItemID() {

        if (currentUser.isAnonymous()) {
            Cursor cursor = sqLiteManager.fetch(Constant.recentViewTable);
            if (cursor != null && cursor.getCount() != 0  ) {
                do{
                    String getItemID = cursor.getString(cursor.getColumnIndex("item_id"));
                    itemID.add(getItemID);
                }while (cursor.moveToNext());
                getRecentViewItem();
            }else{
                return;
            }
        } else {
            db.collection(Constant.userCollection)
                    .document(currentUserID)
                    .collection("recentView")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(7)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult()!=null) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        itemID.add(document.getId());
                                        if (itemID.size() == task.getResult().size()) {
                                            getRecentViewItem();
                                        }
                                    }
                                }
                            }else{
                                System.out.println("Error getting documents: ");
                            }
                        }
                    });
        }

    }

    private int i = 0;

    private void getRecentViewItem() {

//        TODO: What if the 7 top recently view has been deleted? Then there would be no data
//        for (int i = 0; i<itemID.size(); i++) {

            int count = i;
            db.collection(Constant.itemCollection)
                    .document(itemID.get(i))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Item item = documentSnapshot.toObject(Item.class);

                    if (item != null) {
                        item.setItemid(documentSnapshot.getId());

                        recentlyViewData.add(item);


//                        if (recentlyViewData.size() == (itemID.size() - deletedItem)) {
//                        if (recentlyViewData.size() == itemID.size()) {
                        recentViewAdapter.notifyDataSetChanged();
                        System.out.println("RECENTVIEW ADAPTER NOTIFYYYYYYYYYYYY");
//                        }
                    }else{
                        deletedItem += 1;
                        if (currentUser.isAnonymous()) {
                            sqLiteManager.delete(Constant.recentViewTable, itemID.get(count));
                        }else{
                            deleteRecentViewItem(count);
                        }
                    }
                    i++;
                    if (i<itemID.size()){
                        getRecentViewItem();
                    }
                }
            });
//        }
    }
    private void deleteRecentViewItem(int pos) {
        db.collection(Constant.userCollection).document(currentUser.getUid().toString()).collection("recentView").document(
                itemID.get(pos)
        ).delete();
    }

    private void setupRecommendationCollectionView() {
        getUserPreferences();

        // Setup horizontal RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(homeFragmentContext, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = v.findViewById(R.id.recommendationCollectionView);
        recyclerView.setLayoutManager(layoutManager);
        prefAdapter = new PreferenceCollectionView(homeFragmentContext, recommendationData,this);
        recyclerView.setAdapter(prefAdapter);
    }

    private void getUserPreferences() {
        //TODO: Change document path to current UID
        if (currentUser.isAnonymous()) {
            return;
        } else {
            db.collection(Constant.userCollection)
                    .document(currentUserID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                preferences = (ArrayList<String>) document.get("preferenceid");

                                if (preferences != null) {
                                    // if (preferences.size() <= 5) {
                                    getRecommendationItem();
                                    // }
                                }
                                if (preferences == null || preferences.size() == 0) {
                                    if (homeFragmentContext != null) {
                                        Toast.makeText(homeFragmentContext, "We Can't Recommend You Anything\nBecause No Preference", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }else{
                                System.out.println("NO USERRRRRRRRRRRRRRRRRRRRRR");
                            }
                        }
                    });
        }

    }

    private void getRecommendationItem() {
        for (int i = 0; i < preferences.size(); i++) {

            db.collection(Constant.itemCollection)
                    .whereEqualTo(Constant.subCategoryField, Constant.capitalize(preferences.get(i)))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    // Filter only item with most view
                    if (!list.isEmpty()) {

                        ArrayList<Item> rawRecommendationData = new ArrayList<>();
                        int documentSize = 0;
                        for(DocumentSnapshot doc : list) {
                            documentSize += 1;
                            Item trending = doc.toObject(Item.class);
                            if (trending!=null) {
                                trending.setItemid(doc.getId());
                            }

                            rawRecommendationData.add(trending);
                            if (rawRecommendationData.size() == list.size()) {
                                Collections.sort(rawRecommendationData);
                                for (Item i:rawRecommendationData){
                                    if (recommendationData.size()<7){
                                        recommendationData.add(i);
                                    }
                                }
//                                recommendationData.add(rawRecommendationData.get(0));
                            }
                        }

//                        if (recommendationData.size() == (preferences.size()-noTopViewItem)) {

//                        if (recommendationData.size() == 7) {
                            System.out.println("PREFERENCE ADAPTER NOTIFYYYYYYYYYYYY");
                            prefAdapter.notifyDataSetChanged();
//                        }
                    }else{
                        System.out.println("Query Empty");
                        noTopViewItem += 1;
                    }

                }
            });
        }

    }



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu); // inflate 'search.xml'
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        homeFragmentContext = null;
    }

}