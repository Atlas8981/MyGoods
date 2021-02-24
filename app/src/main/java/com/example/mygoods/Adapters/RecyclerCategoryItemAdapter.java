package com.example.mygoods.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygoods.R;

public class RecyclerCategoryItemAdapter extends RecyclerView.Adapter<RecyclerCategoryItemAdapter.RecyclerCategoryItemViewHolder>{

    private View view;
    private LayoutInflater layoutInflater;
    private String[] title;
    private int[] imagesRes;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class RecyclerCategoryItemViewHolder extends RecyclerView.ViewHolder{
        public TextView categoryTitleView;
        public ImageView categoryImageView;

        public RecyclerCategoryItemViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
            categoryImageView = itemView.findViewById(R.id.categoryimageView);
            categoryTitleView = itemView.findViewById(R.id.categoryTextView);

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

    public RecyclerCategoryItemAdapter(String[] title, int[] imagesRes) {
        this.title = title;
        this.imagesRes = imagesRes;
    }

    @NonNull
    @Override
    public RecyclerCategoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category_item,parent,false);
        RecyclerCategoryItemViewHolder recyclerCategoryItemViewHolder = new RecyclerCategoryItemViewHolder(v,listener);
        view = v;
        return recyclerCategoryItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerCategoryItemViewHolder holder, int position) {
        if (position<imagesRes.length){
            holder.categoryImageView.setImageResource(imagesRes[position]);
        }
            holder.categoryTitleView.setText(title[position]);
    }

    @Override
    public int getItemCount() {
        return title.length;
    }





}
