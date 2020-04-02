package com.example.rockbee;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.TreeMap;

import static android.os.Environment.getExternalStorageDirectory;

public class CatalogFragment extends Fragment {
    private File root = getExternalStorageDirectory(), parentDirectory = root, catalogForTemporaryMusic = new File(root.getAbsolutePath() + "/Temporary Music From RockBee");
    private ArrayList<File> files = new ArrayList<>(), playlist = new ArrayList<>();
    private ListView cg;
    private MusicFragment mf;
    private PlaylistFragment pf;
    private int color;
    private FloatingActionButton back;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 0;
    private MediaPlayerService service;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.catalog, container, false);
       cg = view.findViewById(R.id.catalog);
       back = view.findViewById(R.id.catalogBack);
       back.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();
           }
       });
       if(!catalogForTemporaryMusic.exists()) catalogForTemporaryMusic.mkdir();
       if (root.isDirectory() && ContextCompat.checkSelfPermission(getActivity(),
               Manifest.permission.READ_EXTERNAL_STORAGE)
               == PackageManager.PERMISSION_GRANTED)openDirectory(root, cg);
       return view;
    }
    public void openDirectory(final File f, final ListView lv) {
        files.clear();
        playlist.clear();
        try {
            TreeMap<String, File> directories = new TreeMap<>();
            File[] filesTemp = f.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File file, String s) {
                    return new File(file.getAbsolutePath() + "/" + s).isDirectory();
                }
            });
            for (File file : filesTemp) {
                directories.put(file.getName(), file);
            }
            files.addAll(directories.values());
            directories = new TreeMap<>();
            filesTemp = f.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File file, String s) {
                    return (s.contains(".mp3") ||
                            s.contains(".ac3") ||
                            s.contains(".flac") ||
                            s.contains(".ogg") ||
                            s.contains(".wav") ||
                            s.contains(".wma"));
                }
            });
            for (File file : filesTemp) {
                directories.put(file.getName(), file);
            }
            for(File file: directories.values()) {
                files.add(file);
                if(file.isFile())playlist.add(file);
            }
        } catch (NullPointerException e) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);
            }
            else Toast.makeText(getActivity(), getResources().getText(R.string.emptyCatalog), Toast.LENGTH_LONG).show();
        }
        CatalogAdapter adapter = new CatalogAdapter(getActivity(), files, "" + getResources().getText(R.string.cg), color);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(files.get(position).isDirectory()) {
                    parentDirectory = files.get(position);
                    openDirectory(files.get(position), lv);
                    back.show();
                }
                else {
                    service.playMusic(files.get(position), playlist);
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
                                            .setItems(pf.getNames().toArray(new String[0]), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    pf.addNewSongToPlaylist(files.get(position), pf.getNames().get(which));
                                                }
                                            }).create().show();
                                    else if(which == 2) new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                                    .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(files.get(position).delete()) {
                                                files.remove(position);
                                                openDirectory(parentDirectory, cg);
                                            } else Toast.makeText(getActivity(), getResources().getText(R.string.cantDelete), Toast.LENGTH_SHORT).show();
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
                        if(files.get(position).delete()) {
                            files.remove(position);
                            openDirectory(parentDirectory, cg);
                        }
                        else Toast.makeText(getActivity(), getResources().getText(R.string.cantDelete), Toast.LENGTH_SHORT).show();
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
    public void onBackPressed(){
        if(parentDirectory.equals(root)) getActivity().finish();
        else {
            openDirectory(parentDirectory.getParentFile(), cg);
            parentDirectory = parentDirectory.getParentFile();
            if(parentDirectory.equals(root)) back.hide();
        }
    }
    public void setMusicFragment(MusicFragment fragment) {mf = fragment;}
    public void setPlaylistFragment(PlaylistFragment fragment) {pf = fragment;}
    public void changeColor(int text){
        color = text;
        if(cg != null) {
            CatalogAdapter adapter = new CatalogAdapter(getActivity(), files, "" + getResources().getText(R.string.cg), color);
            cg.setAdapter(adapter); }
    }
    public ListView getCg(){return cg;}
    public void setService(MediaPlayerService s){service = s;}
}
