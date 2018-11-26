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

public class Bulb extends Things {
	public Bulb(String code, Socket socket) {
		super(code, socket);
	}

	@CMD("开灯")
	public String turnOn(String input) {
		//action
		logger.info("[Bulb] the light bulb is going to be on");
		return "state:on";
	}
	
	@CMD("关灯")
	public String turnOff(String input){
		//something else
		logger.info("[Bulb] the light bulb is off");
		return "state:off";
	}

	@Override
	public String type() {
		return Bulb.class.getSimpleName();
	}
}
