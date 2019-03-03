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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class CustomerContext extends WXContext {
	public static final String CMD = "我要付款";
	List<KeyValue> goods = new ArrayList<KeyValue>();
	
	String code;
	State<Context<CorpusService>> scan = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 732776875790276170L;
		boolean seen = false;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			WXnews news = new WXnews();
			news.setTitle("请点击进入支付");
			news.setDescription("点击进入支付页面，支付完成之后可以得到验证码！");
			news.setUrl("http://suppresswarnings.com/payment.html?state=" + code);
			Gson gson = new Gson();
			String json = gson.toJson(news);
			u.output("news://" + json);
			seen = true;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[CustomerContext] input " + t);
			if(t.startsWith("SCAN_")) {
				code = t.substring("SCAN_".length());
				return scan;
			}
			if(CMD.equals(t)) {
				return choose;
			}
			if(seen) {
				seen = false;
				return init;
			}
			return choose;
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
	
	
	State<Context<CorpusService>> choose = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2520059662255761593L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请输入要购买的商品：");
			for(KeyValue kv : goods) u.output(kv.key());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return buy;
		}

		@Override
		public String name() {
			return "选择商品";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> buy = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5562021136568829399L;
		boolean have = false;
		/**
		 * 
		 */
		@Override
		public void accept(String t, Context<CorpusService> u) {
			for(KeyValue kv : goods) {
				if(t.contains(kv.key())) {
					code = kv.value();
					have = true;
					scan.accept(t, u);
					break;
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(have) {
				have = false;
				return init;
			}
			return choose;
		}

		@Override
		public String name() {
			return "提供购买页面";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public CustomerContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		
		this.state = scan;
		
		this.goods.add(new KeyValue("赚钱软件", "P_Pay_Software_1551101705933_317"));
		this.goods.add(new KeyValue("面对面支付", "Money"));
		this.goods.add(new KeyValue("代购权限", "DaigouAgent"));
	}
	
}
