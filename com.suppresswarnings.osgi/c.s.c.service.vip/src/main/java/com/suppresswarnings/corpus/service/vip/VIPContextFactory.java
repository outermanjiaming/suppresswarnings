package com.suppresswarnings.corpus.service.vip;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class VIPContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return VIPContext.CMD;
	}

	@Override
	public String description() {
		return "素朴网联VIP机制，提供更高级的操作。";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

	@Override
	public String[] requiredAuth() {
		return VIPContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new VIPContext(wxid, openid, content);
	}
}
