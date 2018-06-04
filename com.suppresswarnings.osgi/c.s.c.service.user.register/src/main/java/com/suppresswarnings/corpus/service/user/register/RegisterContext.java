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

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class RegisterContext extends WXContext {
	public static final String CMD = "我要注册";
	State<Context<CorpusService>> register = new State<Context<CorpusService>>() {
		int status = 0;
		/**
		 * 
		 */
		private static final long serialVersionUID = -4882781032446504875L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你好，请问怎么称呼您？");
			status = 1;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(status == 0) {
				return register;
			}
			return name;
		}

		@Override
		public String name() {
			return "Register";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> name = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6500306340308780734L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			//TODO check t as name: length, prefix, symbols
			String nameKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Name");
			String name = u.content().account().get(nameKey);
			if(name == null) {
				u.content().account().put(nameKey, t);
				u.output("太好了，以后就称呼您：" + t);
			} else {
				String nameLastKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Name", time());
				u.content().account().put(nameLastKey, name);
				u.content().account().put(nameKey, t);
				u.output("好的，以后就改成这样称呼您：" + t);
			}
			u.appendLine("如果没问题就回到首页。\n您也可以随时输入'我要注册'。");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "Name";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public RegisterContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = register;
	}
	
}
