package com.example.rockbee;


import android.os.Handler;

public class LookingForProgress extends Thread {
    MusicFragment mf;
    boolean mode = true;
    Handler h;
    @Override
    public void run() {
        while(!isInterrupted()){
            try {
                Thread.sleep(25);
                if(mode) h.sendEmptyMessage(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalStateException e){}
        }
    }
    public void changeMode(){
        if(mode) mode = false;
        else mode = true;
    }
    public void setH(Handler h) {
        this.h = h;
    }
    public void setMusicFragment(MusicFragment fragment) {mf = fragment;}
}
