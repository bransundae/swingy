package com.swingy.audio;

import javax.sound.sampled.*;
import java.io.File;

public class AudioFile implements LineListener{
    private File soundFile;
    private AudioInputStream ais;
    private AudioFormat format;
    private DataLine.Info info;
    private Clip clip;
    private FloatControl gainControl;
    private volatile boolean playing;

    public void setVolume(int volumeMod) {
        this.gainControl.setValue(volumeMod);
    }

    public AudioFile(String fileName){
        soundFile = new File(fileName);
        try {
            ais = AudioSystem.getAudioInputStream(soundFile);
            format = ais.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip)AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(ais);
            gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void play(){
        gainControl.setValue(-20);
        clip.start();
        playing = true;
    }

    public void play(float gainMod){
        gainControl.setValue(gainMod);
        clip.start();
        playing = true;
    }

    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.START)
            playing = true;
        else if (event.getType() == LineEvent.Type.STOP) {
            clip.stop();
            clip.flush();
            clip.setFramePosition(0);
            playing = false;
        }
    }
}
