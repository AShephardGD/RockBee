package com.example.rockbee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ServerPlaylistAdapter extends ArrayAdapter<String> {
    private int color;
    public ServerPlaylistAdapter(Context context, ArrayList<String> arr, int color){
        super(context, R.layout.files, arr);
        this.color = color;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final String string = getItem(position);
        if(convertView == null)convertView = LayoutInflater.from(getContext()).inflate(R.layout.files, null);
        ((TextView)convertView.findViewById(R.id.fileName)).setTextColor(color);
        ((ImageView)convertView.findViewById(R.id.fileOrDirectory)).setImageResource(R.drawable.note);
        (convertView.findViewById(R.id.nums)).setVisibility(View.GONE);
        ((TextView)convertView.findViewById(R.id.fileName)).setText(string.substring(0, string.lastIndexOf(".")));
        return convertView;
    }
}
