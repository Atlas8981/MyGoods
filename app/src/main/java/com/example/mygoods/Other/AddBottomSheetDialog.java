package com.example.mygoods.Other;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mygoods.Adapters.ListRowChoiceAdapter;
import com.example.mygoods.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class AddBottomSheetDialog extends BottomSheetDialogFragment {

    private onItemBottomSheetListener listener;
    private ListView listView;
    private EditText searchText;
    private AutoCompleteTextView autoCompleteTextView;
    private List<String> stringList;
    private ListRowChoiceAdapter stringArrayAdapter;

    public AddBottomSheetDialog(List<String> stringList){
        this.stringList = stringList;
    }

    public void setOnItemBottomSheetListener(onItemBottomSheetListener listener){
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottom_sheet_layout,container,false);

        listView = v.findViewById(R.id.bottom_listView);

        stringArrayAdapter = new ListRowChoiceAdapter(getActivity(),stringList);

        listView.setAdapter(stringArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                listener.onItemClicked(stringArrayAdapter.getItem(position));
            }
        });

        searchText = v.findViewById(R.id.searchEditText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                stringArrayAdapter.getFilter().filter(editable.toString());
            }
        });

        if (stringList.contains("Used")){
            searchText.setVisibility(View.GONE);
        }

        return v;

    }


    public interface onItemBottomSheetListener {
        void onItemClicked(String name);
    }

}
