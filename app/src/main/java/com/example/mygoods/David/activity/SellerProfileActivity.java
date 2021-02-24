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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mygoods.David.others.CustomProgressDialog;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SellerProfileActivity extends AppCompatActivity {

    private ImageView sellerImage;
    private TextView sellerName;
    private TextView sellerPhone;
    private ListView sellerItem;
    private CustomProgressDialog progressDialog;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<User> seller;
    private ArrayList<Item> sellerItems = new ArrayList<>();
    private ArrayList<String> time = new ArrayList<>();
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        setupViews();
        setupData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    private void setupViews() {
        sellerImage     = (ImageView) findViewById(R.id.sellerProfileImage);
        sellerName      = (TextView) findViewById(R.id.sellerProfileName);
        sellerPhone     = (TextView) findViewById(R.id.sellerProfilePhone);
        sellerItem      = (ListView) findViewById(R.id.sellerProfileListView);
        sellerItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                moveToDetailActivity(i);
            }
        });
    }

    private void setupData() {
        progressDialog = new CustomProgressDialog(SellerProfileActivity.this);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        progressDialog.show();
        seller = (ArrayList<User>) getIntent().getSerializableExtra("sellerData");

        Glide.with(SellerProfileActivity.this)
                .load(seller.get(0).getImage().getImageURL())
                .centerCrop()
                .into(sellerImage);

        sellerName.setText(seller.get(0).getUsername());
        sellerPhone.setText(seller.get(0).getPhoneNumber());
        getSellerItems();
        adapter = new CustomAdapter(this, R.layout.custom_newsfeed, sellerItems);
        sellerItem.setAdapter(adapter);
    }

    private void getSellerItems() {
//        db.collection(Constant.itemCollection).whereEqualTo(Constant.ownerField
        db.collection("items")
                .whereEqualTo("userid", seller.get(0).getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                int count = 0;
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot doc : list) {
                        count += 1;
                        Item tempItem = doc.toObject(Item.class);

                        sellerItems.add(tempItem);
                    }
                    getItemDuration();
                }else {

                    progressDialog.hide();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.hide();
            }
        });
    }

    private void getItemDuration() {
        for (int i = 0; i<sellerItems.size(); i++) {
            String duration = calculateDate(sellerItems.get(i).getDate());
            time.add(duration);
            if (i == (sellerItems.size() - 1)) {
                adapter.notifyDataSetChanged();
                progressDialog.hide();
            }

        }
    }

    private void moveToDetailActivity(int pos){
        Intent intent = new Intent();
        intent.setClass(this, ItemDetailActivity.class);
        intent.putExtra("ItemData", sellerItems.get(pos));
        startActivity(intent);
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

    private static class ViewHolder{
        ImageView itemImage;
        TextView itemName;
        TextView itemPrice;
        TextView itemOwner;
        TextView itemDuration;
        TextView itemViewCount;
    }

    private class CustomAdapter extends ArrayAdapter<Item> {

        private Context mContext;
        private int mResource;
        private ArrayList<Item> dataObjects;

        public CustomAdapter(Context context, int resource, ArrayList<Item>dataObjects) {
            super(context, resource, dataObjects);
            this.mContext = context;
            this.mResource = resource;
            this.dataObjects = dataObjects;
        }

        public View getView(int pos, View cView, ViewGroup parent){
            ViewHolder viewHolder = new ViewHolder();
            if(cView == null){
                LayoutInflater inflater = LayoutInflater.from(mContext);
                cView = inflater.inflate(R.layout.custom_newsfeed,parent,false);
                viewHolder.itemName = (TextView)cView.findViewById(R.id.titleTextView);
                viewHolder.itemPrice = (TextView)cView.findViewById(R.id.priceTextView);
                viewHolder.itemOwner = (TextView)cView.findViewById(R.id.postedByTextView);
                viewHolder.itemDuration = (TextView)cView.findViewById(R.id.durationTextView);
                viewHolder.itemViewCount = (TextView)cView.findViewById(R.id.viewCountTextView);
                viewHolder.itemImage = (ImageView)cView.findViewById(R.id.itemImageView);
                cView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) cView.getTag();
            }

            viewHolder.itemName.setText(dataObjects.get(pos).getName());
            viewHolder.itemPrice.setText("USD "+dataObjects.get(pos).getPrice());
            viewHolder.itemOwner.setText("Posted by: "+seller.get(0).getUsername());
            viewHolder.itemDuration.setText(time.get(pos));
            viewHolder.itemViewCount.setText("View: "+dataObjects.get(pos).getViews());
            viewHolder.itemImage.setImageResource(R.drawable.plastic);
            Glide.with(mContext).load(dataObjects.get(pos).getImages().get(0).getImageURL()).centerCrop().placeholder(R.drawable.loading).into(viewHolder.itemImage);

            return cView;
        }
    }
}