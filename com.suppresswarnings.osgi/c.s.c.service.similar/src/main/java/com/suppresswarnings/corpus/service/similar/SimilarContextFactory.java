/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.similar;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class SimilarContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return SimilarContext.CMD;
	}

	@Override
	public String description() {
		return "员工回复同义句";
	}

	@Override
	public long ttl() {
		return TimeUnit.DAYS.toMillis(1);
	}

	@Override
	public String[] requiredAuth() {
		return SimilarContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new SimilarContext(wxid, openid, content);
	}

}
