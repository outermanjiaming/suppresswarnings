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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class ThingsManager {

	public static String execute(Things things, Map<String, Method> cmds, String call, String msg) {
		Method method = cmds.get(call);
		if(method == null) {
			return "notfound";
		} else {
			try {
				Object object = method.invoke(things, msg);
				return String.valueOf(object);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return "error";
	}
	public static void connect(Things things, String configPath) {
		Map<String, Method> cmds = new HashMap<>();
		Method[] methods = things.getClass().getDeclaredMethods();
		List<String> commands = new ArrayList<>();
		for(Method method : methods) {
			CMD cmd = method.getDeclaredAnnotation(CMD.class);
			if(cmd == null) continue;
			String command = cmd.value();
			commands.add(command);
			cmds.put(command, method);
		}
		
		try {
			Properties config = new Properties();
			config.load(new FileInputStream(configPath));
			System.out.println(config.toString());
			String server = config.getProperty("server.ssl.host", "139.199.104.224");
			String sslPorts = config.getProperty("aiiot.ssl.port", "6617");
			String code = config.getProperty("thing.code"); 
			int sslPort = Integer.parseInt(sslPorts);
			String debug = config.getProperty("debug");
			if("true".equals(debug)) System.setProperty("javax.net.debug", "ssl,handshake");
			System.setProperty("javax.net.ssl.keyStore", config.getProperty("javax.net.ssl.keyStore"));
			System.setProperty("javax.net.ssl.trustStore", config.getProperty("javax.net.ssl.trustStore"));
			System.setProperty("javax.net.ssl.keyStorePassword", config.getProperty("javax.net.ssl.keyStorePassword"));    
			System.setProperty("javax.net.ssl.trustStorePassword",config.getProperty("javax.net.ssl.trustStorePassword"));
			
			SocketFactory factory = SSLSocketFactory.getDefault();    
			Socket sslsocket = factory.createSocket(server, sslPort);
			String knock = String.join(",", things.description(), code, String.join(";", commands)) + "\n";
			System.out.println("knock ======== " + knock);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(),"UTF-8"));    
			out.write(knock);
			out.flush();
			InputStream is = sslsocket.getInputStream();
			InputStreamReader reader = new InputStreamReader(is,"UTF-8");
			while(!sslsocket.isClosed()) {
			    try {
					BufferedReader in = new BufferedReader(reader);
					String msg = in.readLine();
					System.out.println("msg ========= " + msg);
					String call = msg.trim();
					String ret = execute(things, cmds, call, msg);
					System.out.println("ret ========= " + ret);
					out.write(ret + "\n");
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
