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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Brain {
	
	BlockingQueue<byte[]> memory;
	BufferedImage image;
	Ear ear;
	Mouth mouth;
	Eye eye;
	Face face;
	Thinker thinker;
	class Face extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 895910330481860809L;
		BufferedImage image = null;
		public Face(String s) {
			super(s);
		}
		public void show(BufferedImage image) {
			this.image = image;
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(image, 0, 0, 200, 200, null);
			System.out.println("New image seen");
		}
		
	}
    final ReentrantLock lock = new ReentrantLock();
    final Condition notListen = lock.newCondition();
    final Condition notSpeak = lock.newCondition();
	public Brain(){
		this.image = null;
		this.memory = new LinkedBlockingQueue<>(1);
		this.ear = new Ear(this);
		this.mouth = new Mouth(this);
		this.eye = new Eye(this);
		this.face = new Face("Face");
		this.thinker = new Thinker();
		face.setSize(200, 200);
		face.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		face.setVisible(true);
	}
	
	public void run(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ear.listen();
					System.out.println("Ear is deaf");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					mouth.muttering();
					System.out.println("Mouth is close");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	
	public void look(){
		this.eye.see();
	}
	
	public void ready(){
		this.face.setTitle(this.face.getTitle() + "--准备好了");
	}
	
	
	public static void main(String[] args) {
		Brain brain = new Brain();
		brain.run();
	}
	public static final String DIR = "/Users/lijiaming/Learn/meyou/test/";
	public void see(BufferedImage image) {
		this.image = image;
		System.out.println("I can see this: " + image);
		this.face.setTitle("让我想一想");
		long time = System.currentTimeMillis();
		int i = thinker.decide(image);
		String name = "";
		if(i==0) name = "李嘉铭";
		else name = "胡兰兰";
		
		time = System.currentTimeMillis() - time;
		time = time / 1000;
		this.face.setTitle(time + "s " + name);
		
		try {
			ImageIO.write(image, "jpg", new File(DIR, name + System.currentTimeMillis() + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		face.show(image);
		face.repaint();
	}

	public void learn(){
		try {
			thinker.learn();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
