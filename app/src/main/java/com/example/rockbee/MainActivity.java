package com.example.rockbee;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static android.os.Environment.getExternalStorageDirectory;

/*
Список Ошибок:
1)LookingForProgressThread(возможно): Периодически зависает активность: кнопки нажимают, а отжаться не могут. При этом никаких действий не выполняют.
Доделать:
1)Серверную часть(обязательно)
2)Отдать на проверку бетатестерами, чтобы их кривые руки указали на ошибки.
 */

public class MainActivity extends FragmentActivity {
    private FragmentManager fm = getSupportFragmentManager();
    private SettingFragment sf = new SettingFragment(); //0
    private CatalogFragment cf = new CatalogFragment();//1
    private MusicFragment mf = new MusicFragment();//2
    private PlaylistFragment pf = new PlaylistFragment();//3
    private ServerMusicFragment smf = new ServerMusicFragment();//4
    private int isLooping = 0,  color = 1;
    private boolean isRandom = false;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 0;
    private LookingForProgress progress = new LookingForProgress();
    private TreeMap<String, ArrayList<File>> playlists;
    private SharedPreferences sPref;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabs;
    private File root = getExternalStorageDirectory();
    private MediaPlayerService service;
    private ServiceConnection sConn;
    private ArrayList<File> lastPlaylist = new ArrayList<>();
    private MediaPlayerService.MyBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startService(new Intent(this, MediaPlayerService.class));
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MediaPlayerService.MyBinder) service;
                MainActivity.this.service = ((MediaPlayerService.MyBinder) service).getService();
                MainActivity.this.service.setFragments(mf);
                cf.setService(MainActivity.this.service);
                mf.setService(MainActivity.this.service);
                pf.setService(MainActivity.this.service);
                sectionsPagerAdapter = new SectionsPagerAdapter(fm, sf, cf, mf, pf, smf, MainActivity.this);
                viewPager = findViewById(R.id.view_pager);
                tabs = findViewById(R.id.tabs);
                load();
                viewPager.setAdapter(sectionsPagerAdapter);
                tabs.setupWithViewPager(viewPager);
                progress.setMusicFragment(mf);
                progress.start();
                mf.setThread(progress);
                mf.setCatalogFragment(cf);
                cf.setPlaylistFragment(pf);
                cf.setMusicFragment(mf);
                pf.setCatalogFragment(cf);
                pf.setMusicFragment(mf);
                viewPager.setCurrentItem(1);
                mf.setIsPlaying(MainActivity.this.service.getNowPlaying());
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(MainActivity.this, getResources().getText(R.string.cantConnect), Toast.LENGTH_SHORT).show();
                finish();
            }
        };
        bindService(new Intent(this, MediaPlayerService.class), sConn, BIND_AUTO_CREATE);
    }
    public void onBackPressed(){
        int num = viewPager.getCurrentItem();
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
                ed.putString(i + Integer.toString(j), file.getAbsolutePath());
                j++;
            }
            ed.putString(i + "name", entry.getKey());
            ed.putInt(Integer.toString(i), j);
            j = 0;
            i++;
        }
        ed.putInt("numbersOfPlaylists", i);
        ed.putBoolean("IsRandom", isRandom);
        ed.putInt("IsLooping", isLooping);
        ed.putInt("color", color);
        lastPlaylist = new ArrayList<>(mf.getPlaylist());
        int len = lastPlaylist.size();
        for(int a = 0; a < len; a++)ed.putString("lastPlaylist" + a, lastPlaylist.get(a).getAbsolutePath());
        ed.putInt("lenLastPlaylist", len);
        ed.apply();
    }
    public void load() {
        sPref = getPreferences(MODE_PRIVATE);
        String name;
        int len, i = sPref.getInt("numbersOfPlaylists", 0);
        color = sPref.getInt("color", 0);
        ArrayList<File> songs;
        playlists = new TreeMap<>();
        for(int it = 0; it < i; it++) {
            name = sPref.getString(it + "name", "");
            len = sPref.getInt(Integer.toString(it), 0);
            songs = new ArrayList<>();
            for (int j = 0; j < len; j++)
                songs.add(new File(sPref.getString(it + Integer.toString(j), "")));
            playlists.put(name, songs);
        }
        pf.setPlaylists(playlists);
        isRandom = sPref.getBoolean("IsRandom", false);
        isLooping = sPref.getInt("IsLooping", 0);
        sf.set(isRandom, isLooping);
        mf.setIsRandom(isRandom);
        sf.setColorNum(color);
        if(color == 0)changeColor(getResources().getColor(R.color.white), getResources().getColor(R.color.black));
        else if(color == 1) changeColor(getResources().getColor(R.color.black), getResources().getColor(R.color.white));
        else if(color == 2) changeColor(getResources().getColor(R.color.beige), getResources().getColor(R.color.emerald));
        else if(color == 3) changeColor(getResources().getColor(R.color.gray), getResources().getColor(R.color.pink));
        else if(color == 4) changeColor(getResources().getColor(R.color.greenLime), getResources().getColor(R.color.darkBrown));
        else if(color == 5) changeColor(getResources().getColor(R.color.lightGreen), getResources().getColor(R.color.red));
        else if(color == 6) changeColor(getResources().getColor(R.color.cherryRed), getResources().getColor(R.color.lightOrange));
        else if(color == 7) changeColor(getResources().getColor(R.color.brown), getResources().getColor(R.color.veryLightBlue));
        else if(color == 8) changeColor(getResources().getColor(R.color.darkBrown), getResources().getColor(R.color.yellow));
        else if(color == 9) changeColor(getResources().getColor(R.color.orange), getResources().getColor(R.color.blue));
        else if(color == 10) changeColor(getResources().getColor(R.color.lightOrange), getResources().getColor(R.color.brown));
        else if(color == 11) changeColor(getResources().getColor(R.color.darkOrange), getResources().getColor(R.color.paleYellow));
        else if(color == 12) changeColor(getResources().getColor(R.color.paleYellow), getResources().getColor(R.color.red));
        else if(color == 13) changeColor(getResources().getColor(R.color.goldYellow), getResources().getColor(R.color.azure));
        else if(color == 14) changeColor(getResources().getColor(R.color.turquoise), getResources().getColor(R.color.darkPurple));
        else if(color == 15) changeColor(getResources().getColor(R.color.electrician), getResources().getColor(R.color.goldYellow));
        else if(color == 16) changeColor(getResources().getColor(R.color.darkBlue), getResources().getColor(R.color.yellowGreen));
        else if(color == 17) changeColor(getResources().getColor(R.color.lily), getResources().getColor(R.color.darkPurple));
        else if(color == 18) changeColor(getResources().getColor(R.color.darkPurple), getResources().getColor(R.color.turquoise));
        else if(color == 19) changeColor(getResources().getColor(R.color.pink), getResources().getColor(R.color.olive));
        len = sPref.getInt("lenLastPlaylist", 0);
        for(int it = 0; it < len; it++){
            lastPlaylist.add(new File(sPref.getString("lastPlaylist" + it, "")));
        }
        service.setIsLooping(isLooping);
        service.setRandom(isRandom);
        mf.setPlaylist(lastPlaylist);
    }
    @Override
    protected void onDestroy() {
        unbindService(sConn);
        super.onDestroy();
        save();
        progress.interrupt();
    }
    public void applyChanges(int back, int text){
        isRandom = sf.getRan();
        isLooping = sf.getLoop();
        color = sf.getColorNum();
        cf.changeColor(text);
        mf.changeColor(text);
        pf.changeColor(text);
        smf.changeColor(text);
        viewPager.setBackgroundColor(back);
        tabs.setTabTextColors(text, text);
        tabs.setBackgroundColor(back);
        mf.setIsRandom(isRandom);
        service.setIsLooping(isLooping);
        service.setRandom(isRandom);
    }

    public void setNewPlaylistName(String s){
        pf.createNewPlaylist(s);
    }
    public void playlistFromNowPlays(String s){
        pf.newPlaylistfromNowPlays(mf.getPlaylist(), s);
    }
    public void changeColor(int back, int text){
        cf.changeColor(text);
        mf.changeColor(text);
        pf.changeColor(text);
        smf.changeColor(text);
        sf.setColor(text);
        viewPager.setBackgroundColor(back);
        tabs.setBackgroundColor(back);
        tabs.setTabTextColors(text, text);
    }
    public void newPlaylist(){
        NewPlaylistDialog newPlaylistDialog = new NewPlaylistDialog();
        newPlaylistDialog.show(fm, "newPlaylist");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (root.isDirectory())cf.openDirectory(root, cf.getCg());
                } else{
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                }
        }
    }
}