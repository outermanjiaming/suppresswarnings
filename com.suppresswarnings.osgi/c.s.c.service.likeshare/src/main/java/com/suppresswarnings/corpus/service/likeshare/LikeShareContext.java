package com.suppresswarnings.corpus.service.likeshare;

import java.util.Random;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
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
			logger.info("like share input: " + t);
			if(t.startsWith("SCAN_")) {
				if(t.startsWith("SCAN_P_Like_Game")) {
					return likegame;
				}
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
	
	State<Context<CorpusService>> likegame = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6040006707683444576L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			synchronized (likegame) {
				String done = String.join(Const.delimiter, Const.Version.V2, "Game", "Like", openid());
				String exist = u.content().account().get(done);
				if(exist == null) {
					u.content().account().put(done, time());
					u.content().data().put(String.join(Const.delimiter, Const.Version.V2, "Game", "Like", openid(), time()), openid());
					u.content().data().put(String.join(Const.delimiter, Const.Version.V2, openid(), "Game", "Like", time()), openid());
					
					double score = new Random().nextDouble();
					if(score < 0.6) {
						u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "Project", "Money", "Like", time()), "3");
						u.output("恭喜你，抽中三等奖，可以直接输入「我要提现」申请提现哦，秒到账！");
					} else if(score > 0.9) {
						u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "Project", "Money", "Like", time()), "100");
						u.output("恭喜你，抽中一等奖，可以直接输入「我要提现」申请提现哦，秒到账！");
					} else {
						u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "Project", "Money", "Like", time()), "10");
						u.output("恭喜你，抽中二等奖，可以直接输入「我要提现」申请提现哦，秒到账！");
					}
				} else {
					u.content().account().put(String.join(Const.delimiter, Const.Version.V2, "Inform", "Game", "Like", time(), random()), openid());
					u.output("你已经参与抽奖活动了，奖金已经在你的账户，你可以输入「我要提现」申请提现哦，秒到账！");
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "点赞抽奖活动";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	public LikeShareContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = likeshare;
	}

}
