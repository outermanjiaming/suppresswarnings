/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.daigou;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class DaigouContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new DaigouContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return DaigouContext.CMD;
	}

	@Override
	public String description() {
		return "用户和代理进入代购页面";
	}

	@Override
	public long ttl() {
		return 72000;
	}

}
