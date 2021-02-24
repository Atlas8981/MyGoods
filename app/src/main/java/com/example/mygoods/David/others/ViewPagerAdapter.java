package com.example.mygoods.David.others;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<String> image = new ArrayList<String>();
    private OnViewPagerItemClick onViewPagerItemClick;

    public ViewPagerAdapter(Context mContext, ArrayList<String> image, OnViewPagerItemClick onViewPagerItemClick) {
        this.mContext = mContext;
        this.image = image;
        this.onViewPagerItemClick = onViewPagerItemClick;
    }

    @Override
    public int getCount() {
        return image.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewPagerItemClick.onViewPagerItemClickListener();
            }
        });
        Glide.with(mContext).load(image.get(position)).centerCrop().into(imageView);
        container.addView(imageView, 0);

        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }

    public interface OnViewPagerItemClick {
        public void onViewPagerItemClickListener();
    }
}
