package com.suppresswarnings.corpus.service.mqtt;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class MQTTContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return MQTTContext.CMD;
	}

	@Override
	public String description() {
		return "消息订阅发布";
	}

	@Override
	public long ttl() {
		return TimeUnit.DAYS.toMillis(30);
	}

	@Override
	public String[] requiredAuth() {
		return MQTTContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new MQTTContext(wxid, openid, content);
	}

}
