package com.example.rockbee;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.os.Environment.getExternalStorageDirectory;


public class MediaPlayerService extends Service {
    private final int NOTIFICATION_ID = 1;
    private final String NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<File> playlist = new ArrayList<>(), usedSongsWithServer = new ArrayList<>();
    private boolean isRandom = false, wasPlaying = true, isConnected = false, isRoom = false, isPassed;
    private int isLooping = 0;
    private File nowPlaying = null;
    private MusicFragment mf;
    private boolean audioFocus = false;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private String UUIDConnectedRoom = null, nameOfSong = null;
    private OkHttpClient client;
    private WebSocket ws;
    private Gson gson = new Gson();
    private ArrayList<Audio> music = new ArrayList<>();
    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
    MediaSessionCompat mediaSession;
    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    private ServerMusicFragment smf;
    private int currentState = PlaybackStateCompat.STATE_STOPPED;
    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            wasPlaying = true;
            startService(new Intent(getApplicationContext(), MediaPlayerService.class));
            updateMetadataFromTrack();
            if(!audioFocus){
                audioFocus = true;
                int audioFocusResult;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
                else audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if(audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return;
            }
            mediaSession.setActive(true);
            registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            mediaPlayer.start();
            if(isRoom){
                MessageToWebSocket message = new MessageToWebSocket();
                message.setCommand("startplaying");
                message.setUUID(UUIDConnectedRoom);
                message.setData(nowPlaying.getName());
                ws.send(gson.toJson(message));
            }
            if(isConnected){
                MessageToWebSocket message = new MessageToWebSocket();
                message.setCommand("checkfortime");
                message.setUUID(UUIDConnectedRoom);
                message.setData("");
                ws.send(gson.toJson(message));
            }
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(!isRoom && !isConnected) {
                        if (isRandom && isLooping != 2)
                            playMusic(playlist.get((int) Math.round(Math.random() * (playlist.size() - 1))), playlist);
                        else if ((isLooping == 1 || playlist.indexOf(nowPlaying) + 1 != playlist.size()) && isLooping != 2)
                            playMusic(playlist.get((playlist.indexOf(nowPlaying) + 1) % playlist.size()), playlist);
                        else if (isLooping == 2) playMusic(nowPlaying, playlist);
                        else mf.setPlay();
                    } else if(isRoom){
                        playlist.remove(0);
                        if(!playlist.isEmpty()){
                            playMusicFromRoom(playlist.get(0));
                        } else{
                            mediaPlayer.release();
                            mediaPlayer = new MediaPlayer();
                            wasPlaying = false;
                            audioFocus = false;
                            nowPlaying = null;
                            nameOfSong = null;
                            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                            currentState = PlaybackStateCompat.STATE_PAUSED;
                            refresh(currentState);
                            mf.setPlay();
                            mf.setIsPlaying(null);
                            MessageToWebSocket message = new MessageToWebSocket();
                            message.setCommand("songended");
                            message.setUUID(UUIDConnectedRoom);
                            message.setData("");
                            ws.send(gson.toJson(message));
                        }
                    }
                }
            });
            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_PLAYING;
            refresh(currentState);
            mf.setPause();
            mf.setMax();
        }

        @Override
        public void onPause() {
            wasPlaying = false;
            audioFocus = false;
            mediaPlayer.pause();
            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_PAUSED;
            refresh(currentState);
            mf.setPlay();
        }

        @Override
        public void onStop() {
            mediaSession.setActive(false);
            unregisterReceiver(becomingNoisyReceiver);
            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            if(!isRoom && !isConnected) {
                if (isRandom && isLooping != 2)
                {
                    nowPlaying = playlist.get((int) Math.round(Math.random() * (playlist.size() - 1)));
                    nameOfSong = nowPlaying.getName();
                }
                else {
                    nowPlaying = playlist.get((playlist.indexOf(nowPlaying) + 1) % playlist.size());
                    nameOfSong = nowPlaying.getName();
                }
                MediaPlayerService.this.playMusic(nowPlaying, playlist);
            } else{
                if(!isPassed) {
                    MessageToWebSocket message = new MessageToWebSocket();
                    message.setCommand("pass");
                    message.setUUID(UUIDConnectedRoom);
                    message.setData("");
                    ws.send(gson.toJson(message));
                    setPassed(true);
                }
            }
        }

        @Override
        public void onSkipToPrevious() {
            int index;
            if (isRandom && isLooping != 2){
                nowPlaying = playlist.get((int) Math.round(Math.random() * (playlist.size() - 1)));
                nameOfSong = nowPlaying.getName();
            }
            else {
                index = (playlist.indexOf(nowPlaying) - 1);
                if (index < 0) index = playlist.size() - 1;
                nowPlaying = playlist.get(index);
                nameOfSong = nowPlaying.getName();
            }
            MediaPlayerService.this.playMusic(nowPlaying, playlist);
        }
        private void updateMetadataFromTrack() {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.drawable.bee));
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, nameOfSong.substring(0, nameOfSong.lastIndexOf(".")));
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "RockBee");
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "RockBee");
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
            mediaSession.setMetadata(metadataBuilder.build());
        }
    };
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch(focusChange){
                case AudioManager.AUDIOFOCUS_GAIN:
                    if(wasPlaying) mediaSessionCallback.onPlay();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mediaPlayer.setVolume(0.3f, 0.3f);
                    break;
                default:
                    mediaSessionCallback.onPause();
            }
        }
    };
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) mediaSessionCallback.onPause();
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID, "Player controls", NotificationManagerCompat.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, "MediaPlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);
        Context applicationContext = getApplicationContext();
        Intent intent = new Intent(applicationContext, MainActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivity(applicationContext, 0, intent,0));
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, applicationContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(applicationContext, 0, mediaButtonIntent, 0));
        Log.e("Service created", "hi");
        client = new OkHttpClient();
        Request request = new Request.Builder().url("ws:192.168.1.65:8080/handler").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        ws = client.newWebSocket(request, listener);
    }
    @Override
    public void onDestroy() {
        stopForeground(true);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaSession.release();
        super.onDestroy();
        client.dispatcher().executorService().shutdown();
        Log.e("Service ended", "BYE");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
    class MyBinder extends Binder {
        public MediaPlayerService getService() {return MediaPlayerService.this; }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }
    public void playMusic(final File f, ArrayList<File> sentPlaylist){
        mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(f.getAbsolutePath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    MediaControllerCompat controller;
                    try {
                        controller = new MediaControllerCompat(MediaPlayerService.this, mediaSession.getSessionToken());
                        controller.getTransportControls().play();
                        playlist = new ArrayList<>(sentPlaylist);
                        nowPlaying = f;
                        nameOfSong = nowPlaying.getName();
                        mf.setIsPlaying(f);
                        mf.setPlaylist(sentPlaylist);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void pause(){
        MediaControllerCompat controller;
        try {
            controller = new MediaControllerCompat(this, mediaSession.getSessionToken());
            controller.getTransportControls().pause();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void play(){
        MediaControllerCompat controller;
        try {
            controller = new MediaControllerCompat(this, mediaSession.getSessionToken());
            controller.getTransportControls().play();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public int getCurrentPosition() {
        try {
            return mediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return 0;
        }
    }
    public void refresh(int playbackState){
        switch(playbackState) {
            case PlaybackStateCompat.STATE_PLAYING:
                startForeground(NOTIFICATION_ID, getNotification(playbackState));
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                NotificationManagerCompat.from(MediaPlayerService.this).notify(NOTIFICATION_ID, getNotification(playbackState));
                stopForeground(false);
                break;
            default:
                stopForeground(true);
        }
    }
    private Notification getNotification(int playbackState){
        if(nowPlaying != null) nameOfSong = nowPlaying.getName();
        else nameOfSong = getResources().getString(R.string.noSong) + ".";
        MediaControllerCompat controller = mediaSession.getController();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(nameOfSong.substring(0, nameOfSong.lastIndexOf(".")))
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if(!isConnected && !isRoom)builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, getResources().getString(R.string.prev), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        if(playbackState == PlaybackStateCompat.STATE_PLAYING && !isRoom)
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause,
                            getResources().getString(R.string.pause),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        else if(!isRoom) builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play,
                            getResources().getString(R.string.play),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, getResources().getString(R.string.next), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
        if(!isRoom && !isConnected)builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                        .setMediaSession(mediaSession.getSessionToken()));
        else if(!isRoom)builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                .setMediaSession(mediaSession.getSessionToken()));
        else builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                    .setMediaSession(mediaSession.getSessionToken()));
        builder.setSmallIcon(R.drawable.bee)
                .setColor(ContextCompat.getColor(this, R.color.black))
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID)
                .setSound(null);
        return builder.build();
    }
    public void next(){
        MediaControllerCompat controller;
        try {
            controller = new MediaControllerCompat(this, mediaSession.getSessionToken());
            controller.getTransportControls().skipToNext();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void prev(){
        MediaControllerCompat controller;
        try {
            controller = new MediaControllerCompat(this, mediaSession.getSessionToken());
            controller.getTransportControls().skipToPrevious();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void connected(){
        mediaSession.setActive(true);
        File[] songs = new File(getExternalStorageDirectory().getAbsolutePath() + "/Temporary Music From RockBee").listFiles();
        usedSongsWithServer.addAll(Arrays.asList(Objects.requireNonNull(songs)));
        MediaControllerCompat controller;
        try {
            controller = new MediaControllerCompat(this, mediaSession.getSessionToken());
            if(nowPlaying != null){
                controller.getTransportControls().pause();
                nowPlaying = null;
                nameOfSong = null;
            }
            playlist = new ArrayList<>();
            refresh(PlaybackStateCompat.STATE_PAUSED);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private class EchoWebSocketListener extends WebSocketListener {
        private Handler refresh = new Handler(){
            public void handleMessage(android.os.Message msg){
                Toast.makeText(MediaPlayerService.this, R.string.somethingCreateError, Toast.LENGTH_SHORT).show();
            }};
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {

            MessageFromWebSocket message = gson.fromJson(text, MessageFromWebSocket.class);
            switch(message.getCommand()){
                case "audio":
                    AudioData data = gson.fromJson(message.getData(), AudioData.class);
                    Audio audio = new Audio();
                    audio.setName(data.getName());
                    audio.setBy(data.getBy());
                    audio.setLen(Integer.parseInt(data.getLenAudio()));
                    int part = Integer.parseInt(data.getPart());
                    if(part != 0){
                        audio = music.get(music.indexOf(audio));
                        audio.addData(data.getAudio());
                        music.add(music.indexOf(audio), audio);
                    } else {
                        audio.addData(data.getAudio());
                        music.add(audio);
                    }
                    if(part + 1 == audio.getLen()){
                        StringBuilder input = new StringBuilder();
                        for(String s: audio.getAudioData()) input.append(s);
                        byte[] bytes = input.toString().getBytes(StandardCharsets.ISO_8859_1);
                        try{
                            File song = new File(getExternalStorageDirectory().getAbsolutePath() + "/Temporary Music From RockBee/" + audio.getName());
                            if(!song.exists()) song.createNewFile();
                            if(song.canWrite()) {
                                FileOutputStream fos = new FileOutputStream(song, false);
                                fos.write(bytes);
                                fos.close();
                                Audio a = new Audio();
                                a.setName(audio.getName());
                                a.setLen(audio.getLen());
                                music.remove(audio);
                                usedSongsWithServer.add(song);
                                if(isRoom)addToThePlaylistFromRoom(song);
                                else if(isConnected &&nameOfSong.equals(song.getName())){playMusicFromRoom(song);}
                            }else  {
                                refresh.sendEmptyMessage(1);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "check":
                    AudioData data1 = gson.fromJson(message.getData(), AudioData.class);
                    MessageToWebSocket toSend = new MessageToWebSocket();
                    toSend.setCommand("checked");
                    toSend.setUUID(UUIDConnectedRoom);
                    File song1 = null;
                    boolean is = false;
                    for(File f: usedSongsWithServer) if(f.getName().equals(data1.getName()) && f.length() == Integer.parseInt(data1.getLenAudio())) {
                        is = true;
                        song1 = f;
                    }
                    if(is && isRoom && song1 != null) addToThePlaylistFromRoom(song1);
                    if(is && isConnected && song1 != null && song1.getName().equals(nameOfSong))playMusicFromRoom(song1);
                    data1.setAudio(Boolean.toString(is));
                    toSend.setData(gson.toJson(data1));
                    webSocket.send(gson.toJson(toSend));
                    break;
                case "createnewfile":
                    File song = new File(getExternalStorageDirectory().getAbsolutePath() + "/Temporary Music From RockBee/" + message.getData());
                    if(!song.exists()) {
                        try {
                            song.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "songendedfromusers":
                    playlist.remove(0);
                    if(!playlist.isEmpty()){
                        nowPlaying = playlist.get(0);
                        MediaPlayerService.this.playMusicFromRoom(nowPlaying);
                    } else{
                        mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                        wasPlaying = false;
                        audioFocus = false;
                        nowPlaying = null;
                        mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                        currentState = PlaybackStateCompat.STATE_PAUSED;
                        refresh(currentState);
                        mf.setPlay();
                        mf.setIsPlaying(null);
                    }
                    break;
                case "startedplaying":
                    nameOfSong = message.getData();
                    File songToPlay = null;
                    for(File f: usedSongsWithServer) {
                        if(f.getName().equals(message.getData())) songToPlay = f;
                    }
                    if(Objects.requireNonNull(songToPlay).exists() && songToPlay.length() != 0) {playMusicFromRoom(songToPlay); }
                    else{
                        AudioData data2 = new AudioData();
                        data2.setName(nameOfSong);
                        data2.setAudio(String.valueOf(false));
                        MessageToWebSocket message1 = new MessageToWebSocket();
                        message1.setCommand("checked");
                        message1.setUUID(UUIDConnectedRoom);
                        message1.setData(gson.toJson(data2));
                        ws.send(gson.toJson(message1));
                    }
                    break;
                case "needtime":
                    MessageToWebSocket message1 = new MessageToWebSocket();
                    message1.setCommand("catchtime");
                    message1.setUUID(UUIDConnectedRoom);
                    message1.setData(String.valueOf(getCurrentPosition()));
                    ws.send(gson.toJson(message1));
                    break;
                case "catchtime":
                    seekTo(Integer.parseInt(message.getData()));
                    break;
                case "allmusicended":
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                    wasPlaying = false;
                    audioFocus = false;
                    nowPlaying = null;
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                    currentState = PlaybackStateCompat.STATE_PAUSED;
                    refresh(currentState);
                    mf.setPlay();
                    mf.setIsPlaying(null);
                    break;
                case "addfromnotcreator":
                    String name = message.getData();
                    for(File f: usedSongsWithServer){
                        if(f.getName().equals(name)){
                            addToThePlaylistFromRoom(f);
                            return;
                        }
                    }
                    break;
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable okhttp3.Response response) {
            refresh.sendEmptyMessage(1);
            Log.e("WebSocketService", t.toString());
        }
    }
    public void connectToTheServer(String UUID, String UUIDConnectedRoom){
        this.UUIDConnectedRoom = UUIDConnectedRoom;
        MessageToWebSocket message = new MessageToWebSocket();
        message.setCommand("connectservice");
        message.setUUID(UUIDConnectedRoom);
        message.setData(UUID);
        ws.send(new Gson().toJson(message));
    }
    public void disconnectToTheServer(String UUID, boolean isClosed){
        if(isClosed) {
            MessageToWebSocket message = new MessageToWebSocket();
            message.setCommand("disconnectservice");
            message.setUUID(UUID);
            message.setData("");
            ws.send(new Gson().toJson(message));
        }
        pause();
    }
    public void addToThePlaylistFromRoom(File f){
        playlist.add(f);
        usedSongsWithServer.add(f);
        if(nowPlaying == null) playMusicFromRoom(f);
        MessageToWebSocket message = new MessageToWebSocket();
        message.setCommand("newplaylist");
        message.setUUID(UUIDConnectedRoom);
        AudioData data = new AudioData();
        data.setName(f.getName());
        data.setAudio("add");
        message.setData(gson.toJson(data));
        ws.send(gson.toJson(message));
    }
    public void playMusicFromRoom(File f){
        mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(f.getAbsolutePath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    MediaControllerCompat controller;
                    try {
                        controller = new MediaControllerCompat(MediaPlayerService.this, mediaSession.getSessionToken());
                        controller.getTransportControls().play();
                        nowPlaying = f;
                        mf.setIsPlaying(f);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void userDisconnected(){
        playlist = new ArrayList<>();
        nowPlaying = null;
        usedSongsWithServer = new ArrayList<>();
    }
    public void setRandom(boolean r) {isRandom = r;}
    public void setIsLooping(int i){isLooping = i;}
    public boolean isPlaying(){return mediaPlayer.isPlaying();}
    public void seekTo(int i){mediaPlayer.seekTo(i);}
    public File getNowPlaying(){return nowPlaying;}
    public void setFragments(MusicFragment f, ServerMusicFragment smf){mf = f;this.smf = smf;}
    public int getDuration(){return mediaPlayer.getDuration();}
    public void setConnected(boolean connected) { isConnected = connected; }
    public void setRoom(boolean room) { isRoom = room; }
    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }
    public void setName(String name){
        nameOfSong = name;
        refresh(currentState);
    }
    public void deleteSongs(){
        if(smf.isDelete() && false){
            File catalog = new File(getExternalStorageDirectory().getAbsolutePath() + "/Temporary Music From RockBee");
            for(File f: Objects.requireNonNull(catalog.listFiles())){
                if(usedSongsWithServer.contains(f))f.delete();
            }
        }
    }
    public void addSongToServer(File f){usedSongsWithServer.add(f);}
}
