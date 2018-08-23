/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.collect;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class CollectContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new CollectContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return CollectContext.CMD;
	}

	@Override
	public String description() {
		return "用户发起收集语料任务，分享给朋友们进行收集";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(5);
	}

}
