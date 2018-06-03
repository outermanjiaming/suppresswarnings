/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.report.raspberrypi;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class PiContextFactory implements ContextFactory<CorpusService> {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public void activate() {
		logger.info("[pi] activate.");
	}

	public void deactivate() {
		logger.info("[pi] deactivate.");
	}

	public void modified() {
		logger.info("[pi] modified.");
	}
	
	@Override
	public String command() {
		return PiContext.CMD;
	}

	@Override
	public String description() {
		return "查询我的树莓派上报的数据";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new PiContext(wxid, openid, content);
	}
}
