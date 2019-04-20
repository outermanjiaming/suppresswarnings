package com.suppresswarnings.corpus.service.likeshare;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class LikeShareContext extends WXContext {
	public static final String CMD = "我要分享点赞";
	State<Context<CorpusService>> likeshare = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 9034450850853785003L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			Gson gson = new Gson();
			WXnews news = new WXnews();
			news.setTitle("进来点赞，先赞一个亿，一起分红！");
			news.setDescription("「分享点赞可以分红」进来点赞，先赞一个亿！所有参与点赞者均有分红！");
			if(t.startsWith("SCAN_")) {
				String qrScene = t.substring("SCAN_".length());
				news.setUrl("http://SuppressWarnings.com/like.html?state="+qrScene);
			} else {
				news.setUrl("http://SuppressWarnings.com/like.html");
			}
			news.setPicUrl("http://SuppressWarnings.com/like.png");
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				return likeshare;
			}
			if(t.startsWith("SCAN_")) {
				return likeshare;
			}
			return init;
		}

		@Override
		public String name() {
			return "我要分享点赞";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public LikeShareContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = likeshare;
	}

}
