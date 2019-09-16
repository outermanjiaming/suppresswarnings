package com.suppresswarnings.corpus.service.invest;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class Invest extends WXContext {
	public static final String CMD = "我是投资人";
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5294297598563761028L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("素朴网联于2019年8月1日12:00正式开放吸收投资人。请您按需求输入以下命令：");
			u.output(what.name());
			u.output(pay.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return invest;
		}

		@Override
		public String name() {
			return "进入投资";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};

	State<Context<CorpusService>> invest = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3890297295400329808L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			enter.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(what.name().equals(t)) return what;
			if(pay.name().equals(t)) return pay;
			return init;
		}

		@Override
		public String name() {
			return "投资";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> pay = new State<Context<CorpusService>>() {


		/**
		 * 
		 */
		private static final long serialVersionUID = -1723262671394863907L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			Gson gson = new Gson();
			WXnews news = new WXnews();
			news.setTitle("投资「素朴网联」");
			news.setDescription("感谢您对素朴网联的信任，请付款之前了解清楚投资方案，如果您是误操作，您随时可以申请退款。");
			news.setUrl("http://suppresswarnings.com/payment.html?state=Invest1000000");
			news.setPicUrl("http://suppresswarnings.com/dollar.png");
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "投资付款";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	State<Context<CorpusService>> what = new State<Context<CorpusService>>() {


		/**
		 * 
		 */
		private static final long serialVersionUID = 859458841806131796L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("素朴网联的项目细节，"
					+ "请您通过邮件与我沟通，"
					+ "目前几个项目都在文章中有介绍，"
					+ "还请您阅读过目。"
					+ "目前投资方案为：总股份100万股，股价1元，投资周期3年，投资金额：1万元～10万元，具体实施需要签订合约。");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return invest;
		}

		@Override
		public String name() {
			return "投资介绍";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	
	public Invest(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(enter);
	}

}
