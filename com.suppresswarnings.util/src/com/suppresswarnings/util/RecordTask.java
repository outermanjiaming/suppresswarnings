package com.suppresswarnings.util;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RecordTask implements Runnable {
	Robot robot;
	TimeUnit unit;
	int keyEventKey;
	boolean closed;
	long gap;
	long clicked;
	public RecordTask(Robot robot, TimeUnit unit, int keyEventKey, long clicked, boolean closed, long gap) {
		this.robot = robot;
		this.unit = unit;
		this.keyEventKey = keyEventKey;
		this.clicked = clicked;
		this.closed = closed;
		this.gap = gap;
	}
	
	long lastTime = System.currentTimeMillis();
	@Override
	public void run() {
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(keyEventKey);
		robot.delay(20);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_SHIFT);
		robot.keyRelease(KeyEvent.VK_ALT);
		robot.keyRelease(keyEventKey);
		clicked ++;
		closed = !closed;
		long now = System.currentTimeMillis();
		long time = now - lastTime;
		System.out.println("( " + TimeUnit.MILLISECONDS.toSeconds(time) + "s ) " + new Date(now) + " [" + keyEventKey + "] key clicked " + clicked + " times, " + (closed ? "closed" : "opened"));
		lastTime = now;
		if(closed) {
			try {
				robot.mouseMove(-666, 444);
				robot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
				unit.sleep(gap);
				robot.mouseMove(-666, 444);
				robot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
			} catch (Exception e) {
				e.printStackTrace();
			}
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(keyEventKey);
			robot.delay(20);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_ALT);
			robot.keyRelease(keyEventKey);
			closed = !closed;
			System.out.println("( " + TimeUnit.MILLISECONDS.toSeconds(gap) + "s ) " + new Date() + " clicked " + clicked + " times, " + (closed ? "closed" : "opened"));
		}
	}
}
