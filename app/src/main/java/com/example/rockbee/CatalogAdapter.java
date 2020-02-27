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

public class CatalogAdapter extends ArrayAdapter<File> {
    public CatalogAdapter(Context context, ArrayList<File> arr) {
        super(context, R.layout.files, arr);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final File file = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.files, null);
        }
        ((TextView) convertView.findViewById(R.id.fileName)).setText(file.getName());
        if(file.isFile()) ((ImageView) convertView.findViewById(R.id.fileOrDirectory)).setImageResource(R.drawable.note);
        else ((ImageView) convertView.findViewById(R.id.fileOrDirectory)).setImageResource(R.drawable.directory);
        return convertView;
    }
}
