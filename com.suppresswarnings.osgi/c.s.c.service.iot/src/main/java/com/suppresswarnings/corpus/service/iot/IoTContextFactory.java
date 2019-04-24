package com.suppresswarnings.corpus.service.iot;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class IoTContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new IoTContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return IoTContext.CMD;
	}

	@Override
	public String description() {
		return "通过命令我要物联网获得二维码和code";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(1);
	}

}
