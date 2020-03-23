package com.example.rockbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {
    private Switch random;
    private RadioButton loop0, loop1, loop2;
    private boolean isRandom = false;
    private int isLooping = 0;
    private RadioButton[] radioButtons = new RadioButton[20];
    private int colorNum = 0, color;
    private LinearLayout linearLayout;
    private TextView tw1, tw2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);
        random = view.findViewById(R.id.switch2);
        loop0 = view.findViewById(R.id.loop0);
        loop1 = view.findViewById(R.id.loop1);
        loop2 = view.findViewById(R.id.loop2);
        tw1 = view.findViewById(R.id.textView);
        tw2 = view.findViewById(R.id.textView2);
        linearLayout = view.findViewById(R.id.sf);
        radioButtons[0] = view.findViewById(R.id.radioButton);
        radioButtons[1] = view.findViewById(R.id.radioButton2);
        radioButtons[2] = view.findViewById(R.id.radioButton3);
        radioButtons[3] = view.findViewById(R.id.radioButton4);
        radioButtons[4] = view.findViewById(R.id.radioButton5);
        radioButtons[5] = view.findViewById(R.id.radioButton6);
        radioButtons[6] = view.findViewById(R.id.radioButton7);
        radioButtons[7] = view.findViewById(R.id.radioButton8);
        radioButtons[8] = view.findViewById(R.id.radioButton9);
        radioButtons[9] = view.findViewById(R.id.radioButton10);
        radioButtons[10] = view.findViewById(R.id.radioButton11);
        radioButtons[11] = view.findViewById(R.id.radioButton12);
        radioButtons[12] = view.findViewById(R.id.radioButton13);
        radioButtons[13] = view.findViewById(R.id.radioButton14);
        radioButtons[14] = view.findViewById(R.id.radioButton15);
        radioButtons[15] = view.findViewById(R.id.radioButton16);
        radioButtons[16] = view.findViewById(R.id.radioButton17);
        radioButtons[17] = view.findViewById(R.id.radioButton18);
        radioButtons[18] = view.findViewById(R.id.radioButton19);
        radioButtons[19] = view.findViewById(R.id.radioButton20);
        radioButtons[colorNum].setChecked(true);
        random.setChecked(isRandom);
        tw1.setTextColor(color);
        tw2.setTextColor(color);
        random.setTextColor(color);
        loop0.setTextColor(color);
        loop1.setTextColor(color);
        loop2.setTextColor(color);
        for(RadioButton rb: radioButtons) rb.setTextColor(color);
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
                        break;
                    case R.id.loop1:
                        isLooping = 1;
                        break;
                    case R.id.loop2:
                        isLooping = 2;
                        break;
                    case R.id.radioButton:
                        colorNum = 0;
                        break;
                    case R.id.radioButton2:
                        colorNum = 1;
                        break;
                    case R.id.radioButton3:
                        colorNum = 2;
                        break;
                    case R.id.radioButton4:
                        colorNum = 3;
                        break;
                    case R.id.radioButton5:
                        colorNum = 4;
                        break;
                    case R.id.radioButton6:
                        colorNum = 5;
                        break;
                    case R.id.radioButton7:
                        colorNum = 6;
                        break;
                    case R.id.radioButton8:
                        colorNum = 7;
                        break;
                    case R.id.radioButton9:
                        colorNum = 8;
                        break;
                    case R.id.radioButton10:
                        colorNum = 9;
                        break;
                    case R.id.radioButton11:
                        colorNum = 10;
                        break;
                    case R.id.radioButton12:
                        colorNum = 11;
                        break;
                    case R.id.radioButton13:
                        colorNum = 12;
                        break;
                    case R.id.radioButton14:
                        colorNum = 13;
                        break;
                    case R.id.radioButton15:
                        colorNum = 14;
                        break;
                    case R.id.radioButton16:
                        colorNum = 15;
                        break;
                    case R.id.radioButton17:
                        colorNum = 16;
                        break;
                    case R.id.radioButton18:
                        colorNum = 17;
                        break;
                    case R.id.radioButton19:
                        colorNum = 18;
                        break;
                    case R.id.radioButton20:
                        colorNum = 19;
                        break;
                }
            }
        };
        random.setOnClickListener(listener);
        loop0.setOnClickListener(listener);
        loop1.setOnClickListener(listener);
        loop2.setOnClickListener(listener);
        for(RadioButton rb: radioButtons) rb.setOnClickListener(listener);
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
    public int getColorNum() { return colorNum; }
    public void setColorNum(int colorNum) { this.colorNum = colorNum; }
    public void changeColor(int text){color = text;}
    public void apply(){
        tw1.setTextColor(color);
        tw2.setTextColor(color);
        random.setTextColor(color);
        loop0.setTextColor(color);
        loop1.setTextColor(color);
        loop2.setTextColor(color);
        for(RadioButton rb: radioButtons) rb.setTextColor(color);
    }
}
