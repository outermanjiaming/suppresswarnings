/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.corpus.accost;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;

public class AccostContextFactory implements ContextFactory<CorpusService> {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public void activate() {
		logger.info("[accost] activate.");
	}

	public void deactivate() {
		logger.info("[accost] deactivate.");
	}

	public void modified() {
		logger.info("[accost] modified.");
	}
	
	@Override
	public String command() {
		return "我要搭讪";
	}

	@Override
	public String description() {
		return "闲聊搭讪";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new AccostContext(wxid, openid, content);
	}

}
