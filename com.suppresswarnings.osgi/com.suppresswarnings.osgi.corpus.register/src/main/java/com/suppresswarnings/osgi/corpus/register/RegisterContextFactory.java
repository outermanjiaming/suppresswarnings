package com.suppresswarnings.osgi.corpus.register;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.corpus.ContextFactory;
import com.suppresswarnings.osgi.corpus.WXService;

public class RegisterContextFactory implements ContextFactory {
	long ttl = TimeUnit.MINUTES.toMillis(20);
	String command = "注册";
	String description = "注册成为不同类型的用户：1.游客，2.用户（邮箱），3.领导（邀请码）";
	@Override
	public Context<WXService> getInstance(String openid, WXService content) {
		RegisterContext context = new RegisterContext(openid, content);
		context.init(context.R0);
		return context;
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
