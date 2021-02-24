package com.example.mygoods.David.others.collectionview.Add;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygoods.R;

import java.util.ArrayList;

public class PhotoCollectionView extends RecyclerView.Adapter<PhotoCollectionView.ViewHolder> {

    private ArrayList<Bitmap> data = new ArrayList<Bitmap>();
    private AddPhotoCollectionViewObserver photoCollectionViewObserver;
    private Context mContext;
    private int[] notifyArray = new int[1];

    public PhotoCollectionView(Context mContext, ArrayList<Bitmap> data, int[] notifyArray, AddPhotoCollectionViewObserver photoCollectionViewObserver) {
        this.mContext = mContext;
        this.data = data;
        this.photoCollectionViewObserver = photoCollectionViewObserver;
        this.notifyArray = notifyArray;
    }

    // onCreateViewHolder: bind the layout file
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_collectionview, parent, false);
        return new ViewHolder(view);
    }

    // onBindViewHolder: provide data to each individual widgets
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemImage.setImageBitmap(data.get(position));
        if (notifyArray[0] == 0) {
                holder.removeImageButton.setVisibility(View.GONE);
        }else if (notifyArray[0] == 1){
            holder.removeImageButton.setVisibility(View.VISIBLE);
            holder.itemImage.setEnabled(false);
        }else{
            holder.removeImageButton.setVisibility(View.GONE);
            holder.itemImage.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView itemImage;
        ImageButton removeImageButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.photoForUpload);
            removeImageButton = itemView.findViewById(R.id.removeImageButton);

            itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    photoCollectionViewObserver.onImageViewClickListener();
                }
            });

            removeImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    photoCollectionViewObserver.onRemoveButtonClickListener(getAdapterPosition());
                }
            });
        }
    }

    public interface AddPhotoCollectionViewObserver {
        void onImageViewClickListener();
        void onRemoveButtonClickListener(int pos);
    }
}

