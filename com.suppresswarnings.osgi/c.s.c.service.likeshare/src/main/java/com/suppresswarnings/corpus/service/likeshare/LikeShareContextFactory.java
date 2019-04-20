package com.suppresswarnings.corpus.service.likeshare;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class LikeShareContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new LikeShareContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return LikeShareContext.CMD;
	}

	@Override
	public String description() {
		return "用户扫码进入或命令输入：我要分享点赞";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(1);
	}

}
