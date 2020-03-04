package com.example.rockbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {
    private Switch random;
    private RadioButton loop0, loop1, loop2;
    private boolean isRandom = false;
    private int isLooping = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);
        random = view.findViewById(R.id.switch2);
        loop0 = view.findViewById(R.id.loop0);
        loop1 = view.findViewById(R.id.loop1);
        loop2 = view.findViewById(R.id.loop2);
        random.setChecked(isRandom);
        if(isLooping == 0) loop0.setChecked(true);
        else if(isLooping == 1) loop1.setChecked(true);
        else loop2.setChecked(true);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.switch2:
                        isRandom = random.isChecked();
                        break;
                    case R.id.loop0:
                        isLooping = 0;
                        loop1.setChecked(false);
                        loop2.setChecked(false);
                        break;
                    case R.id.loop1:
                        isLooping = 1;
                        loop0.setChecked(false);
                        loop2.setChecked(false);
                        break;
                    case R.id.loop2:
                        isLooping = 2;
                        loop1.setChecked(false);
                        loop0.setChecked(false);
                        break;
                }
            }
        };
        random.setOnClickListener(listener);
        loop0.setOnClickListener(listener);
        loop1.setOnClickListener(listener);
        loop2.setOnClickListener(listener);
        return view;
    }
    public void set(boolean ran, int loops){
        isRandom = ran;
        isLooping = loops;
    }
    public boolean getRan(){
        return isRandom;
    }
    public int getLoop(){
        return isLooping;
    }
}
