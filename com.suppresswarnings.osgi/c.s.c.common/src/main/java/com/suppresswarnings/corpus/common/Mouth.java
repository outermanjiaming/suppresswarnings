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
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Mouth {
	Brain brain;
	Baidu baidu;
	public Mouth(Brain brain) {
		this.brain = brain;
		this.baidu = new Baidu();
	}

	public void speak(String words) {
		String mp3 = this.baidu.speak(words);
		speak(MP3Wave.audioInputStream(mp3));
	}
	public void speak(Path waveFilePath){
	    
	    AudioInputStream audioInputStream = null;
	    
	    try {
			audioInputStream = AudioSystem.getAudioInputStream(waveFilePath.toFile());
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
		if(bytes == null) {
			System.err.println("data[] is null");
			return;
		}
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
		if(audioInputStream == null) return;
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

	public void muttering() {
		boolean flag = true;
		do{
			try {
				final ReentrantLock speakLock = brain.lock;
				speakLock.lock();
		        try {
		        	if(brain.memory.size() == 0) {
			        	System.out.println("Mouth brain.notListen.await();");
			        	brain.notListen.await();
			        	System.out.println("Mouth brain.notListen");
		        	}
					byte[] listen = brain.memory.take();
					String words = baidu.listen(listen);
					if(words.length() > 1) {
						speak(listen);
						
						System.err.println("####### Heard #######"+ words);
						if(words.contains("拍照") || words.contains("是谁") || words.contains("茄子") ){
							brain.look();
						}
						if(words.contains("学习")) {
							brain.learn();
						}
					}
					System.out.println(System.currentTimeMillis() + "Mouth brain.notSpeak.signalAll();");
		        	Thread.sleep(500);
		        	brain.notSpeak.signal();
		        	System.out.println(System.currentTimeMillis() + "Mouth brain.notSpeak");
		        } finally {
		        	speakLock.unlock();
		        	System.out.println("Mouth unlock");
		        }
			} catch (InterruptedException e) {
				e.printStackTrace();
				flag = false;
			}
		} while(flag);
	}
}
