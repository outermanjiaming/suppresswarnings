package com.suppresswarnings.util;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

public class JHook {
	private static final int SHOW = 88;
	private static final int HIDE = 89;
	private static final int LEFT = 90;
	private static final int EXIT = 91;
	private static final int UP = 92;
	private static final int DOWN = 93;
	private static final int RIGHT = 94;
	private static final int SAVE = 95;
	private static final int DELETE = 96;
	private static final int HOTKEY = 97;
	private static boolean running = true;
	static Logger log = Logger.getLogger("hook");
	static File[] files;
	static int pos = 0;
	static int location = 200;
	static int height = 880;
	static int size = 135;
	static boolean auto = true;
	static boolean hotkey = true;
	
	public static void main(String[] args) throws InterruptedException, SecurityException, IOException {
		FileHandler fileHandler = new FileHandler("jhook.log", true); 
        fileHandler.setLevel(Level.INFO); 
        fileHandler.setFormatter(new Formatter() {
			
			@Override
			public String format(LogRecord record) {
				return record.getMessage() + System.getProperty("line.separator");
			}
		}); 
        log.addHandler(fileHandler); 
		final JHook hook = new JHook();
		final JFrame frame = new JFrame("IELTS");
		final JLabel label = new JLabel("");
		label.setFont(new java.awt.Font("Dialog", 1, size));
		label.addMouseMotionListener(new MouseListener(frame,label));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setOpacity(0.4f);
		frame.setSize(150, 60);
		frame.setLocation(location, height);
		frame.setVisible(true);
		frame.setAlwaysOnTop(running);
		frame.add(label);
		final File dir = new File("audio/");
		final File done = new File("done/");
		files = dir.listFiles();
		final int length = files.length;
		final Random r = new Random();
		//WIN + 2 + C
		JIntellitype.getInstance().registerHotKey(HOTKEY, JIntellitype.MOD_WIN, 'C');
		hook.unregister(false);
		JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {

			@Override	
			public void onHotKey(int arg0) {
				auto = false;
				switch (arg0) {
				case SHOW:
					if(!frame.isVisible()) frame.setVisible(true);
					label.setText(files[r.nextInt(length)].getName().split(".mp3")[0]);
					frame.pack();
					break;
				case LEFT:
					frame.setLocation(location -= 10, frame.getY());
					break;
				case HOTKEY:
					hotkey = hook.unregister(hotkey);
					break;
				case SAVE:
					log.info(label.getText());
					
					label.setText(label.getText()+" 脳");
					frame.pack();
					break;
				case DELETE:
					String name = label.getText() + ".mp3";
					File file = new File(dir, name);
					file.renameTo(new File(done, name));
					
					label.setText(label.getText()+" 鈭�");
					frame.pack();
					break;
				case RIGHT:
					frame.setLocation(location += 10, frame.getY());
					break;
				case UP:
					label.setFont(new java.awt.Font("Dialog", 1, size += 10));
					frame.pack();
					break;
				case DOWN:
					label.setFont(new java.awt.Font("Dialog", 1, size -= 10));
					frame.pack();
					break;
				case HIDE:
					frame.dispose();
					hook.exit();
					break;
				case EXIT:
					frame.setVisible(false);
					break;
				default:
					System.out.println("nothing but " + arg0);
					break;
				}
			}
		});
		
		while(running) {
			if(auto) {
				label.setText(files[r.nextInt(length)].getName().split(".mp3")[0]);
				frame.pack();
			}
			auto =true;
			Thread.sleep(5000 + r.nextInt(3000));
		}
		
		hook.unregister(true);
		JIntellitype.getInstance().unregisterHotKey(HOTKEY);
		JIntellitype.getInstance().cleanUp();
	}
	
	public boolean unregister(boolean un){
		if(!un){
			JIntellitype.getInstance().registerHotKey(SHOW, 0, 192);
			JIntellitype.getInstance().registerHotKey(EXIT, 0, 27);
			JIntellitype.getInstance().registerHotKey(LEFT, 0, 37);
			JIntellitype.getInstance().registerHotKey(UP, 0, 38);
			JIntellitype.getInstance().registerHotKey(RIGHT, 0, 39);
			JIntellitype.getInstance().registerHotKey(DOWN, 0, 40);
			JIntellitype.getInstance().registerHotKey(HIDE, "F1");
			JIntellitype.getInstance().registerHotKey(SAVE, 0, 110);
			JIntellitype.getInstance().registerHotKey(DELETE, 0, 107);
		} else {
			JIntellitype.getInstance().unregisterHotKey(HIDE);
			JIntellitype.getInstance().unregisterHotKey(SHOW);
			JIntellitype.getInstance().unregisterHotKey(LEFT);
			JIntellitype.getInstance().unregisterHotKey(EXIT);
			JIntellitype.getInstance().unregisterHotKey(UP);
			JIntellitype.getInstance().unregisterHotKey(RIGHT);
			JIntellitype.getInstance().unregisterHotKey(SAVE);
			JIntellitype.getInstance().unregisterHotKey(DELETE);
			JIntellitype.getInstance().unregisterHotKey(DOWN);
		}
		return !un;
	}

	private void exit(){
		running = false;
		System.out.println("It will be shutdown in 5 seconds...");
	}
}

class MouseListener extends MouseAdapter {
	JFrame frame;
	JLabel label;
	MouseListener(JFrame frame, JLabel label){
		this.frame = frame;
		this.label = label;
	}
	    private boolean top = false;
	    private boolean down = false;
	    private boolean left = false;
	    private boolean right = false;
	    private Point draggingAnchor = null;
	    @Override
		public void mouseMoved(MouseEvent e) {
			if (e.getPoint().getY() == 0) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				top = true;
			} else if (Math.abs(e.getPoint().getY() - frame.getSize().getHeight()) <= 1) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				down = true;
			} else if (e.getPoint().getX() == 0) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				left = true;
			} else if (Math.abs(e.getPoint().getX() - frame.getSize().getWidth()) <= 1) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				right = true;
			} else {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				draggingAnchor = new Point(e.getX() + label.getX(), e.getY() + label.getY());
				top = false;
				down = false;
				left = false;
				right = false;
			}

		}
	            
        @Override
        public void mouseDragged(MouseEvent e) {
        	Dimension dimension = frame.getSize();
        	if(top){
            	dimension.setSize(dimension.getWidth() ,dimension.getHeight()-e.getY());
            	frame.setSize(dimension);
            	frame.setLocation(frame.getLocationOnScreen().x, frame.getLocationOnScreen().y + e.getY());
        	}else if(down){
            	dimension.setSize(dimension.getWidth() , e.getY());
            	frame.setSize(dimension);
        	}else if(left){
            	dimension.setSize(dimension.getWidth() - e.getX() ,dimension.getHeight() );
            	frame.setSize(dimension);
            	frame.setLocation(frame.getLocationOnScreen().x + e.getX(),frame.getLocationOnScreen().y );
        	}else if(right){
            	dimension.setSize(e.getX(),dimension.getHeight());
            	frame.setSize(dimension);
        	}else {	
                frame.setLocation(e.getLocationOnScreen().x - draggingAnchor.x, e.getLocationOnScreen().y - draggingAnchor.y);
        	}
        }
    }
