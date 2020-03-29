package com.example.rockbee;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

public class LookingForProgress extends Thread {
    MusicFragment mf;
    boolean mode = true;
    MediaPlayer mediaPlayer;
    private File file = new File(getExternalStorageDirectory().getAbsolutePath()+"/Temporary Music From RockBee", "a"),
            playPath = new File(getExternalStorageDirectory().getAbsolutePath()+"/Temporary Music From RockBee", "b"),
            playTime = new File(getExternalStorageDirectory().getAbsolutePath()+"/Temporary Music From RockBee", "c"),
            playing;
    @Override
    public void run() {
        while(getRun()){
            try {
                Thread.sleep(10);
                if(mode) mf.resetTime();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalStateException e){}

        }
        try(FileWriter writer = new FileWriter(playPath, false))
        {
            if(playing != null && mediaPlayer.isPlaying())writer.write("" + playing.getAbsolutePath());
            else writer.write("");
            writer.flush();
        } catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
        boolean b = mediaPlayer.isPlaying();
        mediaPlayer.pause();
        try(FileWriter writer = new FileWriter(playTime, false))
        {
            if(playing != null && b)writer.write("" + mediaPlayer.getCurrentPosition());
            else writer.write("");
            writer.flush();
        } catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
        mediaPlayer.release();
        this.interrupt();
    }
    public void setMusicFragment(MusicFragment fragment) {mf = fragment;}
    public void changeMode(){
        if(mode) mode = false;
        else mode = true;
    }
    public boolean getRun(){
        boolean bool = true;
        try(FileReader reader = new FileReader(file))
        {
            String str = "";
            int c;
            while((c=reader.read())!=-1){
                str += (char) c;
            }
            if(str.equals("true") || str.equals("false")) bool = Boolean.parseBoolean(str);
        }
        catch(IOException ex){
            Log.e("error IOException: ", ex.toString());
        }
        return bool;
    }
    public void setMediaPlayer(MediaPlayer mp){mediaPlayer = mp; }
    public void setPlaying(File file){playing = file;}
}
