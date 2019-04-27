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
		return "用户VIP+邀请机制。如果需要购买权限，请点击链接：https://suppresswarnings.com/payment.html?state=VIP";
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
