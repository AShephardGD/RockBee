package com.example.rockbee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlaylistAdapter extends ArrayAdapter<String> {
    private String nums;
    private ArrayList<ArrayList<File>> playlists;
    public PlaylistAdapter(Context context, ArrayList<String> arr, String s, ArrayList<ArrayList<File>> list){
        super(context,R.layout.files, arr);
        nums = s;
        playlists = new ArrayList<>(list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String playName = getItem(position);
        if(convertView == null) convertView = LayoutInflater.from(getContext()).inflate(R.layout.files, null);
        ((ImageView) convertView.findViewById(R.id.fileOrDirectory)).setImageResource(R.drawable.vinil);
        ((TextView) convertView.findViewById(R.id.fileName)).setText(playName);
        ((TextView) convertView.findViewById(R.id.nums)).setText(nums + ": " + playlists.get(position).size());
        return convertView;
    }
}
