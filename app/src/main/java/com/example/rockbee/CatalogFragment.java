package com.example.rockbee;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class CatalogFragment extends Fragment {
    private File root = new Environment().getExternalStorageDirectory(), parentDirectory = root, catalogForTemporaryMusic = new File(root.getAbsolutePath() + "/Temporary Music From RockBee");
    private ArrayList<File> files = new ArrayList<>(), playlist = new ArrayList<>();
    private ListView cg;
    private MediaPlayer mediaPlayer;
    private boolean isRandom = false;
    private int isLooping = 0;
    private SeekBar seekBar;
    private MusicFragment mf;
    private PlaylistFragment pf;
    private int color;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.catalog, container, false);
       cg = view.findViewById(R.id.catalog);
       if(!catalogForTemporaryMusic.exists()) catalogForTemporaryMusic.mkdir();
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
        CatalogAdapter adapter = new CatalogAdapter(getActivity(), files, "" + getResources().getText(R.string.cg), color);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(files.get(position).isDirectory()) {
                    parentDirectory = files.get(position);
                    openDirectory(files.get(position), lv);
                }
                else {
                    playMusic(files.get(position), playlist);
                    mf.setPlaylist(playlist);
                }
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if(files.get(position).isFile()){
                    new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.chooseAction))
                            .setItems(new String[]{getResources().getText(R.string.addToPlaylist) + "", getResources().getText(R.string.addToNowPlays) + "", getResources().getText(R.string.delete) + ""}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.choosePlaylist))
                                            .setItems(pf.getNames().toArray(new String[pf.getNames().size()]), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    pf.addNewSongToPlaylist(files.get(position), pf.getNames().get(which));
                                                }
                                            }).create().show();
                                    else if(which == 2) new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                                    .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            files.get(position).delete();
                                            files.remove(position);
                                            openDirectory(parentDirectory, cg);
                                        }
                                    })
                                    .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).create().show();
                                    else mf.addNewSongToNowPlays(files.get(position));
                                }
                            }).create().show();
                }
                else new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        files.get(position).delete();
                        files.remove(position);
                        openDirectory(parentDirectory, cg);
                    }
                })
                        .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
                return true;
            }
        });
    }

    public void playMusic(final File file, ArrayList<File> newPlaylist) {
        try {
            if(mf.getPS() != null) mf.getPS().setImageResource(R.drawable.ic_media_pause);
            final ArrayList<File> nowPlays = new ArrayList<>(newPlaylist);
            mf.setIsPlaying(file);
            mf.setName(file);
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar = mf.getSeekBar();
            mf.setMediaPlayer(mediaPlayer);
            if(seekBar != null){
                seekBar.setProgress(0);
                seekBar.setMax(mediaPlayer.getDuration());
                mf.resetTime();
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    seekBar.setProgress(0);
                    if (isRandom && (isLooping != 2)) playMusic(nowPlays.get((int) Math.round(Math.random() * (nowPlays.size() - 1))), nowPlays);
                    else if ((isLooping == 1 || nowPlays.indexOf(file) + 1 != nowPlays.size()) && isLooping != 2)
                        playMusic(nowPlays.get((nowPlays.indexOf(file) + 1) % nowPlays.size()), nowPlays);
                    else if(isLooping == 2) playMusic(file, nowPlays);
                }
            });
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Не воспроизводится!", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackPressed(){
        if(parentDirectory.equals(root)) getActivity().finish();
        else {
            openDirectory(parentDirectory.getParentFile(), cg);
            parentDirectory = parentDirectory.getParentFile();
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
    public void setPlaylistFragment(PlaylistFragment fragment) {pf = fragment;}
    public void changeColor(int text){
        color = text;
        if(cg != null) {
            CatalogAdapter adapter = new CatalogAdapter(getActivity(), files, "" + getResources().getText(R.string.cg), color);
            cg.setAdapter(adapter); }
    }
}
