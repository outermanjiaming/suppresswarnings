package com.suppresswarnings.corpus.service.wxpy;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
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
			if(robot.name().equals(t)) return robot;
			if(myid.name().equals(t)) return myid;
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
	State<Context<CorpusService>> myid = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 65547727119148475L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output(openid());
			u.content().atUser(CorpusService.STUPID, String.format("申请领工资：\n%s\n%s", openid(), user().getNickname()));
			u.content().atUser(openid(), "这是你的ID:\n"+openid()+"\n素朴网联会根据这个ID发工资给你\n你收到工资之后可以查看微信零钱\n谢谢你为素朴网联所做的一切贡献\n");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "领工资";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	State<Context<CorpusService>> robot = new State<Context<CorpusService>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 65547727119148475L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("欢迎加入机器人俱乐部" + openid());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return silent;
		}

		@Override
		public String name() {
			return "我是机器人";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> silent = new State<Context<CorpusService>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1777998538915301472L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			logger.info("机器人:"+ openid() +":" + t);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			String command = CheckUtil.cleanStr(t);
			ContextFactory<CorpusService> cf = u.content().factories.get(command);
			if(cf == null) {
				String exchange = u.content().globalCommand(command);
				if(exchange != null) {
					cf = u.content().factories.get(exchange);
				}
			}
			
			if(cf == null) {
				cf = u.content().factories.get(command.toLowerCase());
			}
			logger.info("ContextFactory: " + cf);
			if(cf != null) {
				return init;
			}
			return silent;
		}

		@Override
		public String name() {
			return "不回复";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public WxpyContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(wxpy);
	}

}
