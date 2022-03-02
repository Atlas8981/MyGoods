package com.example.mygoods.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.mygoods.Activity.Category.CategoryListViewActivity;
import com.example.mygoods.Activity.Category.SubCategoryListActivity;
import com.example.mygoods.Adapters.RecyclerCategoryItemAdapter;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.PopularCategory;
import com.example.mygoods.Model.PopularCategoryView;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class CategoryFragment extends Fragment {

    private View v;
    private String[] arrCategoryTitles;
    private int[] arrCategoryImages;
    private RecyclerView categoryRecyclerView;
    private PopularCategoryView[] popularCategoryView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference itemRef = db.collection("items");

    private List<String> arrSubCat;
    private static List<PopularCategory> popularCategories;

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_category, container, false);

        progressBar = v.findViewById(R.id.categoryProgress);
        progressBar.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(Color.argb(100, 56, 144, 255));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressBar.setVisibility(View.VISIBLE);
                getPopularCategories();
            }
        });

        initializePopularCategoryView();

        arrSubCat = new ArrayList<>();
        mapAmoutViewPerSubCategory = new HashMap<>();
        arrSubCat.addAll(Arrays.asList(getResources().getStringArray(R.array.furiture)));
        arrSubCat.addAll(Arrays.asList(getResources().getStringArray(R.array.vehicle)));
        arrSubCat.addAll(Arrays.asList(getResources().getStringArray(R.array.electronic)));
        arrSubCat.removeIf(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.equalsIgnoreCase("other");
            }
        });

        if (popularCategories != null) {
            putDataintoPopularCategoryViews(popularCategories);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            getPopularCategories();
        }

        ActivateListCategory();

        return v;
    }

    private void getPopularCategories() {
//        1. Get The Total amount of views per item in Each Sub Category
        getPopularCategoryFromFirestore();
    }


    private int i = 0;
    private HashMap<String, Integer> mapAmoutViewPerSubCategory;

    private void getPopularCategoryFromFirestore() {
//        We loop it for each subCategory
        itemRef.whereEqualTo("subCategory", arrSubCat.get(i)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int totalViewsPerSubCategory = 0;
//                top6PopularCategories.put(arrSubCat.get(i),queryDocumentSnapshots.size());
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Item tempItem = documentSnapshot.toObject(Item.class);
                    if (tempItem.getViewers() != null) {
                        totalViewsPerSubCategory = totalViewsPerSubCategory + tempItem.getViews();
                    }
                }
//                For Each category we put the category name in key
//                and total amount of View per Sub Cat in the value
                mapAmoutViewPerSubCategory.put(arrSubCat.get(i), totalViewsPerSubCategory);

//                Check When the loop end

                if (i < arrSubCat.size() - 1) {
                    i++;
                    getPopularCategoryFromFirestore();
                } else {
                    //2. Then determine the top 6 popular category
                    setUpPopularCategory();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Map<String, Integer> SortHashMap(HashMap<String, Integer> unSortedMap) {
        //convert HashMap into List
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unSortedMap.entrySet());
        //sorting the list elements
        list.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> t1, Map.Entry<String, Integer> t2) {
//                if (order){
                return t2.getValue().compareTo(t1.getValue());
//                else{
//                    return o2.getValue().compareTo(o1.getValue());
//                }

            }
        });

        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }


        return sortedMap;
    }


    private void setUpPopularCategory() {

//        3. Sort data we get from firestore then put it in a list so that we can easily view
        populatePopularCategoriesList(SortHashMap(mapAmoutViewPerSubCategory));

    }

    private void populatePopularCategoriesList(Map<String, Integer> sortedMap) {
        popularCategories = new ArrayList<>();

//        We get that sorted hashmap which is the data
        sortedMap.entrySet().forEach(entry -> {
            popularCategories.add(new PopularCategory(entry.getKey(), findImageForSubCategory(entry.getKey())));
//            System.out.println(entry.getKey() + " " + entry.getValue());
        });

        putDataintoPopularCategoryViews(popularCategories);
    }


    private void putDataintoPopularCategoryViews(List<PopularCategory> popularCategories) {
//        initialize array of PopularCategory Views (Card Findview by id)

        for (int i = 0; i < popularCategoryView.length; i++) {
            int position = i;
            PopularCategory currentPopCat = popularCategories.get(i);

            if (getContext() != null) {
                Glide.with(getContext())
                        .load(currentPopCat.getCategoryImageRes())
                        .placeholder(R.drawable.ic_camera)
                        .into(popularCategoryView[i].popularCategoryImage);
            }
            popularCategoryView[i].popularCategoryText.setText(currentPopCat.getCategoryName());

            popularCategoryView[i].cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), CategoryListViewActivity.class);
                    intent.putExtra("SubCategory", popularCategories.get(position).getCategoryName());
                    startActivity(intent);
                }
            });
        }
        progressBar.setVisibility(View.INVISIBLE);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    private int findImageForSubCategory(String subCategoryName) {
        if (subCategoryName.equalsIgnoreCase("Bicycle")) {
            return R.drawable.bikepicture;
        } else if (subCategoryName.equalsIgnoreCase("phone")) {
            return R.drawable.phonepicture;
        } else if (subCategoryName.equalsIgnoreCase("Parts & Accessories")
                || subCategoryName.toLowerCase().contains("Parts".toLowerCase())) {
            return R.drawable.pc;
        } else if (subCategoryName.equalsIgnoreCase("Desktop")) {
            return R.drawable.desktoppic;
        } else if (subCategoryName.equalsIgnoreCase("cars")) {
            return R.drawable.carpic;
        } else if (subCategoryName.equalsIgnoreCase("laptop")) {
            return R.drawable.laptoppicture;
        } else if (subCategoryName.contains("Chair & Sofa") ||
                subCategoryName.toLowerCase().contains("Chair".toLowerCase())) {
            return R.drawable.sofapic;
        } else if (subCategoryName.equalsIgnoreCase("Table & Desk")
                || subCategoryName.toLowerCase().contains("Table".toLowerCase())) {
            return R.drawable.desk;
        } else if (subCategoryName.equalsIgnoreCase("Household item")
                || subCategoryName.toLowerCase().contains("Household".toLowerCase())) {
            return R.drawable.householdpic;
        } else if (subCategoryName.equalsIgnoreCase("Motobikes")
                || subCategoryName.toLowerCase().contains("Moto".toLowerCase())) {
            return R.drawable.motopic;
        } else {
            return R.drawable.camera;
        }
    }

    private void initializePopularCategoryView() {
        popularCategoryView = new PopularCategoryView[6];
        for (int i = 0; i < popularCategoryView.length; i++) {
            popularCategoryView[i] = new PopularCategoryView();
        }
        popularCategoryView[0].cardView = v.findViewById(R.id.card1);
        popularCategoryView[1].cardView = v.findViewById(R.id.card2);
        popularCategoryView[2].cardView = v.findViewById(R.id.card3);
        popularCategoryView[3].cardView = v.findViewById(R.id.card4);
        popularCategoryView[4].cardView = v.findViewById(R.id.card5);
        popularCategoryView[5].cardView = v.findViewById(R.id.card6);

        popularCategoryView[0].popularCategoryText = v.findViewById(R.id.popularCategoryText1);
        popularCategoryView[1].popularCategoryText = v.findViewById(R.id.popularCategoryText2);
        popularCategoryView[2].popularCategoryText = v.findViewById(R.id.popularCategoryText3);
        popularCategoryView[3].popularCategoryText = v.findViewById(R.id.popularCategoryText4);
        popularCategoryView[4].popularCategoryText = v.findViewById(R.id.popularCategoryText5);
        popularCategoryView[5].popularCategoryText = v.findViewById(R.id.popularCategoryText6);

        popularCategoryView[0].popularCategoryImage = v.findViewById(R.id.popularCategoryImage1);
        popularCategoryView[1].popularCategoryImage = v.findViewById(R.id.popularCategoryImage2);
        popularCategoryView[2].popularCategoryImage = v.findViewById(R.id.popularCategoryImage3);
        popularCategoryView[3].popularCategoryImage = v.findViewById(R.id.popularCategoryImage4);
        popularCategoryView[4].popularCategoryImage = v.findViewById(R.id.popularCategoryImage5);
        popularCategoryView[5].popularCategoryImage = v.findViewById(R.id.popularCategoryImage6);
    }


    //    Normal Main Category To SubCategory
    private void ActivateListCategory() {
        populateListCategory();

        categoryRecyclerView = v.findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerCategoryItemAdapter recyclerCategoryItemAdapter = new RecyclerCategoryItemAdapter(arrCategoryTitles, arrCategoryImages);

        categoryRecyclerView.setLayoutManager(layoutManager);
        categoryRecyclerView.setAdapter(recyclerCategoryItemAdapter);

        recyclerCategoryItemAdapter.setOnItemClickListener(new RecyclerCategoryItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Intent intent = new Intent();
                intent.setClass(getContext(), SubCategoryListActivity.class);
                intent.putExtra("MainCategory", arrCategoryTitles[position]);
                startActivity(intent);
            }
        });

    }

    private void populateListCategory() {

        arrCategoryTitles = getResources().getStringArray(R.array.categories);

        arrCategoryImages = new int[]{
                R.drawable.electronic,
                R.drawable.car,
                R.drawable.furniture

        };

    }


}