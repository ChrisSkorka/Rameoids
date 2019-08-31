package info.chris.skorka;

import java.io.*;
import java.net.URL;

import javax.sound.sampled.*;

public class Audio {

    Clip clip;

    /**
     * Creates a Audio object with a audio file loaded and ready to play
     * @param fileName File name with in resources
     */
    public Audio(String fileName){

        URL url = getClass().getResource(fileName);

        try{
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

        }catch(UnsupportedAudioFileException e){
            System.out.println("UnsupportedAudioFileException");
        }catch (IOException e){
            System.out.println("IOException");
        }catch (LineUnavailableException e){
            System.out.println("LineUnavailableException");
        }
    }

    /**
     * Plays the sound. If it is already playing it stops, resets and plays it
     */
    public void play(){
        if(clip != null){
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

}
