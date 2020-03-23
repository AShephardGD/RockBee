package com.example.rockbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class ServerMusicFragment extends Fragment {
    private int color;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.catalog, container, false);
        return view;
    }
    public void changeColor(int text){color = text;}
}
