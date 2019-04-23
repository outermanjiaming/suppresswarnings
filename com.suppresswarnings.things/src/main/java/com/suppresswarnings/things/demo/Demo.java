/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.things.demo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.suppresswarnings.things.SuppressWarnings;
import com.suppresswarnings.things.Things;
import com.suppresswarnings.things.ThingsManager;

public class Demo extends JFrame implements Things {
	private static final long serialVersionUID = -3570646113762994098L;
	private static final int width = 300, height = 500; 
	private boolean status = false;
	private ImageIcon imageOff,imageOn;
	private JButton bulb;
	
	public Demo(){
		init();
	}
	
	public void init(){
		setTitle("client");
		setSize(width, height);
		setLocation(700, 200);
		imageOff = new ImageIcon("off.png");
		imageOn = new ImageIcon("on.png");
		bulb = new JButton(imageOff);
		bulb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				status = !status;
				bulb.setIcon(status?imageOn:imageOff);
			}
		});
		add(bulb, BorderLayout.NORTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		setVisible(true);
		repaint();
	}
	
	public boolean getCommand(){
		return status;
	}
	
	@SuppressWarnings("开灯")
	public String on(String input){
		status=true;
		bulb.setIcon(imageOn);
		return "ON";
	}
	
	@SuppressWarnings("关灯")
	public String off(String input){
		status=false;
		bulb.setIcon(imageOff);
		return "OFF";
	}
	
	@Override
	public String exception(String error) {
		for(int i=0;i<30;i++) {
			on(error);
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			off(error);
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return FAIL;
	}
	
	@Override
	public String description() {
		return "模拟灯具";
	}
	
	@Override
	public String code() {
		return "LiJiaming_Test_Things";
	}
	
	public static void main(String[] args) throws Exception {
		ThingsManager.connect(new Demo());
	}
}
