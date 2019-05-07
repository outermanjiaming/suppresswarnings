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
			u.output("让你的闲置手机帮你工作！「自动刷金币」可以代替你看新闻看视频，轻松赚金币！使用须知：仅支持安卓手机，并且需要root权限，需要后台运行权限。请不要在重要的私人手机上开放root权限！需要下载「自动刷金币」app请在浏览器打开该链接：http://suppresswarnings.com/third.html");
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
