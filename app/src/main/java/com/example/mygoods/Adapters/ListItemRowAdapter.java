package com.example.mygoods.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.mygoods.Model.Item;
import com.example.mygoods.Model.User;
import com.example.mygoods.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ListItemRowAdapter extends ArrayAdapter<Item> {

    private Activity context;
    private List<Item> items;
    private List<User> users;
    private ListMyItemRowAdapter.OnItemClickListener listener;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("users");

    private TextView itemName;
    private TextView itemPrice;
    private TextView itemDate;
    private TextView itemViews;
    private ImageView itemImage;
    private TextView itemOwner;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ListMyItemRowAdapter.OnItemClickListener listener){
        this.listener = listener;
    }


    public ListItemRowAdapter(Activity context, List<Item> items,List<User> users){
        super(context,R.layout.row_item,items);
//        super(context, R.layout.row_item, items);
        this.context = context;
        this.items = items;
        this.users = users;
    }

    public void addListItemToAdapter (List<Item> listItem,List<User> users){
        items.addAll(listItem);
        users.addAll(users);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView;

        rowView = inflater.inflate(R.layout.row_item, null);

        itemName = rowView.findViewById(R.id.itemName);
        itemPrice = rowView.findViewById(R.id.itemPrice);
        itemDate = rowView.findViewById(R.id.itemDate);
        itemViews = rowView.findViewById(R.id.itemView);
        itemImage = rowView.findViewById(R.id.itemImage);
        itemOwner = rowView.findViewById(R.id.itemOwner);


//      Start setting data into each view

        Item currentItem = items.get(position);

        Glide.with(rowView)
                .load(currentItem.getImages().get(0).getImageURL())
                .placeholder(R.drawable.ic_camera)
                .into(itemImage);

        itemViews.setText("Views: " + String.valueOf(currentItem.getViews()));
        itemName.setText(currentItem.getName());
        itemPrice.setText("USD $" + String.valueOf(currentItem.getPrice()));
        itemOwner.setText("Posted by Someone");


        if (currentItem.getDate() != null) {
            itemDate.setText("Posted " + calculateDate(currentItem.getDate()) + "ago");
        }
        if (position<users.size()) {
            for (User u: users){
                if (u.getUserId().equals(currentItem.getUserid())){
                    itemOwner.setText("Posted by " + u.getUsername());
                }
            }

        }




        return rowView;
    }

    private String calculateDate(Date itemDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.format(itemDate);


        Date currentDate = new Date();
        String timeEnd = " minute(s) ";

        double date = (currentDate.getTime() - itemDate.getTime()) / 60000;

        if (date > 0) {
            if (date >= 60) {
                date = date / 60;
                timeEnd = " hour(s) ";

                if (date >= 24) {
                    date = date/24;
                    date = Math.round(date);
                    timeEnd = " day(s) ";

                }
            }
        }


        return ((int) date) + timeEnd;
    }

}
