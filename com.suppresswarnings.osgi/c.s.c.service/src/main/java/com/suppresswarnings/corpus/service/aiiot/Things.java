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

public abstract class Things {
	public static final String FAIL = "fail";
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Socket socket;
	String code;
	public Things(){}
	public Things(String code, Socket socket){
		this.code = code;
		this.socket = socket;
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
	public abstract String type();
	public String execute(String cmd) {
		if(socket == null || socket.isClosed()) {
			logger.info("Things ["+type()+"] socket is null or closed");
			return FAIL;
		}
		
		try {
			logger.info("Things ["+type()+"] send msg: " + cmd);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));    
	        out.write(cmd + "\n");
	        out.flush();
	        logger.info("Things [" + type() + "] waiting to read status");
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		    String msg = in.readLine();
            logger.info("Things [" + type() + "] " + msg);
            return msg;
		} catch (Exception e) {
			logger.error("Things [" + type() + "] Exception while communicating, now close it", e);
			close();
			return FAIL;
		}
	}
	
	public boolean close() {
		if(socket == null) return true;
		try {
			socket.close();
			socket = null;
			logger.info("Things [" + type() + "] closed");
		} catch (Exception e) {
			logger.error("Things [" + type() + "] Exception while close", e);
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Things [" + type() + "," + code + "]";
	}
}
