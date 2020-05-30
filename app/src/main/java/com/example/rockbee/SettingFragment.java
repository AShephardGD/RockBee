package com.example.rockbee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SettingFragment extends Fragment {
    private Switch random;
    private RadioButton loop0, loop1, loop2;
    private boolean isRandom = false;
    private int isLooping = 0;
    private RadioButton[] radioButtons = new RadioButton[19];
    private int colorNum = 0, color;
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
        FloatingActionButton fab = view.findViewById(R.id.fab2);
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
                }
            }
        };
        random.setOnClickListener(listener);
        loop0.setOnClickListener(listener);
        loop1.setOnClickListener(listener);
        loop2.setOnClickListener(listener);
        for(RadioButton rb: radioButtons) rb.setOnClickListener(listener);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorNum == 0)changeColor(getResources().getColor(R.color.white), getResources().getColor(R.color.black));
                else if(colorNum == 1) changeColor(getResources().getColor(R.color.black), getResources().getColor(R.color.white));
                else if(colorNum == 2) changeColor(getResources().getColor(R.color.beige), getResources().getColor(R.color.emerald));
                else if(colorNum == 3) changeColor(getResources().getColor(R.color.gray), getResources().getColor(R.color.pink));
                else if(colorNum == 4) changeColor(getResources().getColor(R.color.greenLime), getResources().getColor(R.color.darkBrown));
                else if(colorNum == 5) changeColor(getResources().getColor(R.color.cherryRed), getResources().getColor(R.color.lightOrange));
                else if(colorNum == 6) changeColor(getResources().getColor(R.color.brown), getResources().getColor(R.color.veryLightBlue));
                else if(colorNum == 7) changeColor(getResources().getColor(R.color.darkBrown), getResources().getColor(R.color.yellow));
                else if(colorNum == 8) changeColor(getResources().getColor(R.color.orange), getResources().getColor(R.color.blue));
                else if(colorNum == 9) changeColor(getResources().getColor(R.color.lightOrange), getResources().getColor(R.color.brown));
                else if(colorNum == 10) changeColor(getResources().getColor(R.color.darkOrange), getResources().getColor(R.color.paleYellow));
                else if(colorNum == 11) changeColor(getResources().getColor(R.color.paleYellow), getResources().getColor(R.color.red));
                else if(colorNum == 12) changeColor(getResources().getColor(R.color.goldYellow), getResources().getColor(R.color.azure));
                else if(colorNum == 13) changeColor(getResources().getColor(R.color.turquoise), getResources().getColor(R.color.darkPurple));
                else if(colorNum == 14) changeColor(getResources().getColor(R.color.electrician), getResources().getColor(R.color.goldYellow));
                else if(colorNum == 15) changeColor(getResources().getColor(R.color.darkBlue), getResources().getColor(R.color.yellowGreen));
                else if(colorNum == 16) changeColor(getResources().getColor(R.color.lily), getResources().getColor(R.color.darkPurple));
                else if(colorNum == 17) changeColor(getResources().getColor(R.color.darkPurple), getResources().getColor(R.color.turquoise));
                else if(colorNum == 18) changeColor(getResources().getColor(R.color.pink), getResources().getColor(R.color.olive));
                Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    public void set(boolean ran, int loops){
        isRandom = ran;
        isLooping = loops;
    }
    public void changeColor(int back, int text){
        color = text;
        tw1.setTextColor(color);
        tw2.setTextColor(color);
        random.setTextColor(color);
        loop0.setTextColor(color);
        loop1.setTextColor(color);
        loop2.setTextColor(color);
        for(RadioButton rb: radioButtons) rb.setTextColor(color);
        ((MainActivity) getActivity()).applyChanges(back, text);
    }
    public void setColor(int c){color = c;}
    public boolean getRan(){
        return isRandom;
    }
    public int getLoop(){
        return isLooping;
    }
    public int getColorNum() { return colorNum; }
    public void setColorNum(int colorNum) { this.colorNum = colorNum; }
}
