/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.customer;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class CustomerContext extends WXContext {
	public static final String CMD = "我要付款";
	String code = "Money";
	State<Context<CorpusService>> scan = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 732776875790276170L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			WXnews news = new WXnews();
			news.setTitle("请点击进入支付");
			news.setDescription("点击进入支付页面，感谢你的支持！");
			news.setUrl("http://suppresswarnings.com/payment.html?state=" + code);
			Gson gson = new Gson();
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[CustomerContext] input " + t);
			if(t.startsWith("SCAN_")) {
				code = t.substring("SCAN_".length());
				return scan;
			}
			if(CMD.equals(t)) {
				return scan;
			}
			return init;
		}

		@Override
		public String name() {
			return "扫码进入或命令进入";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public CustomerContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = scan;
	}
	
}
