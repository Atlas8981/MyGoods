package com.example.mygoods.Adapters;

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

import java.util.List;

public class RecyclerSimilarItemAdapter extends RecyclerView.Adapter<RecyclerSimilarItemAdapter.RecyclerSimilarItemViewHolder> {

    private List<Item> items;
    private View view;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public static class RecyclerSimilarItemViewHolder extends RecyclerView.ViewHolder{
        public ImageView similarItemImage;
        public TextView similarItemName,similarItemPrice;

        public RecyclerSimilarItemViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            similarItemImage = itemView.findViewById(R.id.similarItemImage);
            similarItemName = itemView.findViewById(R.id.similarItemName);
            similarItemPrice = itemView.findViewById(R.id.similarItemPrice);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);

                        }

                    }
                }
            });
        }
    }

    public RecyclerSimilarItemAdapter(){}
    public RecyclerSimilarItemAdapter(List<Item> items){
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerSimilarItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_similar_item,parent,false);
        RecyclerSimilarItemViewHolder viewHolder = new RecyclerSimilarItemViewHolder(v,listener);
        view = v;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerSimilarItemViewHolder holder, int position) {
        Item currentItem = items.get(position);

        Glide.with(view)
                .load(currentItem.getImages().get(0).getImageURL())
                .placeholder(R.drawable.ic_camera)
                .into(holder.similarItemImage);
        holder.similarItemName.setText(currentItem.getName());
        holder.similarItemPrice.setText("USD $" + String.valueOf(currentItem.getPrice()));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }



}
