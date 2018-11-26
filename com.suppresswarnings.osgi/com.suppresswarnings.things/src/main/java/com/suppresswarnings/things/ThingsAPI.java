/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.things;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ThingsAPI extends JFrame {
	private static final long serialVersionUID = -3570646113762994098L;
	private static final int width = 300, height = 500; 
	private boolean status = false;
	private ImageIcon imageOff,imageOn,imageCode;
	private JButton bulb;
	private JLabel 	qrCode;
	public void init(){
		setTitle("client");
		setSize(width, height);
		setLocation(700, 200);
		imageOff = new ImageIcon("off.png");
		imageOn = new ImageIcon("on.png");
		imageCode = new ImageIcon("qrCode.jpg");
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
	
	public static void main(String[] args) {
		ThingsAPI client = new ThingsAPI();
		client.init();
		try {
			client.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void go() throws Exception{
		Properties config = new Properties();
		config.load(new FileInputStream("agent.properties"));
		System.out.println(config.toString());
		String server = config.getProperty("server.ssl.host", "139.199.104.224");
		String sslPorts = config.getProperty("aiiot.ssl.port", "6617");
		int sslPort = Integer.parseInt(sslPorts);
//	    System.setProperty("javax.net.debug", "ssl,handshake");
	    System.setProperty("javax.net.ssl.keyStore", config.getProperty("javax.net.ssl.keyStore"));
        System.setProperty("javax.net.ssl.trustStore", config.getProperty("javax.net.ssl.trustStore"));
        System.setProperty("javax.net.ssl.keyStorePassword", config.getProperty("javax.net.ssl.keyStorePassword"));    
        System.setProperty("javax.net.ssl.trustStorePassword",config.getProperty("javax.net.ssl.trustStorePassword"));
        
		SocketFactory factory = SSLSocketFactory.getDefault();    
		Socket sslsocket = factory.createSocket(server, sslPort);
	    String knock = String.join(",", config.getProperty("thing.type"), config.getProperty("thing.code")) + "\n";
        System.out.println("knock ======== " + knock);
	    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(),"UTF-8"));    
        out.write(knock);
        out.flush();
        InputStream is = sslsocket.getInputStream();
        //String status="off";
        while(!sslsocket.isClosed()) {
		    BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		    String msg = in.readLine();
		    System.out.println("msg ========= "+msg);
		    if(msg.equals("state:on")) {
		    	setCommand(true);
		    	System.out.println("YES");
		    } else {
		    	setCommand(false);
		    }
		    
		    if(getCommand()) {
		    	out.write("on"+"\n");
		    } else {
		    	out.write("off"+"\n");
		    }
	        out.flush();
        }
	}
	
	public boolean getCommand(){
		return status;
	}
	//控制灯的开关
	public void setCommand(boolean command) {
		if(command) {
			status=true;
			bulb.setIcon(imageOn);
		}else {
			status=false;
			bulb.setIcon(imageOff);
		}
	}
}
