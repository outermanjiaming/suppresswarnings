/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.daigou;

import java.io.File;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class DaigouContext extends WXContext {
	public static final String CMD = "我要代购";
	public static final String DAIGOU_AUTH = "DaigouAgent";
	public Gson gson = new Gson();
	
	State<Context<CorpusService>> daigou = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4631970750286096979L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			WXnews news = new WXnews();
			news.setTitle("代购页面");
			news.setDescription("点击查看二维码");
			news.setUrl("http://SuppressWarnings.com/daigou.html");
			String json = gson.toJson(news);
			u.output("news://" + json);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(u.content().authrized(openid(), DAIGOU_AUTH)) {
				return agents;
			} else {
				return others;
			}
		}

		@Override
		public String name() {
			return "代购入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	

	State<Context<CorpusService>> agents = new State<Context<CorpusService>>() {
		boolean notfound = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = 4631970750286096979L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			WXnews news = new WXnews();
			news.setTitle("代购代理页面");
			news.setDescription("点击进入代购页面");
			String filename = "daigou_" + openid() + ".html";
			String rootPath = System.getProperty("path.html");
			File file = new File(rootPath + filename);
			String agentpageKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Daigou", "Agentpage");
			String agentpage = u.content().account().get(agentpageKey);
			if(!file.exists()) {
				notfound = true;
				logger.info("[DaigouContext] agent doesn't have a daigou page, use default");
				agentpage = "daigou.html";
			}
			
			news.setUrl("http://SuppressWarnings.com/" + agentpage);
			news.setPicUrl("http://SuppressWarnings.com/ads.png");
			String json = gson.toJson(news);
			u.output("news://" + json);
			if(!notfound) {
				u.content().sendTxtTo("通知代理", "你具有代购代理权限，这是你的专属代购页面", openid());
			} else {
				u.content().sendTxtTo("通知代理", "你具有代购代理权限，但是你还没有自己的专属代购页面", openid());
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "代理入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	

	State<Context<CorpusService>> others = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4631970750286096979L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			WXnews news = new WXnews();
			news.setTitle("代购页面");
			news.setDescription("点击进入代购页面");
			news.setUrl("http://SuppressWarnings.com/daigou.html");
			news.setPicUrl("http://SuppressWarnings.com/ads.png");
			String json = gson.toJson(news);
			u.output("news://" + json);
			u.content().sendTxtTo("通知用户", "欢迎进入代购页面，请放心选购！", openid());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "用户入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public DaigouContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = daigou;
	}

}
