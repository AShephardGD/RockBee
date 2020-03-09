package com.example.rockbee;

public class LookingForProgress extends Thread {
    MusicFragment mf;
    boolean mode = true;
    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(50);
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
