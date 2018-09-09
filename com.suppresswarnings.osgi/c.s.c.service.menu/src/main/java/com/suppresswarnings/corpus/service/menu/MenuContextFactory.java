/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.menu;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class MenuContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return MenuContext.CMD;
	}

	@Override
	public String description() {
		return "展示所有功能菜单";
	}

	@Override
	public long ttl() {
		return 7200;
	}

	@Override
	public String[] requiredAuth() {
		return MenuContext.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new MenuContext(wxid, openid, content);
	}

}
