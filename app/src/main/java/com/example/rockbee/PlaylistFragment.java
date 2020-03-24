package com.example.rockbee;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class PlaylistFragment extends Fragment {
    private ListView listView;
    private TreeMap<String, ArrayList<File>> playlists = new TreeMap<>();
    private ArrayList<String> names;
    private ArrayList<Integer> sizeOfPlaylist;
    private ArrayList<File> tmpPlaylist;
    private CatalogFragment cf;
    private int num = 0, color;
    private MusicFragment mf;
    private FloatingActionButton fab;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.playlists, container, false);
        listView = view.findViewById(R.id.playlists);
        names = new ArrayList<>();
        sizeOfPlaylist = new ArrayList<>();
        for(Map.Entry<String, ArrayList<File>> entry: playlists.entrySet()){
            names.add(entry.getKey());
            sizeOfPlaylist.add(entry.getValue().size());
        }
        fab = view.findViewById(R.id.fab);
        PlaylistsAdapter adapter = new PlaylistsAdapter(getActivity(), names, "" + getResources().getText(R.string.cg), sizeOfPlaylist, color);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                fab.hide();
                tmpPlaylist = new ArrayList<>(playlists.get(names.get(position)));
                num = 1;
                CatalogAdapter adapter = new CatalogAdapter(getActivity(), tmpPlaylist, "" + getResources().getText(R.string.cg), color);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        cf.playMusic(tmpPlaylist.get(position), tmpPlaylist);
                        mf.setPlaylist(tmpPlaylist);
                    }
                });
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position1, long id) {
                        new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                                .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        tmpPlaylist.remove(position1);
                                        CatalogAdapter adapter = new CatalogAdapter(getActivity(), tmpPlaylist, "" + getResources().getText(R.string.cg), color);
                                        listView.setAdapter(adapter);
                                        playlists.put(names.get(position), tmpPlaylist);
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
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                        .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                playlists.remove(names.get(position));
                                names = new ArrayList<>();
                                sizeOfPlaylist = new ArrayList<>();
                                for(Map.Entry<String, ArrayList<File>> entry: playlists.entrySet()){
                                    names.add(entry.getKey());
                                    sizeOfPlaylist.add(entry.getValue().size());
                                }
                                PlaylistsAdapter adapter = new PlaylistsAdapter(getActivity(), names, "" + getResources().getText(R.string.cg), sizeOfPlaylist, color);
                                listView.setAdapter(adapter);
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).newPlaylist();
            }
        });
        return view;
    }
    public void createNewPlaylist(String s){
        if(!playlists.containsKey(s) && !s.equals("")) {
            playlists.put(s, new ArrayList<File>());
            names = new ArrayList<>();
            sizeOfPlaylist = new ArrayList<>();
            for (Map.Entry<String, ArrayList<File>> entry : playlists.entrySet()) {
                names.add(entry.getKey());
                sizeOfPlaylist.add(entry.getValue().size());
            }
            PlaylistsAdapter adapter = new PlaylistsAdapter(getActivity(), names, "" + getResources().getText(R.string.cg), sizeOfPlaylist, color);
            listView.setAdapter(adapter);
        } else if(s.equals("")) Toast.makeText(getActivity(), "" + getResources().getText(R.string.noNamePlaylist), Toast.LENGTH_SHORT).show();
        else Toast.makeText(getActivity(), "" + getResources().getText(R.string.playlistAlreadyExists), Toast.LENGTH_SHORT).show();
    }
    public void setCatalogFragment(CatalogFragment fragment) {cf = fragment;}
    public void onBackPressed(){
        if(num == 1){
            num = 0;
            fab.show();
            names = new ArrayList<>();
            sizeOfPlaylist = new ArrayList<>();
            for(Map.Entry<String, ArrayList<File>> entry: playlists.entrySet()){
                names.add(entry.getKey());
                sizeOfPlaylist.add(entry.getValue().size());
            }
            PlaylistsAdapter adapter = new PlaylistsAdapter(getActivity(), names, "" + getResources().getText(R.string.cg), sizeOfPlaylist, color);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position1, long id) {
                    fab.hide();
                    tmpPlaylist = new ArrayList<>(playlists.get(names.get(position1)));
                    num = 1;
                    CatalogAdapter adapter = new CatalogAdapter(getActivity(), tmpPlaylist, "" + getResources().getText(R.string.cg), color);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            cf.playMusic(tmpPlaylist.get(position), tmpPlaylist);
                            mf.setPlaylist(tmpPlaylist);
                        }
                    });
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                            new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                                    .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            tmpPlaylist.remove(position1);
                                            CatalogAdapter adapter = new CatalogAdapter(getActivity(), tmpPlaylist, "" + getResources().getText(R.string.cg), color);
                                            listView.setAdapter(adapter);
                                            playlists.put(names.get(position), tmpPlaylist);
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
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    new AlertDialog.Builder(getActivity()).setTitle(getResources().getText(R.string.deleteQ))
                            .setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    playlists.remove(names.get(position));
                                    names = new ArrayList<>();
                                    sizeOfPlaylist = new ArrayList<>();
                                    for(Map.Entry<String, ArrayList<File>> entry: playlists.entrySet()){
                                        names.add(entry.getKey());
                                        sizeOfPlaylist.add(entry.getValue().size());
                                    }
                                    PlaylistsAdapter adapter = new PlaylistsAdapter(getActivity(), names, "" + getResources().getText(R.string.cg), sizeOfPlaylist, color);
                                    listView.setAdapter(adapter);
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
        else getActivity().finish();
    }
    public void addNewSongToPlaylist(File file, String s){
        ArrayList<File> temp = playlists.get(s);
        temp.add(file);
        playlists.put(s, temp);
    }
    public ArrayList<String> getNames(){
        names = new ArrayList<>();
        for(Map.Entry<String, ArrayList<File>> entry: playlists.entrySet()){
            names.add(entry.getKey());
        }
        return names;
    }
    public void setMusicFragment(MusicFragment fragment){mf = fragment;}
    public TreeMap<String, ArrayList<File>> getPlaylists() {return playlists; }
    public void setPlaylists(TreeMap<String, ArrayList<File>> playlists) {this.playlists = playlists; }
    public void newPlaylistfromNowPlays(ArrayList<File> np, String s){
        if(!playlists.containsKey(s) && !s.equals("")) {
            playlists.put(s, new ArrayList<>(np));
            names = new ArrayList<>();
            sizeOfPlaylist = new ArrayList<>();
            for (Map.Entry<String, ArrayList<File>> entry : playlists.entrySet()) {
                names.add(entry.getKey());
                sizeOfPlaylist.add(entry.getValue().size());
            }
            PlaylistsAdapter adapter = new PlaylistsAdapter(getActivity(), names, "" + getResources().getText(R.string.cg), sizeOfPlaylist, color);
            listView.setAdapter(adapter);
        } else if(s.equals("")) Toast.makeText(getActivity(), "" + getResources().getText(R.string.noNamePlaylist), Toast.LENGTH_SHORT).show();
        else Toast.makeText(getActivity(), "" + getResources().getText(R.string.playlistAlreadyExists), Toast.LENGTH_SHORT).show();
    }
    public void changeColor(int text) {color = text;}
}
