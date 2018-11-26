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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Mouth {
	Brain brain;
	
	public Mouth(Brain brain) {
		this.brain = brain;
	}

	public void speak(String waveFilePath){
	    
	    AudioInputStream audioInputStream = null;
	    
	    try {
			audioInputStream = AudioSystem.getAudioInputStream(new File(waveFilePath));
			speak(audioInputStream);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			 try {
				audioInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void speak(byte[] bytes) {
		System.out.println("bytes.length "+ bytes.length);
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));
			speak(audioInputStream);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			 try {
				audioInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void speak(AudioInputStream audioInputStream) {
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
	        
	        
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        System.out.println("audio problem " + ex);
	    } finally {
			line.stop();
	        line.close();
		}
	}

	public void muttering() {
		boolean flag = true;
		do{
			try {
				final ReentrantLock speakLock = brain.speakLock;
				speakLock.lock();
		        try {
		        	System.out.println("Mouth brain.notListen.await();");
		        	brain.notListen.await();
		        	System.out.println("Mouth brain.notListen");
		        } finally {
		        	speakLock.unlock();
		        }
				byte[] listen = brain.memory.take();
				speak(listen);
				final ReentrantLock listenLock = brain.listenLock;
				listenLock.lock();
		        try {
		        	System.out.println(System.currentTimeMillis() + "Mouth brain.notSpeak.signalAll();");
		        	Thread.sleep(200);
		        	brain.notSpeak.signalAll();
		        	System.out.println(System.currentTimeMillis() + "Mouth brain.notSpeak");
		        } finally {
		        	listenLock.unlock();
		        }
			} catch (InterruptedException e) {
				e.printStackTrace();
				flag = false;
			}
		} while(flag);
	}
}
