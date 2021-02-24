package com.example.mygoods.David.others.collectionview.ItemDetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mygoods.Model.Item;
import com.example.mygoods.R;

import java.util.ArrayList;

public class SimilarItemCollectionView extends RecyclerView.Adapter<SimilarItemCollectionView.ViewHolder> {

    private ArrayList<Item> data = new ArrayList<Item>();
    private SimilarItemOnClickListener similarItemOnClickListener;
    private Context mContext;

    public SimilarItemCollectionView(Context mContext, ArrayList<Item> data, SimilarItemOnClickListener similarItemOnClickListener) {
        this.mContext = mContext;
        this.similarItemOnClickListener = similarItemOnClickListener;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_collectionview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).load(data.get(position).getImages().get(0).getImageURL()).fitCenter().into(holder.itemImage);
        holder.itemPrice.setText("USD "+data.get(position).getPrice());
        holder.itemName.setText(data.get(position).getName().toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{
        ImageView itemImage;
        TextView itemName;
        TextView itemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.homeScreenProductImage);
            itemName  = itemView.findViewById(R.id.homeScreenProductName);
            itemPrice = itemView.findViewById(R.id.homeScreenProductPrice);

            itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    similarItemOnClickListener.onItemClickListener(getAdapterPosition());
                }
            });
        }
    }

    public interface SimilarItemOnClickListener {
        void onItemClickListener(int pos);
    }
}
