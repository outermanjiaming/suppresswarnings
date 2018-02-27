package com.suppresswarnings.osgi.corpus.data;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.corpus.ContextFactory;
import com.suppresswarnings.osgi.corpus.WXService;

public class QueryContextFactory implements ContextFactory {
	long ttl = TimeUnit.MINUTES.toMillis(3);
	String command = "询问";
	String description = "询问并记录数据：年龄和邮箱";
	@Override
	public Context<WXService> getInstance(String openid, WXService content) {
		return new QueryContext(openid, content);
	}
	@Override
	public String command() {
		return command;
	}
	@Override
	public String description() {
		return description;
	}
	@Override
	public long ttl() {
		return ttl;
	}

}
