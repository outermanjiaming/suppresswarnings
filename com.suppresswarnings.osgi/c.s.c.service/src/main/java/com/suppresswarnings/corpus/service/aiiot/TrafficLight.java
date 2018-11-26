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

import java.net.Socket;

public class TrafficLight extends Things {
	
	public TrafficLight(String code, Socket socket) {
		super(code, socket);
	}

	@CMD("红灯亮")
	public String redON(String input){
		return "red:on";
	}
	
	@CMD("绿灯亮")
	public String greenON(String input){
		return "green:on";
	}
	
	@CMD("黄灯亮")
	public String yellowON(String input){
		return "yellow:on";
	}
	
	@CMD("关灯")
	public String off(String input){
		return "state:off";
	}
	
	@Override
	public String type() {
		return TrafficLight.class.getSimpleName();
	}

}
