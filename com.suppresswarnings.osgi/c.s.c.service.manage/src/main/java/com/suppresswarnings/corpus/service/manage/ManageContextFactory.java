/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.manage;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class ManageContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return ManageContext.CMD;
	}

	@Override
	public String description() {
		return "后台管理功能集合";
	}

	@Override
	public long ttl() {
		return TimeUnit.HOURS.toMillis(1);
	}

	@Override
	public String[] requiredAuth() {
		return ManageContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new ManageContext(wxid, openid, content);
	}

}
