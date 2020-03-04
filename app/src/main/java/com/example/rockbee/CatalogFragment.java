package com.example.rockbee;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class CatalogFragment extends Fragment {
    private File root = new Environment().getExternalStorageDirectory(), parentDirectory = root;
    private ArrayList<File> files = new ArrayList<>(), playlist = new ArrayList<>();
    private ListView cg;
    private MediaPlayer mediaPlayer;
    private boolean isRandom = false;
    private int isLooping = 0;
    MusicFragment mf;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.catalog_listview, container, false);
       cg = view.findViewById(R.id.catalog);
       if (root.isDirectory()) {
            openDirectory(parentDirectory, cg);
       }
       return view;
    }
    public void openDirectory(final File f, final ListView lv) {
        files.clear();
        playlist.clear();
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
            for(File file: directories.values()) {
                files.add(file);
                if(file.isFile())playlist.add(file);
            }
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Не могу открыть", Toast.LENGTH_LONG).show();
        }
        CatalogAdapter adapter = new CatalogAdapter(getActivity(), files, "" + getResources().getText(R.string.cg));
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(files.get(position).isDirectory()) {
                    parentDirectory = files.get(position);
                    openDirectory(files.get(position), lv);
                }
                else {
                    playMusic(files.get(position), playlist.indexOf(files.get(position)), playlist);
                }
            }
        });
    }

    public void playMusic(File file, final int position, final ArrayList<File> play) {
        boolean newPlay = true;
        final ArrayList<File> playNow = new ArrayList<>(play);
        try {
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mf.setMediaPlayer(mediaPlayer);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (isRandom && (isLooping != 2)) {

                        int newPosition = (int) Math.round(playlist.size() * Math.random());
                        playMusic(playNow.get(newPosition), newPosition, playNow);
                    }
                    else if ((isLooping == 1 || position + 1 != playlist.size()) && isLooping != 2) {
                        playMusic(playNow.get((position + 1) % playNow.size()), (position + 1) % playNow.size(), playNow);
                    }
                    else {
                        playMusic(playNow.get(position), position, playNow);
                    }
                }
            });
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Не воспроизводится!", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed(){
        if(parentDirectory.equals(root)) getActivity().finish();
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
            CatalogAdapter adapter = new CatalogAdapter(getActivity(), files,"" + getResources().getText(R.string.cg));
            cg.setAdapter(adapter);
            cg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(files.get(position).isDirectory()) {
                        parentDirectory = files.get(position);
                        openDirectory(files.get(position), cg);
                    }
                    else playMusic(files.get(position), position, playlist);
                }
            });
        }
    }
    public void setMediaPlayer(MediaPlayer mp){
        mediaPlayer = mp;
    }
    public void set(boolean ran, int loop){
        isRandom = ran;
        isLooping = loop;
    }
    public void setMusicFragment(MusicFragment fragment) {mf = fragment;}
}
