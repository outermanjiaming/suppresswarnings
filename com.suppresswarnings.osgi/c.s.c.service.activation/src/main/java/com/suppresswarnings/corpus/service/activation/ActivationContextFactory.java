package com.suppresswarnings.corpus.service.activation;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class ActivationContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new ActivationContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return ActivationContext.CMD;
	}

	@Override
	public String description() {
		return "获取激活码";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(1);
	}

}
