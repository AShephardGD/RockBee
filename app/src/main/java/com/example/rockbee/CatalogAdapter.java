package com.example.rockbee;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class CatalogAdapter extends ArrayAdapter<File> {
    private String nums;
    private int color;
    public CatalogAdapter(Context context, ArrayList<File> arr, String s, int color) {
        super(context, R.layout.files, arr);
        nums = s;
        this.color = color;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final File file = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.files, null);
        }
        String name = file.getName();
        ((TextView) convertView.findViewById(R.id.fileName)).setTextColor(color);
        if(file.isFile()) {
            ((ImageView) convertView.findViewById(R.id.fileOrDirectory)).setImageResource(R.drawable.note);
            (convertView.findViewById(R.id.nums)).setVisibility(View.GONE);
            ((TextView) convertView.findViewById(R.id.fileName)).setText(name.substring(0, name.lastIndexOf(".")));
        }
        else {
            ((ImageView) convertView.findViewById(R.id.fileOrDirectory)).setImageResource(R.drawable.directory);
            try{
                ((TextView) convertView.findViewById(R.id.nums)).setText(nums + ": " + file.listFiles().length);
            } catch(NullPointerException e){
                Log.e("CatalogAdapter", e.toString());
            }
            ((TextView) convertView.findViewById(R.id.nums)).setTextColor(color);
            ((TextView) convertView.findViewById(R.id.fileName)).setText(file.getName());
        }
        return convertView;
    }
}
