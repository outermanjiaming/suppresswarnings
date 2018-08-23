/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.customer;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class CustomerContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new CustomerContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return CustomerContext.CMD;
	}

	@Override
	public String description() {
		return "商铺客服，不同商铺的二维码扫码进去之后不同的商品服务";
	}

	@Override
	public long ttl() {
		return TimeUnit.HOURS.toMillis(1);
	}

}
