package com.example.rockbee;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

import java.util.ArrayList;

public class ServerMusicFragment extends Fragment {
    private int color;
    private String name = null, connectedAddress = null, UUID = "228322";
    private boolean isConnected = false, isRoom = false, delete = false, playingToo = false, isName = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(name != null) isName = true;
        View view = null;
        if (!isRoom && !isConnected && isName){
            view = inflater.inflate(R.layout.servermusicfragment_noroom, container, false);
            TextView text = view.findViewById(R.id.nameAndUUID);
            text.setText(getResources().getText(R.string.name) + name + "\nUUID: " + UUID);
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
                            view1 = getLayoutInflater().inflate(R.layout.playlists_alert_dialog, null);
                            text1 = view1.findViewById(R.id.newPlaylistName);
                            text1.setHint(R.string.enterData);
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.name)
                                    .setPositiveButton(getResources().getText(R.string.ready), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(getActivity(), "Никого не найдено, потому что еще не работает))))", Toast.LENGTH_SHORT).show();
                                            isConnected = true;
                                            ((MainActivity)getActivity()).refresh();
                                        }
                                    })
                                    .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setView(view1)
                                    .setMessage(R.string.findByNameOrUUID)
                                    .create()
                                    .show();
                            Toast.makeText(getActivity(), "Пока так не умею", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.createMyRoom:
                            isRoom = true;
                            ((MainActivity)getActivity()).refresh();
                            break;
                        case R.id.changeMyName:
                            view1 = getLayoutInflater().inflate(R.layout.playlists_alert_dialog, null);
                            text1 = view1.findViewById(R.id.newPlaylistName);
                            text1.setHint(R.string.enterName);
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.name)
                                    .setPositiveButton(getResources().getText(R.string.ready), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String s = text1.getText().toString();
                                            if(s.equals("")) {
                                                Toast.makeText(getActivity(), getResources().getText(R.string.noName), Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                setName(s);
                                                text.setText(getResources().getText(R.string.name) + name + "\nUUID: Да я хз вообще пока что сюда писать, но должен быстро разобраться, чтобы быстрее сесть за егэ");
                                            }
                                        }
                                    })
                                    .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .setMessage(R.string.whyNeedName)
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
        } else if(isRoom && isName){
            view = inflater.inflate(R.layout.servermusicfragment_myroom, container, false);
            TextView myPass = view.findViewById(R.id.myPass);
            ListView myTracks = view.findViewById(R.id.myTracks);
            Switch myDelete = view.findViewById(R.id.myDelete);
            Button closeTheRoom = view.findViewById(R.id.closeTheRoom), myUserList = view.findViewById(R.id.myUserList), myPassButton = view.findViewById(R.id.myPassButton);
            myDelete.setChecked(delete);
            myPass.setText(getResources().getText(R.string.against) + "0/1");
            ArrayList<String> Users = new ArrayList<>();
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
                        case R.id.myDelete:
                            delete = myDelete.isChecked();
                            break;
                        case R.id.myPass:
                            myPass.setText(getResources().getText(R.string.against) + "1/1");
                            break;
                        case R.id.myUserList:
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.numbersOfUsers);
                    }
                }
            };
        } else if (isName){ //Connected to another
            view = inflater.inflate(R.layout.servermusicfragment_friendroom, container, false);

        }
        return view;
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
}
