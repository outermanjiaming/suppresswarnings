package com.suppresswarnings.corpus.service.autocoin;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class AutoCoinContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new AutoCoinContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return AutoCoinContext.CMD;
	}

	@Override
	public String description() {
		return "「自动刷金币」公告";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(1);
	}

}
