package com.example.rockbee;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class MusicFragment extends Fragment {
    private MediaPlayer mediaPlayer;
    ImageView prev, next, ps, back, forward;
    TextView nowPlays;
    ProgressBar progressBar;
    CatalogFragment cf;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music, container, false);
        prev = view.findViewById(R.id.prev);
        next = view.findViewById(R.id.next);
        ps = view.findViewById(R.id.ps);
        back = view.findViewById(R.id.back);
        forward = view.findViewById(R.id.forward);
        nowPlays = view.findViewById(R.id.nowPlay);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setMax(mediaPlayer.getDuration());
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.prev:
                        break;
                    case R.id.next:
                        break;
                    case R.id.ps:
                        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                        else mediaPlayer.start();
                        break;
                    case R.id.back:
                        break;
                    case R.id.forward:
                        break;
                    case R.id.progressBar:
                        mediaPlayer.seekTo(progressBar.getProgress());
                        Toast.makeText(getActivity(), "" + progressBar.getProgress(), Toast.LENGTH_SHORT).show();
                    default:
                        break;
                }
            }
        };
        prev.setOnClickListener(listener);
        next.setOnClickListener(listener);
        ps.setOnClickListener(listener);
        forward.setOnClickListener(listener);
        back.setOnClickListener(listener);
        progressBar.setOnClickListener(listener);
        return view;
    }
    public void setMediaPlayer(MediaPlayer mp) {
        mediaPlayer = mp;
    }
    public void setCatalogFragment(CatalogFragment fragment){cf = fragment;}
}
