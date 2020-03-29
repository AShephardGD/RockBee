package com.example.rockbee;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static android.os.Environment.getExternalStorageDirectory;

/*
Список Ошибок:
1)LookingForProgressThread(возможно): Периодически зависает активность: кнопки нажимают, а отжаться не могут. При этом никаких действий не выполняют.
2)Много ошибок, неизвестного производства, порой логкэт даже не пишет строчки в моем коде, а только приложение косячит.
Короче работает через раз, а что делать с этим не знаю...
Доделать:
1)Серверную часть(обязательно)
2)Вывод в уведомления(Чтобы пользователь мог управлять воспроизведением вне приложения)
3)Отдать на проверку бетатестерами, чтобы их кривые руки указали на ошибки.
4)Разобрать с аудиофокусом(Необязательно)
 */

public class MainActivity extends FragmentActivity {
    private static final String CHANNEL_ID = "1";
    private MediaPlayer mediaPlayer;
    private FragmentManager fm;
    private SettingFragment sf = new SettingFragment(); //0
    private CatalogFragment cf = new CatalogFragment();//1
    private MusicFragment mf = new MusicFragment();//2
    private PlaylistFragment pf = new PlaylistFragment();//3
    private ServerMusicFragment smf = new ServerMusicFragment();//4
    private int num = 1, isLooping = 0, color = 1;
    private boolean isRandom = false;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 0;
    private LookingForProgress progress = new LookingForProgress();
    private TreeMap<String, ArrayList<File>> playlists;
    private SharedPreferences sPref;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabs;
    private File isPlaying = new File(getExternalStorageDirectory().getAbsolutePath()+"/Temporary Music From RockBee", "a"),
            root = getExternalStorageDirectory(),
    playPath = new File(getExternalStorageDirectory().getAbsolutePath()+"/Temporary Music From RockBee", "b"),
    playTime = new File(getExternalStorageDirectory().getAbsolutePath()+"/Temporary Music From RockBee", "c");
    private ArrayList<File> wasPlayingPlaylist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);
        }
        if(!isPlaying.exists()) {
            try {
                isPlaying.createNewFile();
            } catch (IOException e) {
                Log.e("error IOException: ", e.toString());
            }
        }
        if(!playPath.exists()) {
            try {
                playPath.createNewFile();
            } catch (IOException e) {
                Log.e("error IOException: ", e.toString());
            }
        }
        if(!playTime.exists()) {
            try {
                playTime.createNewFile();
            } catch (IOException e) {
                Log.e("error IOException: ", e.toString());
            }
        }
        changeRun(false);
        loadWasPlayingPlaylist();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = new MediaPlayer();
        fm = getSupportFragmentManager();
        mf.setThread(progress);
        mf.setMediaPlayer(mediaPlayer);
        mf.setCatalogFragment(cf);
        cf.setPlaylistFragment(pf);
        cf.setMusicFragment(mf);
        cf.setMediaPlayer(mediaPlayer);
        cf.setThread(progress);
        progress.setMusicFragment(mf);
        changeRun(true);
        new Thread(progress).start();
        pf.setCatalogFragment(cf);
        pf.setMusicFragment(mf);
        sectionsPagerAdapter = new SectionsPagerAdapter(fm, sf, cf, mf, pf, smf, this);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);
        load();
        sf.setColorNum(color);
        cf.set(isRandom, isLooping);
        wasPlaying();
        createNotificationChannel();
        clearTempFile();
    }
    public void onBackPressed(){
        num = viewPager.getCurrentItem();
        if(num == 1) cf.onBackPressed();
        else if(num == 3) pf.onBackPressed();
        else finish();
    }
    public void save() {
        wasPlayingPlaylist = new ArrayList<>(cf.getNowPlayingPlaylist());
        int i = 0, j = 0, numbersOfSavedPlaylist = wasPlayingPlaylist.size(), a = 0;
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
        ed.putInt("numbersOfSavedPlaylist", numbersOfSavedPlaylist);
        for(File file: wasPlayingPlaylist) {
            ed.putString("wasPlayingPlaylist" + a, file.getAbsolutePath());
            a++;
        }
        ed.putInt("numbersOfPlaylists", i);
        ed.putBoolean("IsRandom", isRandom);
        ed.putInt("IsLooping", isLooping);
        ed.putInt("color", color);
        ed.apply();
    }
    public void load() {
        String name;
        int len;
        ArrayList<File> songs;
        playlists = new TreeMap<>();
        sPref = getPreferences(MODE_PRIVATE);
        int i = sPref.getInt("numbersOfPlaylists", 0);
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
        cf.set(isRandom, isLooping);
        sf.set(isRandom, isLooping);
        mf.setIsRandom(isRandom);
        color = sPref.getInt("color", 1);
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
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        save();

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
        cf.set(isRandom, isLooping);
        mf.setIsRandom(isRandom);
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
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void changeRun(boolean run){
        try(FileWriter writer = new FileWriter(isPlaying, false))
        {
            writer.write("" + run);
            writer.flush();
        } catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
    }
    public void wasPlaying(){
        try(FileReader reader = new FileReader(playPath))
        {

            String str = "";
            int c;
            while((c=reader.read())!=-1){
                str += (char) c;
            }
            if(!str.equals("")) {
                File f = new File(str);
                cf.playMusic(f, wasPlayingPlaylist, false);
            }
        }
        catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
        mf.setPlaylist(wasPlayingPlaylist);
        try(FileReader reader = new FileReader(playTime))
        {
            String str = "";
            int c;
            while((c=reader.read())!=-1){
                str += (char) c;
            }
            if(!str.equals("")) cf.seekTo(Integer.parseInt(str));
        }
        catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
    }
    public void loadWasPlayingPlaylist(){
        sPref = getPreferences(MODE_PRIVATE);
        int i = sPref.getInt("numbersOfSavedPlaylist", 0);
        wasPlayingPlaylist = new ArrayList<>();
        for(int j = 0; j < i; j++)wasPlayingPlaylist.add(new File(sPref.getString("wasPlayingPlaylist" + j, "")));
    }
    public void clearTempFile(){
        try(FileWriter writer = new FileWriter(playTime, false))
        {
            writer.write("");
            writer.flush();
        } catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
        try(FileWriter writer = new FileWriter(playPath, false))
        {
            writer.write("");
            writer.flush();
        } catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
    }
}