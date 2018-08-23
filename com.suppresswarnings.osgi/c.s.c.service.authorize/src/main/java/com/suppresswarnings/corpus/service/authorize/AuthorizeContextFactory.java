/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.authorize;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class AuthorizeContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new AuthorizeContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return AuthorizeContext.CMD;
	}

	@Override
	public String description() {
		return "Admin用户可以接受授权，代理用户可以申请授权，授权之后可以查看或使用quota。";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(5);
	}

}
