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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.suppresswarnings.things.CMD;
import com.suppresswarnings.things.Things;
import com.suppresswarnings.things.ThingsManager;

public class Demo extends JFrame implements Things {
	private static final long serialVersionUID = -3570646113762994098L;
	private static final int width = 300, height = 500; 
	private boolean status = false;
	private ImageIcon imageOff,imageOn,imageCode;
	private JButton bulb;
	private JLabel 	qrCode;
	
	public Demo(){
		init();
	}
	public void init(){
		setTitle("client");
		setSize(width, height);
		setLocation(700, 200);
		imageOff = new ImageIcon("off.png");
		imageOn = new ImageIcon("on.png");
		imageCode = new ImageIcon("demo.jpg");
		bulb = new JButton(imageOff);
		qrCode = new JLabel(imageCode);
		bulb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				status = !status;
				bulb.setIcon(status?imageOn:imageOff);
			}
		});
		add(bulb,BorderLayout.NORTH);
		add(qrCode,BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		setVisible(true);
	}
	
	public boolean getCommand(){
		return status;
	}
	
	@CMD("开灯")
	public String on(String input){
		status=true;
		bulb.setIcon(imageOn);
		return "ON";
	}
	
	@CMD("关灯")
	public String off(String input){
		status=false;
		bulb.setIcon(imageOff);
		return "OFF";
	}

	@Override
	public String description() {
		return "模拟灯具";
	}

	public static void main(String[] args) {
		ThingsManager.connect(new Demo(), "demo.properties");
	}
}
