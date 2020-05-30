package com.example.rockbee;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

/*
Список Ошибок:
1)Иногда сервис не переключает музыку, когда телефон выключен. Выяснить.
2)При долгой паузе и отключенном приложении сервис выключается. Так не должно быть.
3)Включить музыку. Появилось уведомление. Остановить музыку. Смахнуть уведомление. Приложение вылетело. ЛогКэт молчит как партизан.
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
    private int color = 1;
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
    private Handler newUUIDH = new Handler(){
        public void handleMessage(android.os.Message msg){
            smf.UUIDChanged();
        }
    }, check = new Handler(){
        public void handleMessage(android.os.Message msg){
            Toast.makeText(MainActivity.this, "here", Toast.LENGTH_SHORT).show();
        }
    };
    private OkHttpClient client;
    private WebSocket ws;
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
                MainActivity.this.service = ((MediaPlayerService.MyBinder) service).getService();
                MainActivity.this.service.setFragments(mf, smf);
                cf.setService(MainActivity.this.service);
                mf.setService(MainActivity.this.service);
                pf.setService(MainActivity.this.service);
                smf.setService(MainActivity.this.service);
                sectionsPagerAdapter = new SectionsPagerAdapter(fm, sf, cf, mf, pf, smf, MainActivity.this);
                viewPager = findViewById(R.id.view_pager);
                tabs = findViewById(R.id.tabs);
                load();
                viewPager.setAdapter(sectionsPagerAdapter);
                tabs.setupWithViewPager(viewPager);
                progress.setMusicFragment(mf);
                Handler h = new Handler(){
                    public void handleMessage(android.os.Message msg) {
                        mf.resetTime();
                    }
                };
                progress.setH(h);
                progress.start();
                mf.setThread(progress);
                mf.setCatalogFragment(cf);
                mf.setServerMusicFragment(smf);
                mf.setIsPlaying(MainActivity.this.service.getNowPlaying());
                cf.setPlaylistFragment(pf);
                cf.setMusicFragment(mf);
                cf.setServerMusicFragment(smf);
                pf.setCatalogFragment(cf);
                pf.setMusicFragment(mf);
                pf.setServerMusicFragment(smf);
                client = new OkHttpClient();
                Request request = new Request.Builder().url("ws:192.168.1.65:8080/handler").build();
                EchoWebSocketListener listener = new EchoWebSocketListener();
                ws = client.newWebSocket(request, listener);
                smf.setWebSocket(ws);
                if(smf.isConnected() || smf.isRoom()) {
                    MessageToWebSocket message = new MessageToWebSocket();
                    message.setCommand("oncreate");
                    message.setUUID(smf.getConnectedAddress());
                    message.setData("");
                    ws.send(new Gson().toJson(message));
                }
                viewPager.setCurrentItem(1);
                viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
                    @Override
                    public void onPageSelected(int position) {
                        if(position == 4){
                            if(isConnectedToTheInternet())return;
                            if(smf.getName() == null) {
                                View view = getLayoutInflater().inflate(R.layout.playlists_alert_dialog, null);
                                EditText text = view.findViewById(R.id.newPlaylistName);
                                text.setHint(R.string.enterName);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle(R.string.name)
                                        .setPositiveButton(getResources().getText(R.string.ready), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String s = text.getText().toString();
                                                if(s.equals("")) {
                                                    Toast.makeText(MainActivity.this, getResources().getText(R.string.noName), Toast.LENGTH_SHORT).show();
                                                    viewPager.setCurrentItem(3);
                                                }
                                                else {
                                                    smf.setName(s);
                                                    refresh(4);
                                                }
                                            }
                                        })
                                        .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                viewPager.setCurrentItem(3);
                                            }
                                        })
                                        .setCancelable(false)
                                        .setView(view)
                                        .create()
                                        .show();
                            }
                            new checkUUID().execute();
                            smf.setMe(getResources().getString(R.string.name));
                        }
                    }
                    @Override
                    public void onPageScrollStateChanged(int state) { }
                });
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
        ed.putBoolean("IsRandom", sf.getRan());
        ed.putInt("IsLooping", sf.getLoop());
        ed.putInt("color", color);
        lastPlaylist = new ArrayList<>(mf.getPlaylist());
        int len = lastPlaylist.size();
        for(int a = 0; a < len; a++)ed.putString("lastPlaylist" + a, lastPlaylist.get(a).getAbsolutePath());
        ed.putInt("lenLastPlaylist", len);
        ed.putString("nameOfUser", smf.getName());
        ed.putBoolean("isUserConnected", smf.isConnected());
        ed.putBoolean("isUserRoom", smf.isRoom());
        ed.putString("connectedAddress", smf.getConnectedAddress());
        ed.putBoolean("delete", smf.isDelete());
        ed.putBoolean("isPlayingToo", smf.isPlayingToo());
        ed.putString("UUID", smf.getUUID());
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
        boolean isRandom = sPref.getBoolean("IsRandom", false);
        int isLooping = sPref.getInt("IsLooping", 0);
        sf.set(isRandom, isLooping);
        mf.setIsRandom(isRandom);
        sf.setColorNum(color);
        if(color == 0)changeColor(getResources().getColor(R.color.white), getResources().getColor(R.color.black));
        else if(color == 1) changeColor(getResources().getColor(R.color.black), getResources().getColor(R.color.white));
        else if(color == 2) changeColor(getResources().getColor(R.color.beige), getResources().getColor(R.color.emerald));
        else if(color == 3) changeColor(getResources().getColor(R.color.gray), getResources().getColor(R.color.pink));
        else if(color == 4) changeColor(getResources().getColor(R.color.greenLime), getResources().getColor(R.color.darkBrown));
        else if(color == 5) changeColor(getResources().getColor(R.color.cherryRed), getResources().getColor(R.color.lightOrange));
        else if(color == 6) changeColor(getResources().getColor(R.color.brown), getResources().getColor(R.color.veryLightBlue));
        else if(color == 7) changeColor(getResources().getColor(R.color.darkBrown), getResources().getColor(R.color.yellow));
        else if(color == 8) changeColor(getResources().getColor(R.color.orange), getResources().getColor(R.color.blue));
        else if(color == 9) changeColor(getResources().getColor(R.color.lightOrange), getResources().getColor(R.color.brown));
        else if(color == 10) changeColor(getResources().getColor(R.color.darkOrange), getResources().getColor(R.color.paleYellow));
        else if(color == 11) changeColor(getResources().getColor(R.color.paleYellow), getResources().getColor(R.color.red));
        else if(color == 12) changeColor(getResources().getColor(R.color.goldYellow), getResources().getColor(R.color.azure));
        else if(color == 13) changeColor(getResources().getColor(R.color.turquoise), getResources().getColor(R.color.darkPurple));
        else if(color == 14) changeColor(getResources().getColor(R.color.electrician), getResources().getColor(R.color.goldYellow));
        else if(color == 15) changeColor(getResources().getColor(R.color.darkBlue), getResources().getColor(R.color.yellowGreen));
        else if(color == 16) changeColor(getResources().getColor(R.color.lily), getResources().getColor(R.color.darkPurple));
        else if(color == 17) changeColor(getResources().getColor(R.color.darkPurple), getResources().getColor(R.color.turquoise));
        else if(color == 18) changeColor(getResources().getColor(R.color.pink), getResources().getColor(R.color.olive));
        len = sPref.getInt("lenLastPlaylist", 0);
        for(int it = 0; it < len; it++){
            File f = new File(sPref.getString("lastPlaylist" + it, ""));
            if(f.exists())lastPlaylist.add(f);
        }
        service.setIsLooping(isLooping);
        service.setRandom(isRandom);
        mf.setPlaylist(lastPlaylist);
        smf.setName(sPref.getString("nameOfUser", null));
        smf.setConnected(sPref.getBoolean("isUserConnected", false));
        smf.setRoom(sPref.getBoolean("isUserRoom", false));
        smf.setConnectedAddress(sPref.getString("connectedAddress", null));
        smf.setDelete(sPref.getBoolean("delete", false));
        smf.setPlayingToo(sPref.getBoolean("isPlayingToo", false));
        smf.setUUID(sPref.getString("UUID", null));
        smf.setMe(getResources().getString(R.string.name));
    }
    @Override
    protected void onDestroy() {
        unbindService(sConn);
        if(smf.isConnected() || smf.isRoom()) {
            MessageToWebSocket message = new MessageToWebSocket();
            message.setCommand("ondestroy");
            message.setUUID(smf.getUUID());
            message.setData("");
            ws.send(new Gson().toJson(message));
        }
        client.dispatcher().executorService().shutdown();
        super.onDestroy();
        save();
        progress.interrupt();
    }
    public void applyChanges(int back, int text){
        color = sf.getColorNum();
        cf.changeColor(text);
        mf.changeColor(text);
        pf.changeColor(text);
        smf.changeColor(text);
        viewPager.setBackgroundColor(back);
        tabs.setTabTextColors(text, text);
        tabs.setBackgroundColor(back);
        mf.setIsRandom(sf.getRan());
        service.setIsLooping(sf.getLoop());
        service.setRandom(sf.getRan());
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
                    if (root.isDirectory()) cf.new Open(root);
                } else{
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                }
        }
    }
    public void refresh(int n){
        sectionsPagerAdapter = new SectionsPagerAdapter(fm, sf, cf, mf, pf, smf, MainActivity.this);
        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(n);
    }
    public boolean isConnectedToTheInternet(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(!(activeNetwork != null && activeNetwork.isConnectedOrConnecting())) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.noInternet)
                    .setPositiveButton(getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            viewPager.setCurrentItem(3);
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        }
        return !(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }
    class checkUUID extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.65:8080")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            CommandToTheServer command = retrofit.create(CommandToTheServer.class);
            ArrayList<Object> params = new ArrayList<>();
            params.add(smf.getUUID());
            params.add(Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID));
            Call<String> generateUUID = command.generateUUID(Settings.Secure.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID));
            Call<Boolean> checkingUUID = command.checkUUID(params);
            try {
                Response<Boolean> response = checkingUUID.execute();
                if(!response.body()){
                    Response<String> newUUID = generateUUID.execute();
                    smf.setUUID(newUUID.body());
                    newUUIDH.sendEmptyMessage(1);
                }
            } catch (IOException e) {e.printStackTrace();}
            return null;
        }
    }
    private class EchoWebSocketListener extends WebSocketListener {
        private Handler refresh = new Handler(){
            public void handleMessage(android.os.Message msg){
                Toast.makeText(MainActivity.this, R.string.somethingCreateError, Toast.LENGTH_SHORT).show();
            }};
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            smf.gotMessageFromWebSocket(text);
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable okhttp3.Response response) {
            refresh.sendEmptyMessage(1);
            Log.e("WebSocket", t.toString());
        }
    }
    public void setNewPlaylistName(String s){pf.createNewPlaylist(s);}
    public void playlistFromNowPlays(String s){pf.newPlaylistfromNowPlays(mf.getPlaylist(), s);}
}