package com.example.mygoods.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygoods.David.activity.NewsFeedActivity;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.David.others.collectionview.Home.PreferenceCollectionView;
import com.example.mygoods.David.others.collectionview.Home.RecentViewCollectionView;
import com.example.mygoods.David.others.collectionview.Home.TrendingCollectionView;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    private Button trendingViewAllButton;
    private Button recentlyViewViewAllButton;
    private Button recommendationViewAllButton;

    String IMEINumber;
    private static final int REQUEST_CODE = 101;

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

    private void setupFirebase() {

        if (currentUser == null) {
            //TODO: Take user phone IMEI
        } else {
            currentUserID = currentUser.getUid().toString();
            setupTrendingCollectionView();
            setupRecentlyViewedCollectionView();
            setupRecommendationCollectionView();
        }
    }

    private void getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) homeFragmentContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(homeFragmentContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
            return;
        }else{
            Toast.makeText(homeFragmentContext, "error", Toast.LENGTH_SHORT).show();
        }
        IMEINumber = telephonyManager.getImei();
        Toast.makeText(homeFragmentContext, IMEINumber, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(homeFragmentContext, "Permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(homeFragmentContext, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
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
        //TODO: Change document path to current UID
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
                    }
                    i++;
                    if (i<itemID.size()){
                        getRecentViewItem();
                    }
                }
            });
//        }
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
        db.collection(Constant.userCollection)
                .document(currentUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                preferences = (ArrayList<String>) document.get("preferenceid");

                if (preferences !=null) {
                    System.out.println(preferences.size());
                    if (preferences.size() <= 5) {
                        getRecommendationItem();
                    }
                }
            }
        });
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
                                recommendationData.add(rawRecommendationData.get(0));
                            }
                        }
                        if (recommendationData.size() == (preferences.size()-noTopViewItem)) {
                            System.out.println("PREFERENCE ADAPTER NOTIFYYYYYYYYYYYY");
                            prefAdapter.notifyDataSetChanged();
                        }
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