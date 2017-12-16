package com.suppresswarnings.util;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Keyboard {
	Robot robot;
	long clicked = 0;
	boolean closed = true;
	ScheduledExecutorService service;
	public Keyboard() {
		try {
			robot = new Robot();
			robot.setAutoDelay(20);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void autoClick(int key, long start, long period, TimeUnit unit, long gap){
		Runnable command = new Runnable() {
			long lastTime = System.currentTimeMillis();
			@Override
			public void run() {
				robot.keyPress(key);
				robot.keyRelease(key);
				clicked ++;
				closed = !closed;
				long now = System.currentTimeMillis();
				long time = now - lastTime;
				System.out.println("( " + TimeUnit.MILLISECONDS.toSeconds(time) + "s ) " + new Date(now) + " [" + key + "] key clicked " + clicked + " times, " + (closed ? "closed" : "opened"));
				lastTime = now;
				if(closed) {
					try {
						unit.sleep(gap);
					} catch (Exception e) {
						e.printStackTrace();
					}
					robot.keyPress(key);
					robot.keyRelease(key);
					closed = !closed;
					System.out.println("( " + gap + "ms ) " + new Date() + " [" + key + "] key clicked " + clicked + " times, " + (closed ? "closed" : "opened"));
				}
			}
		};
		
		if(service == null) service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(command, start, period, unit);
	}
	
	public static void main(String[] args) {
		Keyboard kb = new Keyboard();
		kb.autoClick(KeyEvent.VK_F12, TimeUnit.SECONDS.toMillis(1), TimeUnit.MINUTES.toMillis(28), TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS.toMillis(666));
	}
}
