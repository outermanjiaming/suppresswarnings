package com.suppresswarnings.corpus.service.vip;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class VIPContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new VIPContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return VIPContext.CMD;
	}

	@Override
	public String description() {
		return "用户VIP+邀请机制";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

}
