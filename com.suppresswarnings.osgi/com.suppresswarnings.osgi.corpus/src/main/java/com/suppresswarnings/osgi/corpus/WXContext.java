package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class WXContext extends Context<WXService> {
	String openid;
	public WXContext(String openid, WXService ctx, State<Context<WXService>> s) {
		super(ctx, s);
		this.openid = openid;
	}

	public String openid(){
		return openid;
	}
}
