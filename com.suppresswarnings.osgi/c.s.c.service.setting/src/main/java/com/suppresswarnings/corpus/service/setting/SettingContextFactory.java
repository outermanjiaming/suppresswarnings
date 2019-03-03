/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.setting;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class SettingContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return SettingContext.CMD;
	}

	@Override
	public String description() {
		return "setting global commands";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(2);
	}

	@Override
	public String[] requiredAuth() {
		return SettingContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new SettingContext(wxid, openid, content);
	}

}
