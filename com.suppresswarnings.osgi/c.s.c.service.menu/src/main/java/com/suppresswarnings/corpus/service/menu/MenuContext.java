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
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class MenuContext extends WXContext {
	public static final String CMD = "我要菜单";
	public static final String[] AUTH = {"Menu"};
	State<Context<CorpusService>> menu = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8480025651279750384L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			show.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return show;
		}

		@Override
		public String name() {
			return "菜单入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> show = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8480025651279750384L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.content().factories.forEach((cmd, cf) ->{
				u.output(cmd + "\n   |-->" + cf.description());
			});
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "菜单入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public MenuContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = menu;
	}

}
