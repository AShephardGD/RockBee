package com.example.rockbee;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends FragmentActivity {
    private MediaPlayer mediaPlayer;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Button prev, next, apply;
    public SettingFragment sf = new SettingFragment(); //0
    public CatalogFragment cf = new CatalogFragment();//1
    public MusicFragment mf = new MusicFragment();//2
    public PlaylistFragment pf = new PlaylistFragment();//3
    private ServerMusicFragment smf = new ServerMusicFragment();//4
    private int num = 1, isLooping = 0;
    private boolean isRandom = false;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 0;
    private LookingForProgress progress = new LookingForProgress();
    private TextView nowItem;
    private ArrayList<File> playlist;
    private TreeMap<String, File> music;
    private TreeMap<String, ArrayList<File>> playlists;

    public SharedPreferences sPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        load();
        mediaPlayer = new MediaPlayer();
        nowItem = findViewById(R.id.nameOfFrag);
        prev = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        apply = findViewById(R.id.apply); // При появлении в активности смещает все наверх
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);  // код не останавливается на месте требовании разрешения, а продолжает выполнение
                                                                // Из-за этого в первый раз на экране пусто???
        }
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.fl, cf);
        mf.setThread(progress);
        mf.setMediaPlayer(mediaPlayer);
        mf.setCatalogFragment(cf);
        cf.setPlaylistFragment(pf);
        cf.setMusicFragment(mf);
        cf.setMediaPlayer(mediaPlayer);
        cf.set(isRandom, isLooping);
        ft.commit();
        progress.setMusicFragment(mf);
        progress.start();
        nowItem.setText(getResources().getText(R.string.catalog));
        prev.setText("<<" + getResources().getText(R.string.settings));
        next.setText(getResources().getText(R.string.isPlaying) + ">>");
        pf.setCatalogFragment(cf);
        pf.setMusicFragment(mf);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.previous:
                        switch(num){
                            case 0://ServerMusicFragment is opened, settings is closed;
                                apply.setVisibility(View.GONE);
                                num = 4;
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, smf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.Playlists));
                                next.setText(getResources().getText(R.string.settings) + ">>");
                                nowItem.setText(getResources().getText(R.string.Lobby));
                                break;
                            case 1://Settings is opened, catalog is closed;
                                apply.setText(getResources().getText(R.string.apply));
                                apply.setVisibility(View.VISIBLE);
                                num = 0;
                                sf.set(isRandom, isLooping);
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, sf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.Lobby));
                                next.setText(getResources().getText(R.string.catalog) + ">>");
                                nowItem.setText(getResources().getText(R.string.settings));
                                break;
                            case 2://Catalog is opened, music is closed;
                                apply.setVisibility(View.GONE);
                                num = 1;
                                cf.set(isRandom, isLooping);
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, cf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.settings));
                                next.setText(getResources().getText(R.string.isPlaying) + ">>");
                                nowItem.setText(getResources().getText(R.string.catalog));
                                break;
                            case 3://Music is opened, playlists is closed;
                                apply.setText(getResources().getText(R.string.playAll));
                                num = 2;
                                mf.setIsRandom(isRandom);
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, mf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.catalog));
                                next.setText(getResources().getText(R.string.Playlists) + ">>");
                                nowItem.setText(getResources().getText(R.string.isPlaying));
                                break;
                            case 4://Playlists is opened, ServerMusic is closed;
                                apply.setText(getResources().getText(R.string.newPlaylist));
                                apply.setVisibility(View.VISIBLE);
                                num = 3;
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, pf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.isPlaying));
                                next.setText(getResources().getText(R.string.Lobby) + ">>");
                                nowItem.setText(getResources().getText(R.string.Playlists));
                                break;
                        }
                        break;
                    case R.id.next:
                        switch(num){
                            case 0://Catalog is opened, settings is closed;
                                apply.setVisibility(View.GONE);
                                num = 1;
                                cf.set(isRandom, isLooping);
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, cf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.settings));
                                next.setText(getResources().getText(R.string.isPlaying) + ">>");
                                nowItem.setText(getResources().getText(R.string.catalog));
                                break;
                            case 1://Music is opened, catalog is closed;
                                apply.setText(getResources().getText(R.string.playAll));
                                apply.setVisibility(View.VISIBLE);
                                num = 2;
                                mf.setIsRandom(isRandom);
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, mf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.catalog));
                                next.setText(getResources().getText(R.string.Playlists) + ">>");
                                nowItem.setText(getResources().getText(R.string.isPlaying));
                                break;
                            case 2://Playlists is opened, Music is closed;
                                apply.setText(getResources().getText(R.string.newPlaylist));
                                num = 3;
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, pf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.isPlaying));
                                next.setText(getResources().getText(R.string.Lobby) + ">>");
                                nowItem.setText(getResources().getText(R.string.Playlists));
                                break;
                            case 3://ServerMusicFragment is opened, Playlists is closed;
                                apply.setVisibility(View.GONE);
                                num = 4;
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, smf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.Playlists));
                                next.setText(getResources().getText(R.string.settings) + ">>");
                                nowItem.setText(getResources().getText(R.string.Lobby));
                                break;
                            case 4://Settings is opened, ServerMusic is closed;
                                apply.setText(getResources().getText(R.string.apply));
                                apply.setVisibility(View.VISIBLE);
                                num = 0;
                                sf.set(isRandom, isLooping);
                                ft = fm.beginTransaction();
                                ft.replace(R.id.fl, sf);
                                ft.commit();
                                prev.setText("<<" + getResources().getText(R.string.Lobby));
                                next.setText(getResources().getText(R.string.catalog) + ">>");
                                nowItem.setText(getResources().getText(R.string.settings));
                                break;
                        }
                        break;
                    case R.id.apply:
                        switch(num) {
                            case 0:
                                applyChanges();
                                break;
                            case 2:
                                music = new TreeMap<>();
                                playlist = new ArrayList<>();
                                playAllMusic(new Environment().getExternalStorageDirectory());
                                for (File f: music.values())playlist.add(f);
                                cf.playMusic(playlist.get((int) Math.round(Math.random() * (playlist.size() - 1))), playlist);
                                mf.setPlaylistFromActivity(playlist);
                                break;
                            case 3:
                                NewPlaylistDialog newPlaylistDialog = new NewPlaylistDialog();
                                newPlaylistDialog.show(fm, "newPlaylist");
                                break;
                        }
                        break;
                }
            }
        };
        prev.setOnClickListener(listener);
        next.setOnClickListener(listener);
        apply.setOnClickListener(listener);
    }
    public void onBackPressed(){
        if(num == 1) cf.onBackPressed();
        else if(num == 3) pf.onBackPressed();
        else finish();
    }
    public void save() {
        int i = 0, j = 0;
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        playlists = new TreeMap<>(pf.getPlaylists());
        for(Map.Entry<String, ArrayList<File>> entry: playlists.entrySet()){
            for(File file: entry.getValue()){
                ed.putString(Integer.toString(i) + Integer.toString(j), entry.getKey() + " " + file.getAbsolutePath());
                j++;
            }
            j = 0;
            i++;
        }
        ed.putInt("i", i);
        ed.putBoolean("IsRandom", isRandom);
        ed.putInt("IsLooping", isLooping);
        ed.commit();
    }
    public void load() {
        sPref = getPreferences(MODE_PRIVATE);
        int i =sPref.getInt("i", 0);
        isRandom = sPref.getBoolean("IsRandom", false);
        isLooping = sPref.getInt("IsLooping", 0);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        save();
    }
    public void applyChanges(){
        isRandom = sf.getRan();
        isLooping = sf.getLoop();
        cf.set(isRandom, isLooping);
    }
    public void playAllMusic(File file){
        for(File f: file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File pathname, String s) {
                return new File(pathname.getAbsolutePath() + "/" + s).isDirectory() ||
                        s.contains(".mp3") ||
                        s.contains(".ac3") ||
                        s.contains(".flac") ||
                        s.contains(".ogg") ||
                        s.contains(".wav") ||
                        s.contains(".wma");
            }
        })){
            if(f.isDirectory()) playAllMusic(f);
            else music.put(f.getName(), f);
        }
    }
    public void setNewPlaylistName(String s){
        pf.createNewPlaylist(s);
    }
    public void addNewSongToPlaylist(File file, String s){pf.addNewSongToPlaylist(file, s);}
    public Button gotApply(){return apply;}
}