package com.suppresswarnings.corpus.service.captcha;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class CaptchaContext extends WXContext {
	public static final String CMD = "临时支付";
	String numbers = null;
	String number = null;

	State<Context<CorpusService>> captcha = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8860395301948178002L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String code = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Payment", "Keyword", t));
			if(!u.content().isNull(code)) {
				WXnews news = new WXnews();
				Gson gson = new Gson();
				news.setTitle("临时支付通道");
				news.setDescription("因为您输入了：" + t + ", 请按照约定支付金额，支付成功之后将通知对方。");
				news.setUrl("http://suppresswarnings.com/payment.html?state=" + code);
				news.setPicUrl("http://SuppressWarnings.com/like.png");
				String json = gson.toJson(news);
				u.output("news://" + json);
			} else {
				u.output("临时支付通道不存在，请确认");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[captcha] input: " + t);
			return init;
		}

		@Override
		public String name() {
			return "支付链接";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3851369518154268501L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			captcha.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return captcha;
		}

		@Override
		public String name() {
			return "临时支付";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};

	public CaptchaContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = enter;
	}

}
