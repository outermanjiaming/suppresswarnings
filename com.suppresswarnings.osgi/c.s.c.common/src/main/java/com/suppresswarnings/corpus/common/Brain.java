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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Brain {
	BlockingQueue<byte[]> memory;
	Ear ear;
	Mouth mouth;
	
    final ReentrantLock speakLock = new ReentrantLock();
    final Condition notListen = speakLock.newCondition();
    final ReentrantLock listenLock = new ReentrantLock();
    final Condition notSpeak = listenLock.newCondition();
	
	public Brain(){
		this.memory = new LinkedBlockingQueue<>(1);
		this.ear = new Ear(this);
		this.mouth = new Mouth(this);
	}
	
	public static void main(String[] args) {
		Brain brain = new Brain();
		brain.run();
	}
	
	public void run(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ear.listen();
			}
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mouth.muttering();
			}
		}).start();
	}
}
