/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.things;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class AIIoTContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new AIIoTContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return AIIoTContext.CMD;
	}

	@Override
	public String description() {
		return "扫码绑定智能家居设备";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(5);
	}

}
