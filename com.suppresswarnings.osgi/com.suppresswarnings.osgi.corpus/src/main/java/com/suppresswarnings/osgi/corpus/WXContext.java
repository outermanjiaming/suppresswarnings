package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class WXContext extends Context<WXService> {

	public WXContext(WXService ctx, State<Context<WXService>> s) {
		super(ctx, s);
	}
}
