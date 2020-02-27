package com.example.rockbee;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class MainActivity extends FragmentActivity {
    private File root = new Environment().getExternalStorageDirectory(), parentDirectory = root;
    private ArrayList<File> files = new ArrayList<>();
    private ListView cg;
    private MediaPlayer mediaPlayer;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cg = findViewById(R.id.Catalog);
        mediaPlayer = new MediaPlayer();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);  // код не останавливается на месте требовании разрешения, а продолжает выполнение
                                                                // Из-за этого на экране пусто???
        }
        if (root.isDirectory()) {
            openDirectory(root, cg);
        }
        /*fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        CatalogFragment cf = new CatalogFragment();
        ft.add(R.id.fl, cf);
        ft.commit();*/
    }
    public void openDirectory(final File f, final ListView lv) {

        files.clear();
        try {
            TreeMap<String, File> directories = new TreeMap<>();
            for (File file : f.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File file, String s) {
                    return new File(file.getAbsolutePath() + "/" + s).isDirectory();
                }
            })) {
                directories.put(file.getName(), file);
            }
            for (File file: directories.values()) files.add(file);
            directories = new TreeMap<>();
            for (File file : f.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File file, String s) {
                    return (s.contains(".mp3") ||
                            s.contains(".ac3") ||
                            s.contains(".flac") ||
                            s.contains(".ogg") ||
                            s.contains(".wav") ||
                            s.contains(".wma"));
                }
            })) {
                directories.put(file.getName(), file);
            }
            for(File file: directories.values()) files.add(file);
        } catch (NullPointerException e) {}
        CatalogAdapter adapter = new CatalogAdapter(this, files);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(files.get(position).isDirectory()) {
                    parentDirectory = files.get(position);
                    openDirectory(files.get(position), lv);
                }
                else playMusic(files.get(position));
            }
        });
    }

    public void playMusic(File file) {
        try {
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(this, "Не воспроизводится!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed(){
        if(parentDirectory.equals(root)) finish();
        else {
            files.clear();
            parentDirectory = new File(parentDirectory.getParent());
            try {
                TreeMap<String, File> directories = new TreeMap<>();
                for (File file : parentDirectory.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File file, String s) {
                        return new File(file.getAbsolutePath() + "/" + s).isDirectory();
                    }
                })) {
                    directories.put(file.getName(), file);
                }
                for (File file: directories.values()) files.add(file);
                directories = new TreeMap<>();
                for (File file : parentDirectory.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File file, String s) {
                        return (s.contains(".mp3") ||
                                s.contains(".ac3") ||
                                s.contains(".flac") ||
                                s.contains(".ogg") ||
                                s.contains(".wav") ||
                                s.contains(".wma"));
                    }
                })) {
                    directories.put(file.getName(), file);
                }
                for(File file: directories.values()) files.add(file);
            } catch (NullPointerException e) {}
            CatalogAdapter adapter = new CatalogAdapter(this, files);
            cg.setAdapter(adapter);
            cg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(files.get(position).isDirectory()) {
                        parentDirectory = files.get(position);
                        openDirectory(files.get(position), cg);
                    }
                    else playMusic(files.get(position));
                }
            });
        }
    }
}