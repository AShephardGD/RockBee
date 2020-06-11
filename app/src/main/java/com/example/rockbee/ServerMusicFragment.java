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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.os.Environment.getExternalStorageDirectory;

public class ServerMusicFragment extends Fragment {
    private int color;
    private String name = null, UUID = null, password = null, UUIDConnectedRoom, nameForUser = "";
    private boolean isConnected = false, isRoom = false, delete = false, playingToo = false, isName = false, isTracks = true, canDownload = true;
    private ArrayList<String> playlist = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private MediaPlayerService service;
    private User me;
    private WebSocket ws;
    private Integer pass = 0, all = 0;
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.1.65:8080")
            .addConverterFactory(new NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .setLenient()
                    .create()))
            .build();
    private CommandToTheServer commands = retrofit.create(CommandToTheServer.class);
    private Handler refresh = new Handler(){
        public void handleMessage(android.os.Message msg){
            ((MainActivity) getActivity()).refresh(4);
        }}, notMaked = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch(msg.what) {
                case 0:
                    Toast.makeText(getActivity(), R.string.somethingCreateError, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    try{
                        Toast.makeText(getActivity(), R.string.wrongRoom, Toast.LENGTH_SHORT).show();
                        refresh.sendEmptyMessage(1);
                    } catch (NullPointerException e){}
                    isRoom = false;
                    isConnected = false;
                    UUIDConnectedRoom = null;
                    password = null;
                    delete = false;
                    playingToo = false;
                    isTracks = true;
                    playlist = new ArrayList<>();
                    users = new ArrayList<>();
                    pass = 0;
                    all = 0;
                    break;
                case 2:
                    Toast.makeText(getActivity(), R.string.wrongPassword, Toast.LENGTH_SHORT).show();
                    break;
            }
        }}, refreshMe = new Handler(){
        public void handleMessage(android.os.Message msg){
            ArrayList<String> list = new ArrayList<>();
            for (User u: users) list.add(u.toString());
            myTracks.setAdapter(new UserAdapter(getActivity(), list, color));
        }}, refreshFriend = new Handler(){
        public void handleMessage(android.os.Message msg){
            ArrayList<String> list = new ArrayList<>();
            for (User u: users) list.add(u.toString());
            friendsTracks.setAdapter(new UserAdapter(getActivity(), list, color));
        }}, refreshFriendsTracks = new Handler(){
        public void handleMessage(android.os.Message msg){
            friendsTracks.setAdapter(new ServerPlaylistAdapter(getActivity(), playlist,  color));
        }}, refreshMyTracks = new Handler(){
        public void handleMessage(android.os.Message msg){
            myTracks.setAdapter(new ServerPlaylistAdapter(getActivity(), playlist,  color));
        }};
    private TextView text;
    private Gson gson = new Gson();
    private ListView myTracks, friendsTracks;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(name != null) isName = true;
        View view = null;
        if(isConnected || isRoom)new isRoomAlive().execute();
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
                                            MessageToWebSocket message = new MessageToWebSocket();
                                            ConnectToTheRoomData data = new ConnectToTheRoomData();
                                            data.setNameNewUser(me.getName());
                                            data.setUUIDNewUser(me.getUUID());
                                            data.setPassword(password);
                                            message.setData(gson.toJson(data));
                                            message.setUUID(UUIDConnectedRoom);
                                            message.setCommand("connect");
                                            ws.send(gson.toJson(message));
                                            ((MainActivity)getActivity()).refresh(4);
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
                                            new CreateNewRoom().execute();
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
            myTracks = view.findViewById(R.id.myTracks);
            Switch myDelete = view.findViewById(R.id.myDelete);
            myDelete.setChecked(delete);
            Button closeTheRoom = view.findViewById(R.id.closeTheRoom),
                    myUserList = view.findViewById(R.id.myUserList),
                    myPassButton = view.findViewById(R.id.myPassButton),
                    myInfo = view.findViewById(R.id.myInfo),
                    setPass = view.findViewById(R.id.setPass);
            if(isTracks){
                myUserList.setText(R.string.listUsers);
                myTracks.setAdapter(new ServerPlaylistAdapter(getActivity(), playlist,  color));
            } else {
                myUserList.setText(R.string.tracks);
                ArrayList<String> list = new ArrayList<>();
                for (User u: users) list.add(u.toString());
                myTracks.setAdapter(new UserAdapter(getActivity(), list, color));
            }
            myDelete.setChecked(delete);
            myDelete.setTextColor(color);
            if(!users.contains(me))users.add(me);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
                        case R.id.myDelete:
                            delete = myDelete.isChecked();
                            break;
                        case R.id.myPassButton:
                            if(!service.isPassed()) {
                                MessageToWebSocket message = new MessageToWebSocket();
                                message.setCommand("pass");
                                message.setUUID(UUIDConnectedRoom);
                                message.setData("");
                                ws.send(gson.toJson(message));
                                service.setPassed(true);
                            }
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
                                myTracks.setAdapter(new ServerPlaylistAdapter(getActivity(), playlist,  color));
                            }
                            break;
                        case R.id.closeTheRoom:
                            service.disconnectToTheServer(UUIDConnectedRoom, true);
                            MessageToWebSocket message1 = new MessageToWebSocket();
                            message1.setCommand("close");
                            message1.setUUID(UUIDConnectedRoom);
                            message1.setData("");
                            ws.send(gson.toJson(message1));
                            isRoom = false;
                            UUIDConnectedRoom = null;
                            password = null;
                            delete = false;
                            playingToo = false;
                            isTracks = true;
                            playlist = new ArrayList<>();
                            users = new ArrayList<>();
                            pass = 0;
                            all = 0;
                            refresh.sendEmptyMessage(1);
                            service.setRoom(false);
                            service.userDisconnected();
                            service.deleteSongs();
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
                                            MessageToWebSocket message2 = new MessageToWebSocket();
                                            message2.setCommand("password");
                                            message2.setUUID(UUIDConnectedRoom);
                                            message2.setData(password);
                                            ws.send(gson.toJson(message2));
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
                            all = users.size();
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
            friendsTracks = view.findViewById(R.id.friendTracks);
            friendsTracks.setAdapter(new ServerPlaylistAdapter(getActivity(), playlist,  color));
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
            if(!users.contains(me))users.add(me);
            Switch friendDelete = view.findViewById(R.id.friendDelete);
            friendDelete.setChecked(delete);
            friendDelete.setTextColor(color);
            if(!isTracks){
                friendUserList.setText(R.string.tracks);
                ArrayList<String> list = new ArrayList<>();
                for(User u: users) list.add(u.toString());
                friendsTracks.setAdapter(new UserAdapter(getActivity(), list, color));
            } else {
                friendUserList.setText(R.string.listUsers);
                friendsTracks.setAdapter(new ServerPlaylistAdapter(getActivity(), playlist,  color));
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
                        case R.id.playButton:
                            if(playingToo){
                                playButton.setText(R.string.playToo);
                                playingToo = false;
                                service.disconnectToTheServer(UUIDConnectedRoom, true);
                            } else {
                                playButton.setText(R.string.stopPlaying);
                                playingToo = true;
                                service.connectToTheServer(UUID, UUIDConnectedRoom);
                            }
                            break;
                        case R.id.friendPassButton:
                            if(!service.isPassed()) {
                                MessageToWebSocket message = new MessageToWebSocket();
                                message.setCommand("pass");
                                message.setUUID(UUIDConnectedRoom);
                                message.setData("");
                                ws.send(gson.toJson(message));
                                service.setPassed(true);
                            }
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
                                friendsTracks.setAdapter(new ServerPlaylistAdapter(getActivity(), playlist,  color));
                            }
                            break;
                        case R.id.leave:
                            MessageToWebSocket message1 = new MessageToWebSocket();
                            message1.setCommand("disconnect");
                            message1.setUUID(UUIDConnectedRoom);
                            ConnectData disconnect = new ConnectData();
                            disconnect.setNameNewUser(me.getName());
                            disconnect.setUUIDNewUser(me.getUUID());
                            message1.setData(gson.toJson(disconnect));
                            ws.send(gson.toJson(message1));
                            isConnected = false;
                            UUIDConnectedRoom = null;
                            password = null;
                            delete = false;
                            playingToo = false;
                            isTracks = true;
                            playlist = new ArrayList<>();
                            users = new ArrayList<>();
                            pass = 0;
                            all = 0;
                            refresh.sendEmptyMessage(1);
                            service.setConnected(false);
                            service.userDisconnected();
                            service.disconnectToTheServer(UUIDConnectedRoom, true);
                            service.deleteSongs();
                            break;
                        case R.id.friendDelete:
                            delete = friendDelete.isChecked();
                            break;
                        case R.id.friendInfo:
                            all = users.size();
                            String p;
                            if(password.equals(""))p = getResources().getString(R.string.noPassword);
                            else p = " \""+ password + "\"";
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.info)
                                    .setMessage(getResources().getString(R.string.friendUUID) + UUIDConnectedRoom + "\n" +
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
            playButton.setOnClickListener(listener);
            friendPassButton.setOnClickListener(listener);
            friendUserList.setOnClickListener(listener);
            leave.setOnClickListener(listener);
            friendDelete.setOnClickListener(listener);
            friendInfo.setOnClickListener(listener);
        }
        return view;
    }
    class CreateNewRoom extends AsyncTask{ //Create new room
        @Override
        protected Object doInBackground(Object[] objects){
            ArrayList<Object> params = new ArrayList<>();
                params.add(me.getName());
                params.add(me.getUUID());
                params.add(password);
                Call<Boolean> call = commands.createNewRoom(params);
                try{
                    Response<Boolean> response = call.execute();
                    if(response.body()){
                        UUIDConnectedRoom = UUID;
                        isRoom = true;
                        refresh.sendEmptyMessage(1);
                        all++;
                        MessageToWebSocket message = new MessageToWebSocket();
                        message.setCommand("creatorsession");
                        message.setUUID(UUIDConnectedRoom);
                        message.setData("");
                        ws.send(gson.toJson(message));
                        service.connectToTheServer(UUID, UUIDConnectedRoom);
                        service.setRoom(true);
                        service.connected();
                    } else notMaked.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    notMaked.sendEmptyMessage(0);
                } catch (NullPointerException e){notMaked.sendEmptyMessage(0);}

            return null;
        }
    }
    public void gotMessageFromWebSocket(String message){
        Gson gson = new Gson();
        MessageFromWebSocket msg = gson.fromJson(message, MessageFromWebSocket.class);

        switch(msg.getCommand()){
            case "connect":
                ConnectData data = gson.fromJson(msg.getData(), ConnectData.class);
                users.add(new User(data.getNameNewUser(), data.getUUIDNewUser(), nameForUser));
                if(!isTracks && isConnected){
                    refreshFriend.sendEmptyMessage(1);
                } else if(!isTracks && isRoom){
                    refreshMe.sendEmptyMessage(1);
                }
                break;
            case "disconnect":
                ConnectData data1 = gson.fromJson(msg.getData(), ConnectData.class);
                users.remove(new User(data1.getNameNewUser(), data1.getUUIDNewUser(), nameForUser));
                if(!isTracks && isConnected){
                    refreshFriend.sendEmptyMessage(1);
                } else if(!isTracks && isRoom){
                    refreshMe.sendEmptyMessage(1);
                }
                break;
            case "password":
                password = msg.getData();
                break;
            case "close":
                isConnected = false;
                UUIDConnectedRoom = null;
                password = null;
                delete = false;
                playingToo = false;
                isTracks = true;
                playlist = new ArrayList<>();
                users = new ArrayList<>();
                pass = 0;
                all = 0;
                refresh.sendEmptyMessage(1);
                service.userDisconnected();
                service.disconnectToTheServer(UUIDConnectedRoom,false);
                service.deleteSongs();
                break;
            case "pass":
                pass = Integer.parseInt(msg.getData());
                break;
            case "error":
                notMaked.sendEmptyMessage(Integer.parseInt(msg.getData()));
                break;
            case "successconnection":
                isConnected = true;
                service.setConnected(true);
                service.connected();
                break;
            case "userslist":
                User u = gson.fromJson(msg.getData(), User.class);
                u.setNameString(nameForUser);
                users.add(u);
                break;
            case "songended":
                if(msg.getData().equals("start"))playlist = new ArrayList<>();
                else if(msg.getData().equals("end") && isTracks){
                    service.setPassed(false);
                    if(myTracks != null && isRoom)refreshMyTracks.sendEmptyMessage(0);
                    if(friendsTracks != null && isConnected) refreshFriendsTracks.sendEmptyMessage(0);
                } else if(!msg.getData().equals("end") && !msg.getData().equals("start"))playlist.add(msg.getData());
                break;
            case "startedplaying":
                service.setName(msg.getData());
                break;
            case "addtotheplaylist":
                playlist.add(msg.getData());
                if(myTracks != null && isRoom)refreshMyTracks.sendEmptyMessage(0);
                if(friendsTracks != null && isConnected) refreshFriendsTracks.sendEmptyMessage(0);
                break;
            case "deletefromtheplaylist":
                playlist.remove(msg.getData());
                if(myTracks != null && isRoom)refreshMyTracks.sendEmptyMessage(0);
                if(friendsTracks != null && isConnected) refreshFriendsTracks.sendEmptyMessage(0);
                service.setPassed(false);
                break;
            case "allmusicended":
                playlist = new ArrayList<>();
                if(myTracks != null && isRoom)refreshMyTracks.sendEmptyMessage(0);
                if(friendsTracks != null && isConnected) refreshFriendsTracks.sendEmptyMessage(0);
                service.setPassed(false);
                break;
        }
    }
    public void setMe(String s){
        if(name != null && UUID != null)me = new User(name, UUID, s);
        nameForUser = s;
    }
    class SendAudioToServer extends AsyncTask{
        File song;
        Handler h = new Handler(){
            public void handleMessage(android.os.Message msg){
                service.addToThePlaylistFromRoom(song);
            }};
        public SendAudioToServer(File f) {
            song = f;
        }

        @Override
        protected Object doInBackground(Object[] objects){
            canDownload = false;
            try {
                byte[] bytes = Files.readAllBytes(song.toPath());
                String toSend = new String(bytes, StandardCharsets.ISO_8859_1);
                int len = toSend.length()/256;
                if(toSend.length() % 256 != 0) len++;
                ArrayList<Object> params = new ArrayList<>();
                params.add(UUIDConnectedRoom);
                params.add(song.length());
                params.add(song.getName());
                Call<Boolean> checkingSong = commands.checkingSongInTheRoom(params);//Проверка: Есть ли песня на сервере
                Response<Boolean> response = checkingSong.execute();
                if(response.body() == null){
                    notMaked.sendEmptyMessage(0);
                    return null;
                }
                else if(!response.body()) {//Песни нет на сервере
                    AudioData data = new AudioData();
                    data.setName(song.getName());
                    data.setLenAudio(Integer.toString(len));
                    data.setBy(UUID);
                    MessageToWebSocket message = new MessageToWebSocket();
                    message.setCommand("audio");
                    message.setUUID(UUIDConnectedRoom);
                    for (int i = 0; i < len; i++) {
                        if (((i + 1) * 256) < toSend.length())
                            data.setAudio(toSend.substring(i * 256, (i + 1) * 256));
                        else data.setAudio(toSend.substring(i * 256));
                        data.setPart(Integer.toString(i));
                        message.setData(new Gson().toJson(data));
                        ws.send(new Gson().toJson(message));
                    }
                    service.addSongToServer(song);
                } //Песня докачалась
                else if(isConnected){
                    MessageToWebSocket message = new MessageToWebSocket();
                    message.setCommand("addfromnotcreator");
                    message.setUUID(UUIDConnectedRoom);
                    message.setData(song.getName());
                    ws.send(gson.toJson(message));
                }
                if(isRoom)h.sendEmptyMessage(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            canDownload = true;
            return null;
        }
    }
    class isRoomAlive extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects){
            ArrayList<Object> params = new ArrayList<>();
            params.add(UUIDConnectedRoom);
            Call<Boolean> call = commands.checkingRoom(params);
            Log.i("IsRoomAlive", "checking");
            try {
                Response<Boolean> response = call.execute();
                if(!response.body()){
                    notMaked.sendEmptyMessage(1);
                    Log.i("IsRoomAlive", "thereisnoroom");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){}
            return null;
        }
    }
    public void addToThePlaylist(File f){
        if(canDownload) {
            new SendAudioToServer(f).execute();
            Toast.makeText(getActivity(), R.string.downloading, Toast.LENGTH_SHORT).show();
        } else Toast.makeText(getActivity(), R.string.cantdownload, Toast.LENGTH_SHORT).show();
    }
    public void setService(MediaPlayerService s){service = s;}
    public void setWebSocket(WebSocket webSocket){ws = webSocket;}
    public void changeColor(int text){color = text;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
    public boolean isRoom() { return isRoom; }
    public void setRoom(boolean room) { isRoom = room; }
    public String getConnectedAddress() { return UUIDConnectedRoom; }
    public void setConnectedAddress(String connectedAddress) { UUIDConnectedRoom = connectedAddress; }
    public boolean isDelete() { return delete; }
    public void setDelete(boolean delete) { this.delete = delete; }
    public boolean isPlayingToo() {return playingToo;}
    public void setPlayingToo(boolean playingToo) { this.playingToo = playingToo; }
    public String getUUID() {return UUID;}
    public void setUUID(String UUID) { this.UUID = UUID; }
    public void UUIDChanged(){if(text != null) text.setText(getResources().getText(R.string.name) + " " + name + "\nUUID: " + UUID);}
}
