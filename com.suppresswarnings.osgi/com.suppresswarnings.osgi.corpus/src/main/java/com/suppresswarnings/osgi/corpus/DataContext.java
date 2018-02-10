package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class DataContext extends WXContext {

	public DataContext(String openid, WXService ctx, State<Context<WXService>> s) {
		super(openid, ctx, s);
	}

}
