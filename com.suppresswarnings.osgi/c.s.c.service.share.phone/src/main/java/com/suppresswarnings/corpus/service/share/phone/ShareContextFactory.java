package com.suppresswarnings.corpus.service.share.phone;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class ShareContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return ShareContext.CMD;
	}

	@Override
	public String description() {
		return "我要共享手机号";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

	@Override
	public String[] requiredAuth() {
		return ShareContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new ShareContext(wxid, openid, content);
	}

}
