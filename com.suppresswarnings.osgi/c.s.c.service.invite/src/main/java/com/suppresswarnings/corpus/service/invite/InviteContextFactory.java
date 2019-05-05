package com.suppresswarnings.corpus.service.invite;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class InviteContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new InviteContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return InviteContext.CMD;
	}

	@Override
	public String description() {
		return "任何用户都可以邀请朋友加入素朴网联";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(1);
	}

}
