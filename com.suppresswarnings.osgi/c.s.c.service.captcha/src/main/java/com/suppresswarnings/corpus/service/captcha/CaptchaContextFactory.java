package com.suppresswarnings.corpus.service.captcha;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class CaptchaContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return CaptchaContext.CMD;
	}

	@Override
	public String description() {
		return "素朴网联共享vip获取登录验证码";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

	@Override
	public String[] requiredAuth() {
		return CaptchaContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new CaptchaContext(wxid, openid, content);
	}
	
}
