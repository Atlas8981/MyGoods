package com.example.mygoods.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ListMyItemRowAdapter extends ArrayAdapter<Item> {
    private Activity context;
    private List<Item> items;
    private OnItemClickListener listener;
    private boolean ableModify;

    public interface OnItemClickListener{
        void onDeleteBtnClick(int position);
        void onEditBtnClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    public ListMyItemRowAdapter(Activity context, List<Item> items,boolean ableModify){
        super(context, R.layout.row_myitem, items);
        this.context = context;
        this.items = items;
        this.ableModify = ableModify;
    }

    public void addListItemToAdapter (List<Item> list){
        items.addAll(list);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView;

        TextView myItemName;
        TextView myItemPrice;
        TextView myItemDate;
        TextView myItemViews;
        ImageView myItemImage;
        ImageButton myItemEdit;
        ImageButton myItemDelete;

        rowView = inflater.inflate(R.layout.row_myitem, null);

        myItemName = rowView.findViewById(R.id.myItemName);
        myItemPrice = rowView.findViewById(R.id.myItemPrice);
        myItemDate = rowView.findViewById(R.id.myItemDate);
        myItemViews = rowView.findViewById(R.id.myItemViews);
        myItemImage = rowView.findViewById(R.id.myItemImage);
        myItemEdit = rowView.findViewById(R.id.myItemEdit);
        myItemDelete = rowView.findViewById(R.id.myItemDelete);

//        Buyer View
        if (!ableModify){
            myItemEdit.setVisibility(View.INVISIBLE);
            myItemDelete.setVisibility(View.INVISIBLE);
        }

//      Start setting data into each view
        Item currentItem = items.get(position);

        Glide.with(rowView)
                .load(currentItem.getImages().get(0).getImageURL())
                .placeholder(R.drawable.ic_camera)
                .into(myItemImage);
        myItemViews.setText("Views " + String.valueOf(currentItem.getViews()));
        myItemName.setText(currentItem.getName());
        myItemPrice.setText("USD $" + String.valueOf(currentItem.getPrice()));

        if (currentItem.getDate() !=null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date itemDate = currentItem.getDate();
            dateFormat.format(itemDate);


            Date currentDate = new Date();
            String timeEnd = " minute(s) ";
            long date = (currentDate.getTime() - itemDate.getTime()) / 60000;
            if (date!=0) {
                if (date >= 60) {
                    date = date / 60;
                    timeEnd = " hour(s) ";
                    if (date >= 24) {
                        date = date/24;
                        timeEnd = " day(s) ";
                    }
                }
            }

            myItemDate.setText("Posted " + String.valueOf(date) + timeEnd + "ago");
        }else{

        }

        myItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null){
                    int position = getPosition(currentItem);
                    if (position != ListView.INVALID_POSITION){
                        listener.onDeleteBtnClick(position);
                    }
                }
            }
        });

        myItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null){
                    int position = getPosition(currentItem);
                    if (position != ListView.INVALID_POSITION){
                        listener.onEditBtnClick(position);
                    }
                }
            }
        });



        return rowView;

    }



}
