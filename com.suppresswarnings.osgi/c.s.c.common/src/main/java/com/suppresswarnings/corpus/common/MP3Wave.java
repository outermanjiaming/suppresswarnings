/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

public class MP3Wave {

	public static AudioInputStream audioInputStream(String mp3Filepath){
		File mp3 = new File(mp3Filepath);
	    try {
	        MpegAudioFileReader mp = new MpegAudioFileReader();
	        AudioInputStream in = mp.getAudioInputStream(mp3);
	        AudioFormat baseFormat = in.getFormat();
	        AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
	        		baseFormat.getSampleRate(), 
	        		16,
	                baseFormat.getChannels(), 
	                baseFormat.getChannels() * 2, 
	                baseFormat.getSampleRate(), 
	                false);
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(targetFormat, in);
	        return audioInputStream;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	public static boolean mp3wave(String mp3Filepath, String outputFilepath) {
		try {
			AudioSystem.write(audioInputStream(mp3Filepath), AudioFileFormat.Type.WAVE, new File(outputFilepath));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args) {
		Mouth mouth = new Mouth(null);
		mouth.speak(audioInputStream("/Users/lijiaming/company/suppresswarnings/com.suppresswarnings.osgi/c.s.c.service.data/speak.mp3"));

//		boolean x = mp3wave("/Users/lijiaming/company/suppresswarnings/com.suppresswarnings.osgi/c.s.c.service.data/speak.mp3", "/Users/lijiaming/company/speak_wave.pcm");
//		if(x) {
//			ProcessBuilder builder = new ProcessBuilder("open", "/Users/lijiaming/company/speak_wave.pcm");
//			try {
//				Process process = builder.start();
//				int y = process.waitFor();
//				System.out.println(y);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
	}
	
}
