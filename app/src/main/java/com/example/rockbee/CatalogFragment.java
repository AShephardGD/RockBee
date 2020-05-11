package com.example.rockbee;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
    private ServerMusicFragment smf;
    Handler emptyCatalog = new Handler(){
        public void handleMessage(android.os.Message msg) {
            Toast.makeText(getActivity(), getResources().getText(R.string.emptyCatalog), Toast.LENGTH_LONG).show();
        }
    }, catalogIsReady = new Handler(){
        public void handleMessage(android.os.Message msg){
            CatalogAdapter adapter = new CatalogAdapter(getActivity(), files, "" + getResources().getText(R.string.cg), color);
            cg.setAdapter(adapter);
            cg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(files.get(position).isDirectory()) {
                        parentDirectory = files.get(position);
                        new Open(files.get(position)).start();
                        cg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        });
                        cg.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                return false;
                            }
                        });
                        back.show();
                    }
                    else {
                        if(!smf.isConnected() && !smf.isRoom())service.playMusic(files.get(position), playlist);
                        else Toast.makeText(getActivity(), getResources().getString(R.string.cantPlayWithServer), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            cg.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    if(files.get(position).isFile()) {
                        if (smf.isConnected() || smf.isRoom()) {
                            new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.chooseAction))
                                    .setItems(new String[]{getResources().getString(R.string.addToPlaylist), getResources().getString(R.string.addToNowPlays), getResources().getString(R.string.addToTheServer), getResources().getString(R.string.delete)}, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0)
                                                new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.choosePlaylist))
                                                        .setItems(pf.getNames().toArray(new String[0]), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                pf.addNewSongToPlaylist(files.get(position), pf.getNames().get(which));
                                                            }
                                                        }).create().show();
                                            else if (which == 2)
                                                smf.addToThePlaylist(files.get(position));
                                            else if (which == 3)
                                                new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                                                        .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if (files.get(position).delete()) {
                                                                    files.remove(position);
                                                                    new Open(parentDirectory).start();
                                                                } else
                                                                    Toast.makeText(getActivity(), getResources().getText(R.string.cantDelete), Toast.LENGTH_SHORT).show();
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
                        } else {
                            new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.chooseAction))
                                    .setItems(new String[]{getResources().getString(R.string.addToPlaylist), getResources().getString(R.string.addToNowPlays),  getResources().getString(R.string.delete)}, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0)
                                                new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.choosePlaylist))
                                                        .setItems(pf.getNames().toArray(new String[0]), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                pf.addNewSongToPlaylist(files.get(position), pf.getNames().get(which));
                                                            }
                                                        }).create().show();
                                            else if (which == 2)
                                                new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                                                        .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if (files.get(position).delete()) {
                                                                    files.remove(position);
                                                                    new Open(parentDirectory).start();
                                                                } else
                                                                    Toast.makeText(getActivity(), getResources().getText(R.string.cantDelete), Toast.LENGTH_SHORT).show();
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
                    }
                    return true;
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.catalog, container, false);
        cg = view.findViewById(R.id.catalog);
        back = view.findViewById(R.id.catalogBack);
        back.setOnClickListener(v -> onBackPressed());
        if (!catalogForTemporaryMusic.exists()) catalogForTemporaryMusic.mkdir();
        if (root.isDirectory() && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) new Open(parentDirectory).start();
        return view;
    }
    public void onBackPressed() {
        if (parentDirectory.equals(root)) getActivity().finish();
        else {
            if (parentDirectory.getParentFile().equals(root)) back.hide();
            new Open(parentDirectory.getParentFile()).start();
            parentDirectory = parentDirectory.getParentFile();
        }
    }
    public void changeColor(int text) {
        color = text;
        if (cg != null) {
            CatalogAdapter adapter = new CatalogAdapter(getActivity(), files, "" + getResources().getText(R.string.cg), color);
            cg.setAdapter(adapter);
        }
    }
    public void setService(MediaPlayerService s) {service = s;}
    public class Open extends Thread {
        File file;
        public Open(File f) {
            file = f;
        }
        @Override
        public void run() {
            files.clear();
            playlist.clear();
            try {
                TreeMap<String, File> directories = new TreeMap<>();
                File[] filesTemp = file.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        File f = new File(file.getAbsolutePath() + "/" + s);
                        return f.isDirectory() && isThereSomeMusic(f);
                    }
                });
                for (File file : filesTemp) {
                    directories.put(file.getName(), file);
                }
                files.addAll(directories.values());
                directories = new TreeMap<>();
                filesTemp = file.listFiles(new FilenameFilter() {
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
                else emptyCatalog.sendEmptyMessage(0);
            }
            catalogIsReady.sendEmptyMessage(0);
        }
    }
    public boolean isThereSomeMusic(File file){
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
                if (f.isDirectory()) {
                    if(isThereSomeMusic(f)) return true;
                }
                else return true;
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
        return false;
    }
    public void setMusicFragment(MusicFragment fragment) {mf = fragment;}
    public void setPlaylistFragment(PlaylistFragment fragment) {pf = fragment;}
    public void setServerMusicFragment(ServerMusicFragment fragment){smf = fragment;}
}
