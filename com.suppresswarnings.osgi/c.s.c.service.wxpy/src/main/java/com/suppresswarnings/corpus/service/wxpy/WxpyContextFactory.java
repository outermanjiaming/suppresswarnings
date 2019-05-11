package com.suppresswarnings.corpus.service.wxpy;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class WxpyContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new WxpyContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return WxpyContext.CMD;
	}

	@Override
	public String description() {
		return "用户登录微信机器人，自动回复所有对话";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(2);
	}

}
