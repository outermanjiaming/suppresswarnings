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
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class Ear {
	ByteArrayOutputStream cache;
	Brain brain;
	
	public Ear(Brain brain){
		this.brain = brain;
		this.cache = new ByteArrayOutputStream();
	}
	
	public void listen(){
		try {
			AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

			while (true) {
				int weight = 2;
				int downSum = 0;
				boolean flag = true;
				ByteArrayInputStream bais = null;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				AudioInputStream ais = null;
				if(brain.memory.size() > 0) {
					final ReentrantLock listenLock = brain.listenLock;
					listenLock.lock();
			        try {
			        	System.out.println("brain.notSpeak.await();");
			        	brain.notSpeak.await();
			        	System.out.println("brain.notSpeak");
			        } finally {
			        	listenLock.unlock();
			        }
				}
				try {
					targetDataLine.open(audioFormat);
					targetDataLine.start();
					byte[] fragment = new byte[1024];
					byte[] lastbyte = new byte[16];
					ais = new AudioInputStream(targetDataLine);
					boolean ready = false;
					boolean listen = false;
					while (flag) {
						targetDataLine.read(fragment, 0, fragment.length);
						if(!ready) {
							ready = true;
							System.out.println("Ready !");
						}
						int threshold = Math.abs(fragment[fragment.length - 1]);
						System.arraycopy(fragment, fragment.length - 16, lastbyte, 0, 16);
						if (listen || threshold > weight) {
							listen = true;
							baos.write(fragment);
							System.out.println(downSum + "\t首位:" + fragment[0] + ",末尾:" + Arrays.toString(lastbyte) + ",lenght:" + fragment.length);
							if (threshold <= weight) {
								downSum++;
							} else {
								downSum = 0;
							}
							if (downSum > 20) {
								listen = false;
								break;
							}
						}
					}
					byte audioData[] = baos.toByteArray();
					int length = baos.size() - 1024 * 8;
					bais = new ByteArrayInputStream(audioData, 0, length);
					ais = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());
					AudioSystem.write(ais, AudioFileFormat.Type.WAVE, cache);
					downSum = 0;
					flag = false;
					brain.memory.put(cache.toByteArray());
					cache.reset();
					final ReentrantLock speakLock = brain.speakLock;
					speakLock.lock();
			        try {
			        	System.out.println("brain.notListen.signalAll();");
			        	brain.notListen.signalAll();
			        	System.out.println("brain.notListen");
			        } finally {
			        	speakLock.unlock();
			        }
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						ais.close();
						bais.close();
						baos.reset();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				System.out.println("===================");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
