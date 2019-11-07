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
import com.suppresswarnings.corpus.common.State;

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
					context.state(new State<Context<CorpusService>>() {
						
						/**
						 * 
						 */
						private static final long serialVersionUID = 6280729600212212503L;
						boolean first = true;

						@Override
						public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
							if(first) {
								first = false;
								return this;
							}
							return context.init;
						}
						
						@Override
						public void accept(String t, Context<CorpusService> u) {
							u.output("（请联系我们：0756-6145606）高级功能，暂时无权使用该服务：" + description());
						}
						
						@Override
						public String name() {
							return "权限控制";
						}
						
						@Override
						public boolean finish() {
							return true;
						}
					});
					return context;
				}
			}
		}
		return getContext(wxid, openid, content);
	}
	public abstract String[] requiredAuth();
	public abstract Context<CorpusService> getContext(String wxid, String openid, CorpusService content);
}
