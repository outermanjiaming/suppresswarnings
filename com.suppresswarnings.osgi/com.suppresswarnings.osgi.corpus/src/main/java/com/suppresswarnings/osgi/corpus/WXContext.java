package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;

public class WXContext extends Context<WXService> {
	String openid;
	public WXContext(String openid, WXService ctx) {
		super(ctx);
		this.openid = openid;
	}

	public String openid(){
		return openid;
	}

	@Override
	public void log(String msg) {
		content().logger.info("[WXContext] " + msg);
	}
}
