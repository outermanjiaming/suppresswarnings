package com.suppresswarnings.osgi.corpus.invite;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.corpus.ContextFactory;
import com.suppresswarnings.osgi.corpus.InviteContext;
import com.suppresswarnings.osgi.corpus.WXService;

public class InviteContextFactory implements ContextFactory {

	@Override
	public Context<WXService> getInstance(String openid, WXService content) {
		return new InviteContext(openid, content);
	}

	@Override
	public String command() {
		return "我要邀请";
	}

	@Override
	public String description() {
		return "目前Leader用户可以使用邀请名额";
	}

	@Override
	public long ttl() {
		return 120000;
	}

}
