package com.suppresswarnings.corpus.service.wxpy;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class WxpyContext extends WXContext {
	public static final String CMD = "我要微信机器人";
	String code = "T_Things_Robot_201905111755";
	Gson gson = new Gson();
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7296937873701644997L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String out = u.content().remoteCall(openid(), code, CMD, openid());
			logger.info("[wxpy] " + out);
			WXnews news = new WXnews();
			news.setTitle(CMD);
			news.setDescription("点击进去，用另外一个手机扫码使用，" + out);
			news.setUrl("http://suppresswarnings.com/robot.html?state=" + openid());
			news.setPicUrl("http://suppresswarnings.com/robot.png");
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "微信机器人";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	State<Context<CorpusService>> wxpy = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7296937873701644997L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			enter.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return enter;
		}

		@Override
		public String name() {
			return "微信机器人";
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	public WxpyContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(wxpy);
	}

}
