package com.example.rockbee;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends FragmentActivity {
    private MediaPlayer mediaPlayer;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Button settings, catalog, music, apply;
    public CatalogFragment cf = new CatalogFragment();
    public MusicFragment mf = new MusicFragment();
    public SettingFragment sf = new SettingFragment();
    private int num = 1, isLooping = 0;
    private boolean isRandom = false;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    public SharedPreferences sPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        load();
        mediaPlayer = new MediaPlayer();
        settings = findViewById(R.id.settings);
        catalog = findViewById(R.id.papki);
        music = findViewById(R.id.music);
        apply = findViewById(R.id.apply);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);  // код не останавливается на месте требовании разрешения, а продолжает выполнение
                                                                // Из-за этого в первый раз на экране пусто???
        }
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.fl, cf);
        cf.setMediaPlayer(mediaPlayer);
        mf.setMediaPlayer(mediaPlayer);
        cf.setMusicFragment(mf);
        mf.setCatalogFragment(cf);
        cf.set(isRandom, isLooping);
        ft.commit();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.settings:
                        sf.set(isRandom, isLooping);
                        num = 0;
                        ft = fm.beginTransaction();
                        ft.replace(R.id.fl, sf);
                        ft.commit();
                        apply.setVisibility(View.VISIBLE);
                        break;
                    case R.id.papki:
                        apply.setVisibility(View.GONE);
                        cf.set(isRandom, isLooping);
                        num = 1;
                        ft = fm.beginTransaction();
                        ft.replace(R.id.fl, cf);
                        ft.commit();
                        break;
                    case R.id.music:
                        apply.setVisibility(View.GONE);
                        num = 2;
                        ft = fm.beginTransaction();
                        ft.replace(R.id.fl, mf);
                        ft.commit();
                        break;
                    case R.id.apply:
                        applyChanges();
                        break;
                }
            }
        };
        settings.setOnClickListener(listener);
        catalog.setOnClickListener(listener);
        music.setOnClickListener(listener);
        apply.setOnClickListener(listener);
    }
    public void onBackPressed(){
        if(num == 0 || num == 2){
            if (num == 0) {
                isRandom = sf.getRan();
                isLooping = sf.getLoop();
            }
            finish();
        }
        else cf.onBackPressed();
    }
    public void save() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("IsRandom", isRandom);
        ed.putInt("IsLooping", isLooping);
        ed.commit();
    }
    public void load() {
        sPref = getPreferences(MODE_PRIVATE);
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
}