package com.suppresswarnings.util;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RecordTask implements Runnable {
	Point click;
	Robot robot;
	TimeUnit unit;
	int keyEventKey;
	boolean closed;
	long gap;
	long clicked;
	public RecordTask(Point click, Robot robot, TimeUnit unit, int keyEventKey, long clicked, boolean closed, long gap) {
		this.click = click;
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
		click();
		if(closed) {
			try {
				unit.sleep(gap);
			} catch (Exception e) {
			}
			click();
		}
	}
	
	public void click(){
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_SHIFT);
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(keyEventKey);
		robot.delay(20);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_SHIFT);
		robot.keyRelease(KeyEvent.VK_ALT);
		robot.keyRelease(keyEventKey);
		
		long now = System.currentTimeMillis();
		long time = now - lastTime;
		System.out.println("( " + TimeUnit.MILLISECONDS.toSeconds(time) + "s ) " + new Date(now) + " [" + keyEventKey + "] key clicked " + clicked + " times, " + (closed ? "closed" : "opened"));
		System.out.println("click [" + click.getX()+"," + click.getY() + "]");
		robot.mouseMove((int)click.getX(), (int)click.getY());
		robot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
		closed = !closed;
		clicked ++;
		lastTime = now;
	}
}
