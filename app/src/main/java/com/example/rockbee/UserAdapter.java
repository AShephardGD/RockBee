package com.example.rockbee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<String> {
    private int color;
    public UserAdapter(Context context, ArrayList<String> arr, int color){
        super(context, android.R.layout.simple_list_item_1, arr);
        this.color = color;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(getItem(position));
        ((TextView) convertView.findViewById(android.R.id.text1)).setTextColor(color);
        return convertView;
    }
}
