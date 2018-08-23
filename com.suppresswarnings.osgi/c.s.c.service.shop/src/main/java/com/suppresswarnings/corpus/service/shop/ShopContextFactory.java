/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.shop;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class ShopContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new ShopContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return ShopContext.CMD;
	}

	@Override
	public String description() {
		return "商家扫码创建商铺客服二维码";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(15);
	}

}
