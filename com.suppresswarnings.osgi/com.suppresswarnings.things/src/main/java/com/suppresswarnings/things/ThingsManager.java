/**
 *       素朴网联
 *       
 *       #  #  $
 *       #     #
 *       #  #  #
 * 
 *   SuppressWarnings
 * 
 */
package com.suppresswarnings.things;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import com.suppresswarnings.things.security.UnsafeClassLoader;

@SuppressWarnings("物联网的总入口，通过ThingsManager.connect(Things)进行连接。")
public class ThingsManager {

	public static final int retry = 3;
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
	public static void connect(Things things) {
		String error = "";
		Map<String, Method> cmds = new HashMap<>();
		Method[] methods = things.getClass().getDeclaredMethods();
		List<String> commands = new ArrayList<>();
		for(Method method : methods) {
			SuppressWarnings cmd = method.getDeclaredAnnotation(SuppressWarnings.class);
			if(cmd == null) continue;
			String command = cmd.value();
			commands.add(command);
			cmds.put(command, method);
		}
		try {
			String code = System.getProperty(Things.Config.CODE, things.code()); 
			String debug = System.getProperty("debug");
			if("true".equals(debug)) System.setProperty("javax.net.debug", "ssl,handshake");
			Config prepare = new UnsafeClassLoader(Thread.currentThread().getContextClassLoader()).load(Config.code(), "prepare");
			Socket sslsocket = prepare.factory().createSocket(Things.Const.HOST, Things.Const.PORT);
			String knock = String.join(",", things.description(), code, String.join(";", commands)) + "\n";
			System.out.println("knock ======== " + knock);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream(), Things.Const.UTF8));    
			out.write(knock);
			out.flush();
			InputStream is = sslsocket.getInputStream();
			InputStreamReader reader = new InputStreamReader(is, Things.Const.UTF8);
			System.out.println("Connected!");
			
			//TODO pull & show QRCode?
			
			while(sslsocket.isConnected() && !sslsocket.isClosed()) {
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
					sslsocket.close();
					e.printStackTrace();
					System.out.println(e.getMessage() + "网络异常，请重启！");
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			error += e.getMessage();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		System.out.println(error + ", 程序退出");
	}
}
