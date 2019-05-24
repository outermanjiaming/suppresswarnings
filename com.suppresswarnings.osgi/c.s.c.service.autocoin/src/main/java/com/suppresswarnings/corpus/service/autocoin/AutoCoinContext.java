package com.suppresswarnings.corpus.service.autocoin;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class AutoCoinContext extends WXContext {
	public static final String CMD = "我要自动赚钱";

	State<Context<CorpusService>> auto = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6826786535908216922L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("让手机为你工作！");
			u.output("「素朴网联」APP可以为你看新闻看视频，轻松赚金币！");
			u.output("请在浏览器打开下载链接");
			u.output("http://suppresswarnings.com/third.html");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				return this;
			}
			if(t.startsWith("SCAN_")) {
				return this;
			}
			return init;
		}

		@Override
		public String name() {
			return "我要自动赚钱";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	public AutoCoinContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(auto);
	}

}
