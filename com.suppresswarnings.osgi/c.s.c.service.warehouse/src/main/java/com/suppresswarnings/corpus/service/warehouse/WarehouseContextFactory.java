/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.warehouse;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class WarehouseContextFactory implements ContextFactory<CorpusService> {

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new WarehouseContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return WarehouseContext.CMD;
	}

	@Override
	public String description() {
		return "仓库管理";
	}

	@Override
	public long ttl() {
		return TimeUnit.HOURS.toMillis(1);
	}

}
