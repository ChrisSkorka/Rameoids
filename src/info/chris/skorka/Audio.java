package info.chris.skorka;

import java.io.*;
import java.net.URL;

import javax.sound.sampled.*;

public class Audio {

    Clip clip;

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

    public void play(){
        if(clip != null){
            clip.setFramePosition(0);
            clip.start();
        }
    }

}
