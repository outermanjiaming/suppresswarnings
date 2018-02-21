package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;

public interface ContextFactory {
	public long forever = -1;
	public Context<WXService> getInstance(String openid, WXService content);
	public String command();
	public String description();
	public long ttl();
}
