package com.suppresswarnings.corpus.service.moneytree;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class MoneyTreeFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new MoneyTree(wxid, openid, content);
	}

	@Override
	public String command() {
		return MoneyTree.CMD;
	}

	@Override
	public String description() {
		return "我的摇钱树+绑定摇钱树";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(2);
	}

}
