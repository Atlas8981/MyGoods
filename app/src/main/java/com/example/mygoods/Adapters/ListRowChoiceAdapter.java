package com.example.mygoods.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mygoods.R;

import java.util.ArrayList;
import java.util.List;

public class ListRowChoiceAdapter extends ArrayAdapter<String> implements Filterable{

    private Activity context;
    private List<String> stringList;
    private TextView textView;

    private ArrayList<String> stringArrayList;
    public ListRowChoiceAdapter(Activity context, List<String> stringList) {
        super(context, R.layout.list_row_choice, stringList);
        this.context = context;
        this.stringList = stringList;
        this.stringArrayList = new ArrayList<String>(stringList);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView;

        rowView = inflater.inflate(R.layout.list_row_choice, null, true);

        textView = rowView.findViewById(R.id.textView3);
//        if (position<stringList.size()) {
            textView.setText(stringList.get(position));
//        }

        return rowView;
    }



    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> filteredText = new ArrayList<>();
            if (constraint == null || constraint.length() == 0){
                filteredText.addAll(stringArrayList);
            }else{
                String filterPatter = constraint.toString().toLowerCase().trim();
                for (String s:stringArrayList){
                    if (s.toLowerCase().contains(filterPatter)){
                        filteredText.add(s);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredText;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            stringList.clear();
            stringList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };


}
