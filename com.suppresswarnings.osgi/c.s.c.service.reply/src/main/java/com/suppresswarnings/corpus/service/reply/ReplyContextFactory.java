/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.reply;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class ReplyContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new ReplyContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return ReplyContext.CMD;
	}

	@Override
	public String description() {
		return "轮流回复收集上来的语料";
	}

	@Override
	public long ttl() {
		return TimeUnit.DAYS.toMillis(5);
	}

}
