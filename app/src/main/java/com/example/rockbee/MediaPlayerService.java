package com.example.rockbee;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service {
    MediaPlayer mediaPlayer = new MediaPlayer();
    MyBinder binder = new MyBinder();
    ArrayList<File> playlist = new ArrayList<>();
    boolean isRandom = false;
    int isLooping = 0;
    File nowPlaying = null;
    MusicFragment mf;
    public MediaPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.e("Service created", "hi");
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        super.onDestroy();
        Log.e("Service ended", "BYE");
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    class MyBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
    public void playMusic(final File f, ArrayList<File> sentPlaylist){
        playlist = new ArrayList<>(sentPlaylist);
        try {
            Toast.makeText(this, isLooping + "  " + isRandom, Toast.LENGTH_SHORT).show();
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(f.getAbsolutePath());
            mediaPlayer.prepareAsync();
            nowPlaying = f;
            mf.setIsPlaying(f);
            mf.setName(f);
            mf.setPlaylist(sentPlaylist);
            if(mf.getPS() != null) mf.getPS().setImageResource(R.drawable.ic_media_pause);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    mf.setMax();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.e("music ended", f.getAbsolutePath());
                            if (isRandom && (isLooping != 2)) playMusic(playlist.get((int) Math.round(Math.random() * (playlist.size() - 1))), playlist);
                            else if ((isLooping == 1 || playlist.indexOf(f) + 1 != playlist.size()) && isLooping != 2)
                                playMusic(playlist.get((playlist.indexOf(f) + 1) % playlist.size()), playlist);
                            else if(isLooping == 2) playMusic(f, playlist);
                        }
                    });
                }
            });
        } catch (IOException e) {
            Log.e("MediaPlayerService", "playMusic - mediaPlayer:" + e.toString());
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.bee)
                .setContentTitle(f.getName());
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }
    public void pause(){mediaPlayer.pause(); }
    public void setRandom(boolean r) {isRandom = r;}
    public void setIsLooping(int i){isLooping = i;}
    public boolean isPlaying(){return mediaPlayer.isPlaying();}
    public void play(){mediaPlayer.start();}
    public int getDuration(){return mediaPlayer.getDuration();}
    public int getCurrentPosition(){return mediaPlayer.getCurrentPosition();}
    public void seekTo(int i){mediaPlayer.seekTo(i);}

    @Override
    public boolean onUnbind(Intent intent) {
        if(!mediaPlayer.isPlaying()){
            stopSelf();
        }
        return super.onUnbind(intent);
    }
    public File getNowPlaying(){return nowPlaying;}
    public void setFragments(MusicFragment f){mf = f;}
}
