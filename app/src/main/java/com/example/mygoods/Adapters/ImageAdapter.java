package com.example.mygoods.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.mygoods.Model.Image;
import com.example.mygoods.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter {

    private Context context;
    private List<Image> images = new ArrayList<>();
    private OnClickListener listener;
    private Boolean isZoomable = false;


    public interface OnClickListener{
        void OnItemClick(int position);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public ImageAdapter(Context context, List<Image> imageUrls,boolean isZoomable) {
        this.context = context;
        this.images = imageUrls;
        this.isZoomable =isZoomable;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Image currentImage = images.get(position);


        if (!isZoomable) {

            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


            Glide.with(context)
                    .load(currentImage.getImageURL())
                    .placeholder(R.drawable.ic_camera)
                    .into(imageView);

            container.addView(imageView, 0);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        if (position != ListView.INVALID_POSITION) {
                            listener.OnItemClick(position);
                        }
                    }
                }
            });
            return imageView;
        }
        else {
            PhotoView photoView = new PhotoView(context);
//            photoView.setScaleType(PhotoView.ScaleType.FIT_CENTER);

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener !=null){
                        if(position != ListView.INVALID_POSITION){
                            listener.OnItemClick(position);
                        }
                    }
                }
            });


            if (currentImage != null && currentImage.getImageURL() != null) {
                Glide.with(context)
                        .load(currentImage.getImageURL())
                        .placeholder(R.drawable.account)
                        .into(photoView);
            }else{
                Glide.with(context)
                        .load(R.drawable.account)
                        .placeholder(R.drawable.account)
                        .into(photoView);
            }

            container.addView(photoView,0);

            return photoView;
        }

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
