/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.user.register;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class RegisterContextFactory implements ContextFactory<CorpusService>{

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new RegisterContext(wxid, openid, content);
	}

	@Override
	public String command() {
		return RegisterContext.CMD;
	}

	@Override
	public String description() {
		return "用户注册，记录姓名，年龄，性别，邀请码等信息";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

}
