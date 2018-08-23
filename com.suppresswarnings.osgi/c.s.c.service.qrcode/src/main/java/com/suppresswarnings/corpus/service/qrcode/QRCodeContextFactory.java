/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.qrcode;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class QRCodeContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new QRCodeContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return QRCodeContext.CMD;
	}

	@Override
	public String description() {
		return "生成二维码";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

}
