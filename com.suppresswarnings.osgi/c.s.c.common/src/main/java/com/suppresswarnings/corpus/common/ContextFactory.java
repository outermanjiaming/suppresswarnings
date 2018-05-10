package com.suppresswarnings.corpus.common;

public interface ContextFactory<T> {
	public long forever = -1;
	public Context<T> getInstance(String openid, T content);
	public String command();
	public String description();
	public long ttl();
}
