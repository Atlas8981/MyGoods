package com.example.mygoods.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mygoods.Activity.CategoryListViewActivity;
import com.example.mygoods.Activity.SubCategoryListActivity;
import com.example.mygoods.Adapters.RecyclerCategoryItemAdapter;
import com.example.mygoods.Model.PopularCategory;
import com.example.mygoods.Model.PopularCategoryView;
import com.example.mygoods.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private View v;
    private String[] arrCategoryTitles;
    private int[] arrCategoryImages;
    private RecyclerView categoryRecyclerView;
    private PopularCategoryView[] popularCategoryView;

    public CategoryFragment() {
        // Required empty public constructor
    }

    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
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
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_category, container, false);


        setUpPopularCategory();

        ActivateListCategory();

        return v;
    }

    private void setUpPopularCategory(){
        List<PopularCategory> popularCategory = new ArrayList<>();
        popularCategory.add(new PopularCategory(
                "Phone",
                R.drawable.phonepicture
        ));
        popularCategory.add(new PopularCategory(
                "Parts & Accessories",
                R.drawable.pc

        ));
        popularCategory.add(new PopularCategory(
                "Smart Watches",
                R.drawable.watches
        ));
        popularCategory.add(new PopularCategory(
                "Bicycle",
                R.drawable.bikepicture
        ));
        popularCategory.add(new PopularCategory(
                "Laptop",
                R.drawable.laptoppicture

        ));
        popularCategory.add(new PopularCategory(
                "Table & Desk",
                R.drawable.desk
        ));

//        initialize array
        popularCategoryView = new PopularCategoryView[6];

        for (int i = 0; i< popularCategoryView.length; i++){
            popularCategoryView[i] = new PopularCategoryView();
        }

//      Initialize view findViewby id
        initializePopularCategoryView();


//        populate Data into View
        for (int i = 0; i< popularCategoryView.length; i++){
            int position = i;
            PopularCategory currentPopCat = popularCategory.get(i);

            Glide.with(getContext())
                    .load(currentPopCat.getCategoryImageRes())
                    .placeholder(R.drawable.ic_camera)
                    .into(popularCategoryView[i].popularCategoryImage);

            popularCategoryView[i].popularCategoryText.setText(currentPopCat.getCategoryName());

            popularCategoryView[i].cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), CategoryListViewActivity.class);
                    intent.putExtra("SubCategory", popularCategory.get(position).getCategoryName());
                    startActivity(intent);
                    Toast.makeText(getContext(), popularCategory.get(position).getCategoryName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initializePopularCategoryView() {
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


    private void ActivateListCategory(){
        populateListCategory();

        categoryRecyclerView = v.findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerCategoryItemAdapter recyclerCategoryItemAdapter = new RecyclerCategoryItemAdapter(arrCategoryTitles,arrCategoryImages);

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

    private void populateListCategory(){

        arrCategoryTitles = getResources().getStringArray(R.array.categories);

        arrCategoryImages = new int[]{
                R.drawable.electronic,
                R.drawable.car,
                R.drawable.furniture

        };

    }



}