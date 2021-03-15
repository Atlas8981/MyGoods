package com.example.mygoods.Activity.Category;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygoods.Adapters.RecyclerCategoryItemAdapter;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.R;


public class SubCategoryListActivity extends AppCompatActivity {

    private RecyclerView subCategoryRecyclerView;
    private String[] arrSubCategoryTitles;
    private int[] arrSubCategoryImages;
    private Bundle bundle;
    private String mainCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category_list);
        initializeUI();

        ActivateListCategory();
    }

    private void ActivateListCategory(){
        populateListCategory();
        subCategoryRecyclerView = findViewById(R.id.subCategoryRecyclerView);
        subCategoryRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerCategoryItemAdapter recyclerCategoryItemAdapter = new RecyclerCategoryItemAdapter(arrSubCategoryTitles,arrSubCategoryImages);

        subCategoryRecyclerView.setLayoutManager(layoutManager);
        subCategoryRecyclerView.setAdapter(recyclerCategoryItemAdapter);

        recyclerCategoryItemAdapter.setOnItemClickListener(new RecyclerCategoryItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent();
                intent.setClass(SubCategoryListActivity.this, CategoryListViewActivity.class);
                intent.putExtra("MainCategory",mainCategory);
                intent.putExtra("SubCategory",arrSubCategoryTitles[position]);
                startActivity(intent);
            }
        });

    }

    private void populateListCategory(){

        if (bundle!=null) {

            if (mainCategory.toLowerCase().contains("electronic")) {
                arrSubCategoryTitles = getResources().getStringArray(R.array.electronic);
                arrSubCategoryImages = new int[]{
                        R.drawable.phone,
                        R.drawable.electronic,
                        R.drawable.accessory,
                        R.drawable.laptop,
                        R.drawable.other
                };
            } else if (mainCategory.toLowerCase().contains("furniture")) {
                arrSubCategoryTitles = getResources().getStringArray(R.array.furiture);
                arrSubCategoryImages = new int[]{
                        R.drawable.table,
                        R.drawable.sofa,
                        R.drawable.household,
                        R.drawable.other
                };
            } else if (mainCategory.toLowerCase().contains("car")) {
                arrSubCategoryTitles = getResources().getStringArray(R.array.vehicle);
                arrSubCategoryImages = new int[]{
                        R.drawable.car,
                        R.drawable.moto,
                        R.drawable.bike,
                        R.drawable.other
                };
            }

            for (String arrSubCategoryTitle : arrSubCategoryTitles) {
                Constant.capitalize(arrSubCategoryTitle);
            }
        }
    }

    private void initializeUI(){
        bundle = getIntent().getExtras();
        mainCategory = bundle.getString("MainCategory");
        setTitle(mainCategory);
    }
}