package com.example.mygoods.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mygoods.R;

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private String[] arrString;
    private int[] arrImages;
//    private Class of something variable

    public GridViewAdapter (Context context,String[] arrString,int[] arrImages){
        this.context = context;
        this.arrString = arrString;
        this.arrImages = arrImages;
    }

    @Override
    public int getCount() {
        return arrImages.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        if (inflater==null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null){
            convertView = inflater.inflate(R.layout.grid_item, null);
        }

        ImageView gridImageView = convertView.findViewById(R.id.gridImage);
        TextView gridTextView = convertView.findViewById(R.id.gridText);

        gridImageView.setImageResource(arrImages[position]);
        gridTextView.setText(arrString[position]);

        return convertView;
    }
}
