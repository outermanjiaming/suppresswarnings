/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.corpus.teach;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class TeachContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new TeachContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return TeachContext.CMD;
	}

	@Override
	public String description() {
		return "教程序说话";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(15);
	}

}
