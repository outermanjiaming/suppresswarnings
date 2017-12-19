package com.suppresswarnings.util;

import java.awt.BorderLayout;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Keyboard extends JFrame implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6967961653164581153L;
	Robot robot;
	long clicked = 0;
	boolean closed = true;
	ScheduledExecutorService service;
	private JTextArea statusBar;
	public Keyboard(String title) {
		 super(title);   
         statusBar = new JTextArea();   
         statusBar.setAutoscrolls(true);
         JScrollPane scrollPane = new JScrollPane();
         scrollPane.setViewportView(statusBar);
         scrollPane.setAutoscrolls(true);
         scrollPane.setFocusable(true);
         getContentPane().add(scrollPane, BorderLayout.CENTER);
         statusBar.addMouseListener(this);   
         setSize(465,100);
         setLocation(-695, 800);
         setVisible(true);
		try {
			robot = new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void autoCtrlShiftAltKeyClick(int keyEventKey, long start, long period, TimeUnit unit, long gap){
		Runnable command = new Runnable() {
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
				println("( " + TimeUnit.MILLISECONDS.toSeconds(time) + "s ) " + new Date(now) + " [" + keyEventKey + "] key clicked " + clicked + " times, " + (closed ? "closed" : "opened"));
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
					println("( " + TimeUnit.MILLISECONDS.toSeconds(gap) + "s ) " + new Date() + " clicked " + clicked + " times, " + (closed ? "closed" : "opened"));
				}
			}
		};
		
		if(service == null) service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(command, start, period, unit);
	}
	
	public static void main(String[] args) {
		System.out.println("It'll click [Ctrl + Shift + Alt + Win] automatically with fixed delay.");
		System.out.println("[#start]s [#delay]m [#break]s");
		if(args.length < 3) {
			System.out.println("Wrong args...");
			return;
		}
		long start = Long.parseLong(args[0]);
		long delay = Long.parseLong(args[1]);
		long broke = Long.parseLong(args[2]);
		String title = String.format("start in %ss, auto-click every %sm, rest %ss each time", start, delay, broke);
		System.out.println(title);
		System.out.println("Continue? y/N");
		Scanner scan = new Scanner(System.in);
		String yes = scan.nextLine();
		scan.close();
		if(!"y".equals(yes)) {
			return;
		}
		Keyboard kb = new Keyboard(title);
		kb.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		kb.autoCtrlShiftAltKeyClick(KeyEvent.VK_WINDOWS, TimeUnit.SECONDS.toMillis(start), TimeUnit.MINUTES.toMillis(delay), TimeUnit.MILLISECONDS, TimeUnit.SECONDS.toMillis(broke));
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		println("mouseClicked " + e.getLocationOnScreen());
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}
	
	public void println(String line) {
		statusBar.append(line+"\n");
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
}
