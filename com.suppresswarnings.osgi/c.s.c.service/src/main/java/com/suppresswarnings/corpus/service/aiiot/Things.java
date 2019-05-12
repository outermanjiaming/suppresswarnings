/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.aiiot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.LoggerFactory;

public class Things implements Cloneable {
	public static final String FAIL = "fail";
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Socket socket;
	String code;
	String desc;
	long diff = 0;
	long last = System.currentTimeMillis();
	long update;
	public Things(String desc, String code, Socket socket){
		this.update = System.currentTimeMillis();
		this.diff = update - last;
		this.desc = desc;
		this.code = code;
		this.socket = socket;
		try {
			socket.setSoTimeout(6000);
			socket.setKeepAlive(true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[Things] error", e);
		}
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String code(){
		return code;
	}
	
	public Socket socket(){
		return socket;
	}
	public boolean isClosed(){
		return socket == null || socket.isClosed();
	}
	
	public String execute(String cmd, String input) {
		if(socket == null || socket.isClosed()) {
			logger.info("Things ["+code()+"] socket is null or closed");
			return FAIL;
		}
		try {
			logger.info("Things ["+code()+"] send msg: " + cmd);
			String command = String.join(";", cmd, input);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));    
	        out.write(command + "\n");
	        out.flush();
	        logger.info("Things [" + code() + "] waiting to read status");
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		    String msg = in.readLine();
            logger.info("Things [" + code() + "] " + msg);
            return msg;
		} catch (Exception e) {
			logger.error("Things [" + code() + "] Exception while communicating, now close it", e);
			close();
			return FAIL;
		}
	}
	
	public boolean close() {
		if(socket == null) return true;
		try {
			socket.close();
			socket = null;
			logger.info("Things [" + code() + "] closed");
		} catch (Exception e) {
			logger.error("Things [" + code() + "] Exception while close", e);
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Things("+desc+") [" + code + "]{"+socket+"}" + diff();
	}

	public long ping() {
		this.last = update;
		String pong = execute("ping", "pong");
		logger.debug("[Things] ping pong " + pong);
		this.update = System.currentTimeMillis();
		this.diff = update - last;
		return diff;
	}
	
	public long diff() {
		return System.currentTimeMillis() - update;
	}
}
