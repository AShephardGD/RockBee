package com.example.rockbee;

public class LookingForProgress extends Thread {
    MusicFragment mf;
    boolean mode = true;
    @Override
    public void run() {
        while(!isInterrupted()){
            try {
                Thread.sleep(25);
                if(mode) mf.resetTime();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalStateException e){}
        }
    }
    public void setMusicFragment(MusicFragment fragment) {mf = fragment;}
    public void changeMode(){
        if(mode) mode = false;
        else mode = true;
    }
}
