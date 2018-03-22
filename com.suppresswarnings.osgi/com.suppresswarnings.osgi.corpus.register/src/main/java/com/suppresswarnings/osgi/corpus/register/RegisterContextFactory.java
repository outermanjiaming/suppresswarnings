package com.suppresswarnings.osgi.corpus.register;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.corpus.ContextFactory;
import com.suppresswarnings.osgi.corpus.RegisterContext;
import com.suppresswarnings.osgi.corpus.WXService;

public class RegisterContextFactory implements ContextFactory {
	long ttl = TimeUnit.MINUTES.toMillis(20);
	String command = "我要注册";
	String description = "注册成为不同类型的用户：\n1.游客，\n2.用户（邮箱），\n3.领导（邀请码）\n请输入数字：";
	@Override
	public Context<WXService> getInstance(String openid, WXService content) {
		RegisterContext context = new RegisterContext(openid, content);
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
