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
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class CustomerContext extends WXContext {
	public static final String CMD = "我要付款";
	String code = "Money";
	State<Context<CorpusService>> payment = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4480615630073732001L;
		String keyword = null;
		State<Context<CorpusService>> confirm = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8778707752022211729L;
			State<Context<CorpusService>> complete = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 6403829348528354168L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", openid(), "Price"), "100");
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", openid(), "Reason"), "临时支付通道");
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", openid(), "Type"), "Data");
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", openid(), "Bossid"), openid());
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", openid(), "What"), "请按照约定的金额付款");
					u.content().setGlobalCommand(keyword, "临时支付", openid(), time());
					u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Payment", "Keyword", keyword), openid());
					u.output("配置成功，现在可以输入关键词试试：" + keyword);
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					return init;
				}

				@Override
				public String name() {
					return "完成配置";
				}

				@Override
				public boolean finish() {
					return true;
				}
				
			};

			@Override
			public void accept(String t, Context<CorpusService> u) {
				keyword = "临时支付" + t;
				String code = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Payment", "Keyword", t));
				if(!u.content().isNull(code) && !openid().equals(code)) {
					keyword = null;
					u.output("关键词被占用了，请重新输入：" + payment.name());
				} else {
					u.output("请输入：确认。接下来，让对方在公众号素朴网联输入：" + keyword + "，对方点击支付链接输入约定好的金额进行支付，支付成功之后，我们将通知你，并将金额通过企业支付的方式转到你的微信零钱(扣除0.012的手续费)，请确保你的微信已经实名认证。如果想更换关键词，请输入：" + payment.name());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(payment.name().equals(t) || keyword == null) {
					return payment;
				}
				return complete;
			}

			@Override
			public String name() {
				return "确认关键词";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("请输入收款关键词：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return confirm;
		}

		@Override
		public String name() {
			return "我要收款";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
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
			} else if(payment.name().equals(t)) {
				return payment;
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
