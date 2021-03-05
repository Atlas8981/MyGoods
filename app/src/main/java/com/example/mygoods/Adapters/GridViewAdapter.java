package com.example.mygoods.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.mygoods.R;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<String> preferences;
    private List<String> preferenceCheck;

    public GridViewAdapter (Context context,List<String> preferences){
        this.context = context;
        this.preferences = new ArrayList<>(preferences);
        preferenceCheck = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return preferences.size();
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
            convertView = inflater.inflate(R.layout.grid_preference_item, null);
        }

        ToggleButton toggleButton = convertView.findViewById(R.id.togglePreferenceButton);

        if (position<preferences.size()) {
            toggleButton.setText(preferences.get(position));
            toggleButton.setTextOff(preferences.get(position));
            toggleButton.setTextOn(preferences.get(position));
        }

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (preferenceCheck.size()<5) {
                        toggleButton.setBackgroundResource(R.drawable.check_button_background);
                        toggleButton.setTextColor(Color.parseColor("#3890FF"));

                        preferenceCheck.add(preferences.get(position));
                    }else{
                        Toast.makeText(context, "Cannot Select More Than 5", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    toggleButton.setBackgroundResource(R.drawable.uncheck_buttonframe);
                    toggleButton.setTextColor(Color.BLACK);

                    if (preferenceCheck != null) {
                        preferenceCheck.remove(preferences.get(position));
                    }
                }
            }
        });


        return convertView;
    }

    public List<String> getCheckedItems(){
        return preferenceCheck;
    }
}
