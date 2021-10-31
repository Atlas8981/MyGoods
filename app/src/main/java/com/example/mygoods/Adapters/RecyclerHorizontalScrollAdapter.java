package com.example.mygoods.Adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mygoods.R;

import java.util.List;

public class RecyclerHorizontalScrollAdapter extends RecyclerView.Adapter<RecyclerHorizontalScrollAdapter.RecyclerAdapterHolder> {

    private List<Bitmap> imageBitmap;
    private View view;
    private OnItemClickListener mlister;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onMinusSignClick(int position);
        void onPlusSignClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mlister = listener;
    }

    public static class RecyclerAdapterHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public ImageView plusSign;
        public ImageView minusSign;

        public RecyclerAdapterHolder (View itemView,final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImageUpload);
            plusSign = itemView.findViewById(R.id.plusSign);
            minusSign = itemView.findViewById(R.id.minusSign);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            minusSign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onMinusSignClick(position);
                        }
                    }
                }
            });

            plusSign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onPlusSignClick(position);
                        }
                    }
                }
            });
        }
    }

    public RecyclerHorizontalScrollAdapter() {}

    public RecyclerHorizontalScrollAdapter(List<Bitmap> imageBitmap) {
        this.imageBitmap = imageBitmap;
    }


    @NonNull
    @Override
    public RecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_image_view_horizontal,parent,false);
        RecyclerAdapterHolder viewHolder = new RecyclerAdapterHolder(v,mlister);
        view = v;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterHolder holder, int position) {

        if (imageBitmap.size()!=0) {
            if (position<imageBitmap.size()) {

                Glide.with(view)
                        .asBitmap()
                        .load(imageBitmap.get(position))
                        .placeholder(R.drawable.ic_camera)
                        .into(holder.imageView)

                ;

                holder.plusSign.setVisibility(View.INVISIBLE);
                holder.minusSign.setVisibility(View.VISIBLE);
            }
        }


    }



    @Override
    public int getItemCount() {
        int numCell = imageBitmap.size()+1;
        if (imageBitmap.size()==0){
            return 1;
        }else if (imageBitmap.size() < 5) {
            return imageBitmap.size()+1;
        }else{
            return imageBitmap.size();
        }

    }



}
