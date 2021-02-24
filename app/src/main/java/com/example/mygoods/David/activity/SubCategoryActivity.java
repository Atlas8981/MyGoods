package com.example.mygoods.David.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.Model.Category;
import com.example.mygoods.David.others.Constant;
import com.example.mygoods.R;

import java.util.ArrayList;

public class SubCategoryActivity extends AppCompatActivity {

    private Bundle dataBundle;
    private CustomAdapter customAdapter;

    private ListView categoryListView;
    private ArrayList<Category> electronicAdapter = new ArrayList<Category>();
    private ArrayList<Category> furnitureAdapter = new ArrayList<Category>();
    private ArrayList<Category> carAdapter = new ArrayList<Category>();
    private String mainCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

        setupAdapters();
        setupViews();
    }

    private void setupAdapters() {
        electronicAdapter.add(new Category(R.drawable.phoneicon, "Phone"));
        electronicAdapter.add(new Category(R.drawable.electronicicon, "Desktop"));
        electronicAdapter.add(new Category(R.drawable.particon, "Part & Accessories"));
        electronicAdapter.add(new Category(R.drawable.electronicicon, "Laptop"));
        electronicAdapter.add(new Category(R.drawable.othericon, "Other"));

        furnitureAdapter.add(new Category(R.drawable.householdicon, "Table & Desk"));
        furnitureAdapter.add(new Category(R.drawable.sofaicon, "Chair & Sofa"));
        furnitureAdapter.add(new Category(R.drawable.deskicon, "Household Items"));
        furnitureAdapter.add(new Category(R.drawable.othericon, "Other"));

        carAdapter.add(new Category(R.drawable.caricon, "Car"));
        carAdapter.add(new Category(R.drawable.motoicon, "Motorbike"));
        carAdapter.add(new Category(R.drawable.bikeicon, "Bike"));
        carAdapter.add(new Category(R.drawable.othericon, "Other"));
    }

    private void setupViews() {
        dataBundle = getIntent().getExtras();
        mainCategory = dataBundle.getString("moveFromMainCat");
        SubCategoryActivity.this.setTitle(mainCategory);

        if (mainCategory.contains("Electronic")) {
            setupListView(electronicAdapter, mainCategory);
        } else if (mainCategory.contains("Furniture & Decors")) {
            setupListView(furnitureAdapter, mainCategory);
        } else {
            setupListView(carAdapter, mainCategory);
        }
    }

    private void setupListView(ArrayList<Category> adapter, String title) {

        categoryListView = (ListView) findViewById(R.id.subCategoryListView);
        customAdapter = new CustomAdapter(this, R.layout.category_listview, adapter);
        categoryListView.setAdapter(customAdapter);

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (title.contains("Electronic")) {
                    moveToNewsFeedActivity(electronicAdapter.get(i).getCategoryTitle());
                } else if (title.contains("Furniture & Decors")) {
                    moveToNewsFeedActivity(furnitureAdapter.get(i).getCategoryTitle());
                } else {
                    moveToNewsFeedActivity(carAdapter.get(i).getCategoryTitle());
                }
            }
        });
    }

    private void moveToNewsFeedActivity(String category) {
        Intent intent = new Intent();
        intent.setClass(this, NewsFeedActivity.class);
        intent.putExtra(Constant.intentFromSubCat, category);
        intent.putExtra(Constant.mainCatTitle, mainCategory);
        startActivity(intent);
    }

    private static class ViewHolder{
        ImageView catIcon;
        TextView catTitle;
    }

    private class CustomAdapter extends ArrayAdapter<Category> {

        private Context mContext;
        private int mResource;
        private ArrayList<Category> dataObjects;

        public CustomAdapter(Context context, int resource, ArrayList<Category>dataObjects) {
            super(context, resource, dataObjects);
            this.mContext = context;
            this.mResource = resource;
            this.dataObjects = dataObjects;
        }

        public View getView(int pos, View cView, ViewGroup parent){
            ViewHolder viewHolder = new ViewHolder();
            if(cView == null){
                LayoutInflater inflater = LayoutInflater.from(mContext);
                cView = inflater.inflate(R.layout.category_listview,parent,false);
                viewHolder.catIcon = (ImageView) cView.findViewById(R.id.categoryIcon);
                viewHolder.catTitle = (TextView)cView.findViewById(R.id.categoryTitle);
                cView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) cView.getTag();
            }

            viewHolder.catIcon.setImageResource(dataObjects.get(pos).getIconName());
            viewHolder.catTitle.setText(dataObjects.get(pos).getCategoryTitle());

            return cView;
        }
    }
}