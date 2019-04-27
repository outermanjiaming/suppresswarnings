package com.suppresswarnings.corpus.service.crew;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class CrewContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new CrewContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return CrewContext.CMD;
	}

	@Override
	public String description() {
		return "素朴网联VIP邀请加入";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(2);
	}

}
