package com.example.rockbee;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    File directory = new File("");

    File[] b = File.listRoots();
    String a = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        for(File root: b)  {
            a += "          " + root.getAbsolutePath() + "\n";
        }
        a += "       " + b[0].isDirectory();
        a += "       " + b[0].list().length;
        TextView tw = findViewById(R.id.tw);
        tw.setText(a);
    }

}