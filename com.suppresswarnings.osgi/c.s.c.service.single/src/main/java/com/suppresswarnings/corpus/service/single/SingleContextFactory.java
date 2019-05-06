package com.suppresswarnings.corpus.service.single;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class SingleContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new SingleContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return SingleContext.CMD;
	}

	@Override
	public String description() {
		return "单身入口";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(10);
	}

}
