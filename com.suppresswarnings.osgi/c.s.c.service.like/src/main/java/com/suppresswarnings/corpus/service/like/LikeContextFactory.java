package com.suppresswarnings.corpus.service.like;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class LikeContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new LikeContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return LikeContext.CMD;
	}

	@Override
	public String description() {
		return "线上项目：先赞一个亿一起分红";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(10);
	}

}
