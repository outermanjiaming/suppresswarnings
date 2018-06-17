/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.setting;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

/**
 * this is the right way to set the start state.
 * @since 6/16/2018 New Zealand (8 Jolson Road, Mount Wellington)
 * @author lijiaming
 *
 */
public class SettingContext extends WXContext {
	public static final String CMD = "全局设置";
	State<Context<CorpusService>> start = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7038361576782659906L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("目前仅支持设置命令\n格式：\n新命令/已有命令\nglobal command setting\nFormat:\nNew Command/Existing Command");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				return init;
			}
			return setting;
		}

		@Override
		public String name() {
			return "start";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> setting = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7038361576782659906L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String[] nowOld = t.split("/");
			if(nowOld.length < 2) {
				u.output("设置失败，格式错误");
				return;
			}
			String now = nowOld[0].trim().toLowerCase();
			String old = nowOld[1].trim().toLowerCase();
			if(now.length() < 2 || old.length() < 2) {
				u.output("设置失败，命令太短");
				return;
			}
			ContextFactory<CorpusService> factory = u.content().factories.get(old);
			if(factory == null) {
				u.output("设置失败，旧命令不存在");
				return;
			}
			factory = u.content().factories.get(now);
			if(factory != null) {
				u.output("设置失败，命令已经存在，新命令不可用");
				return;
			}
			String nowCommandKey = String.join(Const.delimiter, "Setting", "Global", "Command", now);
			u.content().account().put(nowCommandKey, old);
			u.output("新命令已经记录，继续设置或者退出");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "退出")) {
				return init;
			}
			return setting;
		}

		@Override
		public String name() {
			return "setting";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public SettingContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = set(start);
	}

}
