package com.example.rockbee;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;

public class MusicFragment extends Fragment {
    private MediaPlayer mediaPlayer;
    private ImageView prev, next, ps, back, forward;
    private ArrayList<File> playlist = new ArrayList<>();
    private ListView nowPlays;
    private SeekBar seekBar = null;
    private CatalogFragment cf;
    private TextView time, name;
    private int nowSec, nowMin, min, sec;
    private boolean isRandom;
    private LookingForProgress progress;
    private File isPlaying = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music, container, false);
        name = view.findViewById(R.id.SongName);
        prev = view.findViewById(R.id.prev);
        next = view.findViewById(R.id.next);
        ps = view.findViewById(R.id.ps);
        back = view.findViewById(R.id.back);
        forward = view.findViewById(R.id.forward);
        seekBar = view.findViewById(R.id.seekBar);
        time = view.findViewById(R.id.time);
        prev.setImageResource(R.drawable.ic_media_previous);
        next.setImageResource(R.drawable.ic_media_next);
        if(mediaPlayer.isPlaying())ps.setImageResource(R.drawable.ic_media_pause);
        else ps.setImageResource(R.drawable.ic_media_play);
        back.setImageResource(R.drawable.ic_media_rew);
        forward.setImageResource(R.drawable.ic_media_ff);
        resetTime();
        if(isPlaying != null) name.setText(isPlaying.getName());
        else name.setText(getResources().getText(R.string.emptyPlaylist));
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        nowPlays = view.findViewById(R.id.MusicListView);
        CatalogAdapter adapter = new CatalogAdapter(getActivity(), playlist, "" + getResources().getText(R.string.cg));
        nowPlays.setAdapter(adapter);
        nowPlays.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cf.playMusic(playlist.get(position));
                resetTime();
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int delta;
                switch(v.getId()){
                    case R.id.prev:
                        ps.setImageResource(R.drawable.ic_media_pause);
                        int index;
                        if(isRandom) cf.playMusic(playlist.get((int) Math.round(Math.random() * (playlist.size() - 1))));
                        else {
                            index = (playlist.indexOf(isPlaying) - 1);
                            if(index < 0) index = playlist.size() - 1;
                            cf.playMusic(playlist.get(index));
                        }
                        break;
                    case R.id.next:
                        ps.setImageResource(R.drawable.ic_media_pause);
                        if(isRandom)cf.playMusic(playlist.get((int) Math.round(Math.random() * (playlist.size() - 1))));
                        else cf.playMusic(playlist.get((playlist.indexOf(isPlaying) + 1) % playlist.size()));
                        break;
                    case R.id.ps:
                        if (mediaPlayer.isPlaying()) {
                            ps.setImageResource(R.drawable.ic_media_play);
                            mediaPlayer.pause();
                        }
                        else if(isPlaying != null) {
                            ps.setImageResource(R.drawable.ic_media_pause);
                            mediaPlayer.start();
                        }
                        break;
                    case R.id.back:
                        delta = mediaPlayer.getCurrentPosition() - 10000;
                        if (delta > 0) mediaPlayer.seekTo(delta);
                        else mediaPlayer.seekTo(0);
                        break;
                    case R.id.forward:
                        delta = mediaPlayer.getCurrentPosition() + 10000;
                        if(delta < mediaPlayer.getDuration())mediaPlayer.seekTo(delta);
                        else mediaPlayer.seekTo(mediaPlayer.getDuration() - 1);
                        break;
                    default:
                        break;
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
                mediaPlayer.seekTo(seekBar.getProgress());
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
    public void setMediaPlayer(MediaPlayer mp) {
        mediaPlayer = mp;
    }
    public void setCatalogFragment(CatalogFragment fragment){cf = fragment;}
    public void setPlaylist(ArrayList<File> play) {playlist = new ArrayList<>(play);}
    public SeekBar getSeekBar(){return seekBar;}
    public void resetTime(){
        sec = (mediaPlayer.getDuration() / 1000) % 60;
        min = mediaPlayer.getDuration() / 60000;
        nowSec = (mediaPlayer.getCurrentPosition() / 1000) % 60;
        nowMin = mediaPlayer.getCurrentPosition() / 60000;
        try {
            if (nowSec / 10 == 0 && sec / 10 == 0)
                time.setText(nowMin + ":0" + nowSec + "/" + min + ":0" + sec);
            else if (sec / 10 == 0) time.setText(nowMin + ":" + nowSec + "/" + min + ":0" + sec);
            else if (nowSec / 10 == 0) time.setText(nowMin + ":0" + nowSec + "/" + min + ":" + sec);
            else time.setText(nowMin + ":" + nowSec + "/" + min + ":" + sec);
            seekBar.setProgress(mediaPlayer.getCurrentPosition());

        } catch (NullPointerException e) {}
    }
    public void setThread(LookingForProgress thread) {progress = thread;}
    public void setIsRandom(boolean random) {isRandom = random;}
    public void setIsPlaying(File play) {isPlaying = play;}
    public void setName(File file){
        if(name != null) name.setText(file.getName());
    }
    public ImageView getPS(){return ps;}
}
