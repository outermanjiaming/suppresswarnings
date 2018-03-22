package com.suppresswarnings.osgi.corpus.pi;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.corpus.ContextFactory;
import com.suppresswarnings.osgi.corpus.PiContext;
import com.suppresswarnings.osgi.corpus.WXService;

public class PiContextFactory implements ContextFactory {

	@Override
	public Context<WXService> getInstance(String openid, WXService content) {
		return new PiContext(openid, content);
	}

	@Override
	public String command() {
		return "我的树莓派";
	}

	@Override
	public String description() {
		return "帮助树莓派玩家找到自己的IP从而可以ssh连接";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(2);
	}

}
