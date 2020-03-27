package com.example.rockbee;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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

/*
Список Ошибок:
1)MainActivity(52-59): Проверяет выданные разрешения. Если в первый раз выдать разрешения, код не успеет учесть изменения и все равно пойдет так, как будто разрешение не было выдано.
Так же следует проработать тот вариант, когда пользователь отказался давать разрешения.
2)LookingForProgressThread(возможно): Периодически зависает активность: кнопки нажимают, а отжаться не могут. При этом никаких действий не выполняют.
3)Если перезайти в приложение, все состояние не сохранится: Плеер играет, но фрагмент показывает, что ничего не играет.
4)Не дать доступ к памяти - музыкальный фрагмент жалуется на то, что переданный из mainActivity mediaplayer = null;
Доделать:
1)Серверную часть(обязательно)
2)Вывод в уведомления(Чтобы пользователь мог управлять воспроизведением вне приложения)
3)Отдать на проверку бетатестерами, чтобы их кривые руки указали на ошибки.
4)Разобрать с аудиофокусом
 */

public class MainActivity extends FragmentActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);  // код не останавливается на месте требовании разрешения, а продолжает выполнение
            // Из-за этого в первый раз на экране пусто???
        }
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
        progress.setMusicFragment(mf);
        progress.start();
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
    }
    public void onBackPressed(){
        num = viewPager.getCurrentItem();
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
        mediaPlayer.stop();
        mediaPlayer.release();
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
}