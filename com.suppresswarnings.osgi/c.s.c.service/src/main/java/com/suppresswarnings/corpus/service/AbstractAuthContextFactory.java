/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;

public abstract class AbstractAuthContextFactory implements ContextFactory<CorpusService> {
	
	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content){
		//check authority
		String adminKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Auth", "Admin", openid);
		String admin = content.account().get(adminKey);
		if(admin == null || "None".equals(admin)) {
			String[] required = requiredAuth();
			for(String auth : required) {
				String authKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Auth", auth, openid);
				String authrized = content.account().get(authKey);
				if(authrized == null || "None".equals(authrized)) {
					WXContext context = new WXContext(wxid, openid, content);
					context.state(context.reject);
					return context;
				}
			}
		}
		return getContext(wxid, openid, content);
	}
	public abstract String[] requiredAuth();
	public abstract Context<CorpusService> getContext(String wxid, String openid, CorpusService content);
}
