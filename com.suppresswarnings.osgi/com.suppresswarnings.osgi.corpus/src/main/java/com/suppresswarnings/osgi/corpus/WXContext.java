package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class WXContext extends Context<Content> {

	public WXContext(Content ctx, State<Context<Content>> s) {
		super(ctx, s);
	}
}
