package com.suppresswarnings.util;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
	Point click;
	Robot robot;
	Runnable command;
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
		setSize(455, 180);
		click = new Point(-666, 800);
		setLocation(click);
		setVisible(true);
		setAlwaysOnTop(true);
		try {
			robot = new Robot();
		} catch (Exception e) {
		}

	}
	
	
	public void autoCtrlShiftAltKeyClick(int keyEventKey, long start, long period, TimeUnit unit, long gap){
		command = new RecordTask(click, robot, unit, keyEventKey, 0, true, gap);
		if(service == null) service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(command, start, period, unit);
	}
	public void onceCtrlShiftAltKeyClick(int keyEventKey, long start, long period, TimeUnit unit, long gap) {
		command = new RecordTask(click, robot, unit, keyEventKey, 0, false, period);
		command.run();
		this.dispose();
	}
	
	public static void main(String[] args) {
		System.out.println("It'll click [Ctrl + Shift + Alt + Win] automatically.");
		System.out.println("[#start]s [#delay]m [#break]s");
		if(args.length < 3) {
			System.out.println("Wrong args...");
			return;
		}
		long start = Long.parseLong(args[0]);
		long delay = Long.parseLong(args[1]);
		long broke = Long.parseLong(args[2]);
		boolean once = false;
		if(args.length > 3) {
			if("once".equals(args[3])) {
				once = true;
			}
		}
		String title = String.format("start in %ss, %s %sm, then rest %ss", start, once ? "click ONCE" : "auto-click every", delay, broke);
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
		kb.println("Usage:\n\t1. DARG this little frame \n\t2. ONTO the recording screen, \n\t3. CLICK on this frame, \n\t4. MOVE this frame away");
		if(once) {
			kb.onceCtrlShiftAltKeyClick(KeyEvent.VK_WINDOWS, TimeUnit.SECONDS.toMillis(start), TimeUnit.MINUTES.toMillis(delay), TimeUnit.MILLISECONDS, TimeUnit.SECONDS.toMillis(broke));
		} else {
			kb.autoCtrlShiftAltKeyClick(KeyEvent.VK_WINDOWS, TimeUnit.SECONDS.toMillis(start), TimeUnit.MINUTES.toMillis(delay), TimeUnit.MILLISECONDS, TimeUnit.SECONDS.toMillis(broke));
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Point clickOn = e.getLocationOnScreen();
		click.setLocation(clickOn);
		println("\t[NOTE] MOVE this frame AWAY\n\tIt'll click there: [" + click.getX() + "," + click.getY() + "]");
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
