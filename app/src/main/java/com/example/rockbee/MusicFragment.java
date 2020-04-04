package com.example.rockbee;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.TreeMap;

public class MusicFragment extends Fragment {
    private ImageView prev, next, ps, back, forward;
    private ArrayList<File> playlist = new ArrayList<>();
    private ListView nowPlays;
    private SeekBar seekBar = null;
    private CatalogFragment cf;
    private TextView time, name;
    private int nowSec, nowMin, min, sec, color, max;
    private boolean isRandom;
    private LookingForProgress progress;
    private File isPlaying = null;
    private FloatingActionButton fab;
    private TreeMap<String, File> music;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 0;
    private MediaPlayerService service;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music, container, false);
        name = view.findViewById(R.id.SongName);
        prev = view.findViewById(R.id.prev);
        next = view.findViewById(R.id.next);
        ps = view.findViewById(R.id.ps);
        fab = view.findViewById(R.id.fab3);
        back = view.findViewById(R.id.back);
        forward = view.findViewById(R.id.forward);
        seekBar = view.findViewById(R.id.seekBar);
        time = view.findViewById(R.id.time);
        prev.setImageResource(R.drawable.ic_media_previous);
        next.setImageResource(R.drawable.ic_media_next);
        back.setImageResource(R.drawable.ic_media_rew);
        forward.setImageResource(R.drawable.ic_media_ff);
        resetTime();
        time.setTextColor(color);
        name.setTextColor(color);
        if(service.isPlaying())ps.setImageResource(R.drawable.ic_media_pause);
        else ps.setImageResource(R.drawable.ic_media_play);
        seekBar.setMax(service.getDuration());
        seekBar.setProgress(service.getCurrentPosition());
        if(isPlaying != null) setIsPlaying(isPlaying);
        else name.setText(getResources().getText(R.string.emptyPlaylist));
        nowPlays = view.findViewById(R.id.MusicListView);
        CatalogAdapter adapter = new CatalogAdapter(getActivity(), playlist, "" + getResources().getText(R.string.cg), color);
        nowPlays.setAdapter(adapter);
        nowPlays.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                service.playMusic(playlist.get(position), playlist);
                resetTime();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                music = new TreeMap<>();
                playlist = new ArrayList<>();
                playAllMusic(new Environment().getExternalStorageDirectory());
                playlist.addAll(music.values());
                try {
                    service.playMusic(playlist.get((int) Math.round(Math.random() * (playlist.size() - 1))), playlist);
                    CatalogAdapter adapter = new CatalogAdapter(getActivity(), playlist, "" + getResources().getText(R.string.cg), color);
                    nowPlays.setAdapter(adapter);
                } catch (IndexOutOfBoundsException e){}
            }
        });
        nowPlays.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteFromNowPlays))
                        .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isPlaying.equals(playlist.get(position))) next.performClick();
                        playlist.remove(position);
                        CatalogAdapter adapter = new CatalogAdapter(getActivity(), playlist, "" + getResources().getText(R.string.cg), color);
                        nowPlays.setAdapter(adapter);
                    }
                }).create().show();
                return true;
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int delta;
                if(isPlaying != null) {
                     switch (v.getId()) {
                         case R.id.prev:
                             service.prev();
                             break;
                         case R.id.next:
                             service.next();
                             break;
                         case R.id.ps:
                             if (service.isPlaying())service.pause();
                             else service.play();
                             break;
                         case R.id.back:
                             delta = service.getCurrentPosition() - 10000;
                             if (delta > 0) service.seekTo(delta);
                             else service.seekTo(0);
                             break;
                         case R.id.forward:
                             delta = service.getCurrentPosition() + 10000;
                             if (delta < service.getDuration()) service.seekTo(delta);
                             else service.seekTo(service.getDuration() - 1);
                             break;
                         default:
                     }
                 }
            }
        };
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progress.changeMode();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                service.seekTo(seekBar.getProgress());
                resetTime();
                progress.changeMode();
            }
        });
        prev.setOnClickListener(listener);
        next.setOnClickListener(listener);
        ps.setOnClickListener(listener);
        forward.setOnClickListener(listener);
        back.setOnClickListener(listener);
        return view;
    }
    public void setCatalogFragment(CatalogFragment fragment){cf = fragment;}
    public void setPlaylist(ArrayList<File> play) {
        playlist = new ArrayList<>(play);
        if(nowPlays != null){
            CatalogAdapter adapter = new CatalogAdapter(getActivity(), playlist, "" + getResources().getText(R.string.cg), color);
            nowPlays.setAdapter(adapter);
        }
    }
    public void resetTime(){
        int now = service.getCurrentPosition();
        sec = (max / 1000) % 60;
        min =  max / 60000;
        nowSec = (now / 1000) % 60;
        nowMin = now / 60000;
        try {
            if (nowSec / 10 == 0 && sec / 10 == 0)
                time.setText(nowMin + ":0" + nowSec + "/" + min + ":0" + sec);
            else if (sec / 10 == 0) time.setText(nowMin + ":" + nowSec + "/" + min + ":0" + sec);
            else if (nowSec / 10 == 0) time.setText(nowMin + ":0" + nowSec + "/" + min + ":" + sec);
            else time.setText(nowMin + ":" + nowSec + "/" + min + ":" + sec);
            seekBar.setProgress(now);
        } catch (NullPointerException e) {
            Log.e("MusicFragment", "resetTime:" + e.toString());
        }
    }
    public void setThread(LookingForProgress thread) {progress = thread;}
    public void setIsRandom(boolean random) {isRandom = random;}
    public void setIsPlaying(File play) {
        isPlaying = play;
        if(isPlaying != null) {
            String songName = play.getName();
            if (name != null) name.setText(songName.substring(0, songName.lastIndexOf(".")));
        }
    }
    public void addNewSongToNowPlays(File file){
        playlist.add(file);
        if(nowPlays != null){
            CatalogAdapter adapter = new CatalogAdapter(getActivity(), playlist, "" + getResources().getText(R.string.cg), color);
            nowPlays.setAdapter(adapter);
        }
    }
    public ArrayList<File> getPlaylist(){return playlist;}
    public void changeColor(int text) {color = text;}
    public void playAllMusic(File file){
        try {
            File[] files = file.listFiles(new FilenameFilter() {
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
            });
            for (File f : files) {
                if (f.isDirectory()) playAllMusic(f);
                else music.put(f.getName(), f);
            }
        } catch (NullPointerException e){
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);
            }
            else Toast.makeText(getActivity(), getResources().getText(R.string.cantPlay), Toast.LENGTH_SHORT).show();
        }
    }
    public void setService(MediaPlayerService s){service = s;}
    public void setMax(){
        if(seekBar != null) {
            seekBar.setProgress(0);
            seekBar.setMax(service.getDuration());
        }
        max = service.getDuration();
    }
    public void setPlay(){if(ps != null) ps.setImageResource(R.drawable.ic_media_play);}
    public void setPause(){if(ps != null) ps.setImageResource(R.drawable.ic_media_pause);}
}
