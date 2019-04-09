package com.suppresswarnings.corpus.common;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Speaker {
	public static void speakMp3(String mp3Path) {
		AudioInputStream audioInputStream = MP3Wave.audioInputStream(mp3Path);
    	SourceDataLine line = null;
		try {
	 		AudioFormat targetFormat = audioInputStream.getFormat();
	 		DataLine.Info dinfo = new DataLine.Info(SourceDataLine.class, targetFormat);
	        line = (SourceDataLine) AudioSystem.getLine(dinfo);
	        line.open(targetFormat);
	        line.start();

	        int bytesRead = 0, length = 1024;
	        byte[] buffer = new byte[length];
	        while ((bytesRead = audioInputStream.read(buffer, 0, length)) != -1) {
	            line.write(buffer, 0, bytesRead);
	        }
	        line.flush();
	        line.stop();
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        System.out.println("audio problem " + ex);
	    } finally {
	        line.close();
	        System.out.println("line closed");
		}
	}

}
