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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MediaPlayerService extends Service {
    private final int NOTIFICATION_ID = 1;
    private final String NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<File> playlist = new ArrayList<>();
    private boolean isRandom = false;
    private int isLooping = 0;
    private File nowPlaying = null;
    private MusicFragment mf;
    private boolean audioFocus = false;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
    MediaSessionCompat mediaSession;
    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        int currentState = PlaybackStateCompat.STATE_STOPPED;
        @Override
        public void onPlay() {
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
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (isRandom && (isLooping != 2)) playMusic(playlist.get((int) Math.round(Math.random() * (playlist.size() - 1))), playlist);
                    else if ((isLooping == 1 || playlist.indexOf(nowPlaying) + 1 != playlist.size()) && isLooping != 2) playMusic(playlist.get((playlist.indexOf(nowPlaying) + 1) % playlist.size()), playlist);
                    else if (isLooping == 2) playMusic(nowPlaying, playlist);
                    else mf.setPlay();
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
            audioFocus = false;
            mediaPlayer.pause();
            unregisterReceiver(becomingNoisyReceiver);
            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_PAUSED;
            refresh(currentState);
            mf.setPlay();
        }

        @Override
        public void onStop() {
            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            if (isRandom) nowPlaying = playlist.get((int) Math.round(Math.random() * (playlist.size() - 1)));
            else nowPlaying = playlist.get((playlist.indexOf(nowPlaying) + 1) % playlist.size());
            MediaPlayerService.this.playMusic(nowPlaying, playlist);
        }

        @Override
        public void onSkipToPrevious() {
            int index;
            if (isRandom) nowPlaying = playlist.get((int) Math.round(Math.random() * (playlist.size() - 1)));
            else {
                index = (playlist.indexOf(nowPlaying) - 1);
                if (index < 0) index = playlist.size() - 1;
                nowPlaying = playlist.get(index);
            }
            MediaPlayerService.this.playMusic(nowPlaying, playlist);
        }
        private void updateMetadataFromTrack() {
            String name = nowPlaying.getName();
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.drawable.bee));
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, name.substring(0, name.lastIndexOf(".")));
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
                    if(!mediaPlayer.isPlaying()) mediaSessionCallback.onPlay();
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
    public MediaPlayerService() {}
    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID, "Player controls", NotificationManagerCompat.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaSession.release();
        super.onDestroy();
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
    public void setRandom(boolean r) {isRandom = r;}
    public void setIsLooping(int i){isLooping = i;}
    public boolean isPlaying(){return mediaPlayer.isPlaying();}
    public void play(){
        MediaControllerCompat controller;
        try {
            controller = new MediaControllerCompat(this, mediaSession.getSessionToken());
            controller.getTransportControls().play();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public int getDuration(){return mediaPlayer.getDuration();}
    public int getCurrentPosition(){return mediaPlayer.getCurrentPosition();}
    public void seekTo(int i){mediaPlayer.seekTo(i);}
    public File getNowPlaying(){return nowPlaying;}
    public void setFragments(MusicFragment f){mf = f;}
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
        String name = nowPlaying.getName();
        MediaControllerCompat controller = mediaSession.getController();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(name.substring(0, name.lastIndexOf(".")))
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, getResources().getString(R.string.prev), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        if(playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause,
                            getResources().getString(R.string.pause),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        else builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play,
                            getResources().getString(R.string.play),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, getResources().getString(R.string.next), MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                        .setMediaSession(mediaSession.getSessionToken()))
                .setSmallIcon(R.drawable.bee)
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
}
