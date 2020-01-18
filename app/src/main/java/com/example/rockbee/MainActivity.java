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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    File root = new File("/storage/emulated/0");
    String a = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        a += "\t" + root + "\n";

        if (root.isDirectory()) {
            try {
                for (File file : root.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File file, String s) {
                        return s.contains(".mp3");
                    }
                })) {
                    a += "\t\t" + file + "\n";
                }
            } catch (NullPointerException e) {
                a += "root.list() вернул Null" + "\n";
                // Нужно запросить разрешение на работу с файловой системой устройства
                // пример: https://developer.android.com/training/permissions/requesting.html https://developer.android.com/guide/topics/permissions/overview
            }
        }

        TextView tw = findViewById(R.id.tw);
        tw.setText(a);
    }

}