package com.example.rockbee;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerMusicFragment extends Fragment {
    private int color;
    private String name = null, connectedAddress = null, UUID = null, password = null, UUIDConnectedRoom;
    private boolean isConnected = false, isRoom = false, delete = false, playingToo = false, isName = false, isTracks = true;
    private ArrayList<File> playlist = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private MediaPlayerService service;
    private User me;
    private Integer pass = 0, all = 0;
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.1.65:8080")
            .addConverterFactory(new NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .setLenient()
                    .create()))
            .build();
    private CommandToTheServer commands = retrofit.create(CommandToTheServer.class);
    private Handler h = new Handler(){
        public void handleMessage(android.os.Message msg){
            Toast.makeText(getActivity(), R.string.somethingCreateError, Toast.LENGTH_SHORT).show();
        }}, wrongPassword = new Handler(){
        public void handleMessage(android.os.Message msg){
            Toast.makeText(getActivity(), R.string.wrongPassword, Toast.LENGTH_SHORT).show();
        }}, wrongRoom = new Handler(){
        public void handleMessage(android.os.Message msg){
            Toast.makeText(getActivity(), R.string.wrongRoom, Toast.LENGTH_SHORT).show();
        }}, closedRoom = new Handler(){
        public void handleMessage(android.os.Message msg){
            Toast.makeText(getActivity(), R.string.somethingCreateError, Toast.LENGTH_SHORT).show();
        }}, refresh = new Handler(){
        public void handleMessage(android.os.Message msg){
            ((MainActivity) getActivity()).refresh(4);
        }}, notMaked = new Handler(){
        public void handleMessage(android.os.Message msg){
            Toast.makeText(getActivity(), "Не получилось", Toast.LENGTH_SHORT).show();
        }}, newPassword = new Handler(){
        public void handleMessage(android.os.Message msg){
            String p;
            if(password.equals(""))p = getResources().getString(R.string.noPassword);
            else p = " \"" +  password + "\"";
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.info)
                    .setMessage(getResources().getString(R.string.friendUUID) + UUIDConnectedRoom + "\n" +
                            getResources().getString(R.string.password) + p + "\n" +
                            getResources().getString(R.string.against) + pass + "/" + all)
                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .create()
                    .show();
        }
    };
    private TextView text;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(name != null) isName = true;
        View view = null;
        if (!isRoom && !isConnected && isName){ //Started menu
            view = inflater.inflate(R.layout.servermusicfragment_noroom, container, false);
            text = view.findViewById(R.id.nameAndUUID);
            text.setText(getResources().getText(R.string.name) + " " + name + "\nUUID: " + UUID);
            text.setTextColor(color);
            Button connect = view.findViewById(R.id.foundFriendRoom), create = view.findViewById(R.id.createMyRoom), change = view.findViewById(R.id.changeMyName);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((MainActivity)getActivity()).isConnectedToTheInternet()) return;
                    EditText text1;
                    View view1;
                    switch(v.getId()){
                        case R.id.foundFriendRoom:
                            view1 = getLayoutInflater().inflate(R.layout.findroom, null);
                            text1 = view1.findViewById(R.id.roomUUID);
                            EditText text2 = view1.findViewById(R.id.roomPass);
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.findByNameOrUUID)
                                    .setPositiveButton(R.string.ready, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            UUIDConnectedRoom = text1.getText().toString();
                                            password = text2.getText().toString();
                                            new ChosenCommand(1).execute();
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setView(view1)
                                    .create()
                                    .show();
                            break;
                        case R.id.createMyRoom:
                            view1 = inflater.inflate(R.layout.playlists_alert_dialog, null);
                            text1 = view1.findViewById(R.id.newPlaylistName);
                            text1.setHint(R.string.enterPass);
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.passForRoom)
                                    .setPositiveButton(R.string.ready, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            password = text1.getText().toString();
                                            new ChosenCommand(0).execute();
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {}
                                    })
                                    .setView(view1)
                                    .create()
                                    .show();
                            break;
                        case R.id.changeMyName:
                            view1 = getLayoutInflater().inflate(R.layout.playlists_alert_dialog, null);
                            text1 = view1.findViewById(R.id.newPlaylistName);
                            text1.setHint(R.string.enterName);
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.name)
                                    .setPositiveButton(R.string.ready, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String s = text1.getText().toString();
                                            if(s.equals("")) {
                                                Toast.makeText(getActivity(), R.string.noName, Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                setName(s);
                                                setMe(getResources().getString(R.string.name));
                                                text.setText(me.toString());
                                            }
                                        }
                                    })
                                    .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setView(view1)
                                    .create()
                                    .show();
                            break;
                    }
                }
            };
            connect.setOnClickListener(listener);
            create.setOnClickListener(listener);
            change.setOnClickListener(listener);
        } else if(isRoom && isName){//My room created
            view = inflater.inflate(R.layout.servermusicfragment_myroom, container, false);
            ListView myTracks = view.findViewById(R.id.myTracks);
            myTracks.setAdapter(new CatalogAdapter(getActivity(), playlist, getResources().getString(R.string.cg), color));
            Switch myDelete = view.findViewById(R.id.myDelete);
            myDelete.setChecked(delete);
            Button closeTheRoom = view.findViewById(R.id.closeTheRoom), myUserList = view.findViewById(R.id.myUserList), myPassButton = view.findViewById(R.id.myPassButton), myInfo = view.findViewById(R.id.myInfo), setPass = view.findViewById(R.id.setPass);
            myDelete.setChecked(delete);
            myDelete.setTextColor(color);
            users.add(me);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
                        case R.id.myDelete:
                            delete = myDelete.isChecked();
                            break;
                        case R.id.myPassButton:
                            new ChosenCommand(4).execute();
                            break;
                        case R.id.myUserList:
                            if(isTracks){
                                isTracks = false;
                                myUserList.setText(R.string.tracks);
                                ArrayList<String> list = new ArrayList<>();
                                for(User u: users)list.add(u.toString());
                                myTracks.setAdapter(new UserAdapter(getActivity(), list, color));
                            } else {
                                isTracks = true;
                                myUserList.setText(R.string.listUsers);
                                myTracks.setAdapter(new CatalogAdapter(getActivity(), playlist, getResources().getString(R.string.cg), color));
                            }
                            break;
                        case R.id.closeTheRoom:
                            new ChosenCommand(3).execute();
                            break;
                        case R.id.setPass:
                            View view1 = getLayoutInflater().inflate(R.layout.playlists_alert_dialog, null);
                            EditText text1 = view1.findViewById(R.id.newPlaylistName);
                            text1.setHint(R.string.enterPass);
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.setPass)
                                    .setPositiveButton(R.string.ready, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            password = text1.getText().toString();
                                            new ChangePassword(password).execute();
                                        }
                                    })
                                    .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setView(view1)
                                    .create()
                                    .show();
                            break;
                        case R.id.myInfo:
                            String p;
                            if(password.equals(""))p = getResources().getString(R.string.noPassword);
                            else p = " \""+ password + "\"";
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.info)
                                    .setMessage(getResources().getString(R.string.yourUUID) + UUID + "\n" +
                                            getResources().getString(R.string.password) +  p  + "\n" +
                                            getResources().getString(R.string.against) + pass + "/" + all)
                                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {}
                                    })
                                    .create()
                                    .show();
                            break;
                    }
                }
            };
            closeTheRoom.setOnClickListener(listener);
            myUserList.setOnClickListener(listener);
            myPassButton.setOnClickListener(listener);
            myDelete.setOnClickListener(listener);
            myInfo.setOnClickListener(listener);
            setPass.setOnClickListener(listener);
        } else if (isName){ //Connected to another
            view = inflater.inflate(R.layout.servermusicfragment_friendroom, container, false);
            ListView friendsTracks = view.findViewById(R.id.friendTracks);
            friendsTracks.setAdapter(new CatalogAdapter(getActivity(), playlist, getResources().getString(R.string.cg), color));
            Button friendPassButton = view.findViewById(R.id.friendPassButton),
                    friendUserList = view.findViewById(R.id.friendUserList),
                    leave = view.findViewById(R.id.leave),
                    playButton = view.findViewById(R.id.playButton),
                    friendInfo = view.findViewById(R.id.friendInfo);
            if(!playingToo){
                playButton.setText(R.string.playToo);
            } else {
                playButton.setText(R.string.stopPlaying);
            }
            Switch friendDelete = view.findViewById(R.id.friendDelete);
            friendDelete.setChecked(delete);
            friendDelete.setTextColor(color);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
                        case R.id.playButton:
                            if(playingToo){
                                playButton.setText(R.string.playToo);
                                playingToo = false;
                            } else {
                                playButton.setText(R.string.stopPlaying);
                                playingToo = true;
                            }
                            break;
                        case R.id.friendPassButton:
                            new ChosenCommand(4).execute();
                            break;
                        case R.id.friendUserList:
                            if(isTracks){
                                isTracks = false;
                                friendUserList.setText(R.string.tracks);
                                ArrayList<String> list = new ArrayList<>();
                                for(User u: users)list.add(u.toString());
                                friendsTracks.setAdapter(new UserAdapter(getActivity(), list, color));
                            } else {
                                isTracks = true;
                                friendUserList.setText(R.string.listUsers);
                                friendsTracks.setAdapter(new CatalogAdapter(getActivity(), playlist, getResources().getString(R.string.cg), color));
                            }
                            break;
                        case R.id.leave:
                            new ChosenCommand(2).execute();
                            break;
                        case R.id.friendDelete:
                            delete = friendDelete.isChecked();
                            break;
                        case R.id.friendInfo:
                            new ChosenCommand(5).execute();
                            break;
                    }
                }
            };
            playButton.setOnClickListener(listener);
            friendPassButton.setOnClickListener(listener);
            friendUserList.setOnClickListener(listener);
            leave.setOnClickListener(listener);
            friendDelete.setOnClickListener(listener);
            friendInfo.setOnClickListener(listener);
        }
        return view;
    }
    class ChosenCommand extends AsyncTask{
        int command;
        public ChosenCommand(int i) {command = i;}
        @Override
        protected Object doInBackground(Object[] objects){
            ArrayList<Object> params = new ArrayList<>();
            if(command == 0){//Create new room
                params.add(me.getName());
                params.add(me.getUUID());
                params.add(password);
                Call<Boolean> call = commands.createNewRoom(params);
                try{
                    Response<Boolean> response = call.execute();
                    if(response.body()){
                        UUIDConnectedRoom = UUID;
                        service.connected();
                        isRoom = true;
                        refresh.sendEmptyMessage(1);
                    } else notMaked.sendEmptyMessage(1);
                } catch (IOException e) {
                    e.printStackTrace();
                    h.sendEmptyMessage(1);
                } catch (NullPointerException e){h.sendEmptyMessage(1);}
            } else if(command == 1){//Connect to the server
                params.add(command);
                params.add(UUIDConnectedRoom);
                params.add(me.getName());
                params.add(me.getUUID());
                params.add(password);
                Call<Integer> call = commands.command(params);
                try{
                    Response<Integer> response = call.execute();
                    if(response.body() == 0){
                        isConnected = true;
                        service.connected();
                        refresh.sendEmptyMessage(1);
                    }
                    else if(response.body() == 1) wrongPassword.sendEmptyMessage(1);
                    else if(response.body() == 2) wrongRoom.sendEmptyMessage(1);
                    else h.sendEmptyMessage(1);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){h.sendEmptyMessage(1);}
            } else if(command == 2){//Disconnect
                params.add(me.getName());
                params.add(me.getUUID());
                Call<Integer> call = commands.command(params);
                try{
                    Response<Integer> response = call.execute();
                    if(response.body() == 0){
                        isConnected = false;
                        playingToo = false;
                        delete = false;
                        UUIDConnectedRoom = null;
                        refresh.sendEmptyMessage(1);
                    } else notMaked.sendEmptyMessage(1);
                } catch (IOException e) {
                    e.printStackTrace();
                    h.sendEmptyMessage(1);
                } catch (NullPointerException e){h.sendEmptyMessage(1);}
            } else if(command == 3) {//Close the room
                params.add(command);
                params.add(UUIDConnectedRoom);
                Call<Integer> call = commands.command(params);
                try{
                    Response<Integer> response = call.execute();
                    if(response.body() == 0){
                        isRoom = false;
                        playingToo = false;
                        delete = false;
                        UUIDConnectedRoom = null;
                        refresh.sendEmptyMessage(1);
                    } else notMaked.sendEmptyMessage(1);
                } catch (IOException e) {
                    e.printStackTrace();
                    h.sendEmptyMessage(1);
                } catch (NullPointerException e){h.sendEmptyMessage(1);}
            } else if(command == 4){ //user pass
                params.add(UUIDConnectedRoom);
                params.add(UUID);
                Call<Boolean> call = commands.userpass(params);
                try {
                    Response<Boolean> response = call.execute();
                    if(!response.body()) notMaked.sendEmptyMessage(1);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){h.sendEmptyMessage(1);}
            } else if(command == 5){//get info
                Call<String> call = commands.getInfo(UUIDConnectedRoom);
                try{
                    Response<String> response = call.execute();
                    if(response.body() != null){
                        password = response.body();
                        newPassword.sendEmptyMessage(1);
                    }
                    else {
                        password = "";
                        newPassword.sendEmptyMessage(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    class AddSong extends AsyncTask {
        File song;
        public AddSong(File song) {
            this.song = song;
        }
        @Override
        protected Object doInBackground(Object[] objects){
            ArrayList<Object> params = new ArrayList<>();
            params.add(UUIDConnectedRoom);
            params.add(song);
            Call<Integer> call = commands.command(params);
            try{
                Response<Integer> response = call.execute();
                if(!(response.body() == 0))notMaked.sendEmptyMessage(1);
            } catch (IOException e) {
                e.printStackTrace();
                h.sendEmptyMessage(1);
            } catch (NullPointerException e){h.sendEmptyMessage(1);}
            return null;
        }
    }
    class ChangePassword extends AsyncTask {
        String newPassword;
        public ChangePassword(String s){newPassword = s;}
        @Override
        protected Object doInBackground(Object[] objects){
            ArrayList<Object> params = new ArrayList<>();
            params.add(UUIDConnectedRoom);
            params.add(newPassword);
            Call<Boolean> call = commands.changePassword(params);
            try{
                Response<Boolean> response = call.execute();
                if(response.body())password = newPassword;
                else notMaked.sendEmptyMessage(1);
            } catch (IOException e) {
                e.printStackTrace();
                h.sendEmptyMessage(1);
            } catch (NullPointerException e){h.sendEmptyMessage(1);}
            return null;
        }
    }
    public void changeColor(int text){color = text;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
    public boolean isRoom() { return isRoom; }
    public void setRoom(boolean room) { isRoom = room; }
    public String getConnectedAddress() { return connectedAddress; }
    public void setConnectedAddress(String connectedAddress) { this.connectedAddress = connectedAddress; }
    public boolean isDelete() { return delete; }
    public void setDelete(boolean delete) { this.delete = delete; }
    public boolean isPlayingToo() {return playingToo;}
    public void setPlayingToo(boolean playingToo) { this.playingToo = playingToo; }
    public String getUUID() {return UUID;}
    public void setUUID(String UUID) { this.UUID = UUID; }
    public void addToThePlaylist(File f){new AddSong(f).execute();}
    public void setService(MediaPlayerService s){service = s;}
    public void setMe(String s){if(name != null && UUID != null)me = new User(name, UUID, s);}
    public void UUIDChanged(){if(text != null) text.setText(getResources().getText(R.string.name) + " " + name + "\nUUID: " + UUID);}
}
