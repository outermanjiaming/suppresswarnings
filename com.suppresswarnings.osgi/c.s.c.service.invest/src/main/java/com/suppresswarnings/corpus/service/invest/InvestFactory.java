package com.suppresswarnings.corpus.service.invest;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class InvestFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new Invest(wxid, openid, content);
	}

	@Override
	public String command() {
		return Invest.CMD;
	}

	@Override
	public String description() {
		return "吸收投资人";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(1);
	}

}
