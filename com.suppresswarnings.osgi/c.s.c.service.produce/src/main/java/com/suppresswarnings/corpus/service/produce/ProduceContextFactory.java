/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.produce;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class ProduceContextFactory implements ContextFactory<CorpusService> {
	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new ProduceContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return ProduceContext.CMD;
	}

	@Override
	public String description() {
		return "用户扫码参与上报语料数据";
	}

	@Override
	public long ttl() {
		return TimeUnit.HOURS.toMillis(3);
	}

}
