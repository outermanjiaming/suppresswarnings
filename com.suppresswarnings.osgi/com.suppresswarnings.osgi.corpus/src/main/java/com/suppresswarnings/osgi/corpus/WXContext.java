package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.data.Context;
import com.suppresswarnings.osgi.data.State;

public class WXContext extends Context<Content> {

	public WXContext(Content ctx, State<Context<Content>> s) {
		super(ctx, s);
	}
}
