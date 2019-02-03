/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;

public class ChatContext extends WXContext {
	String userid;

	State<Context<CorpusService>> connect = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -64814572511986326L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.content().sendTxtTo("chat context", t, userid);
			ChatContext context = new ChatContext(wxid, userid, openid, u.content());
			u.content().contextx(userid, context , TimeUnit.MINUTES.toMillis(5));
			u.output("已经建立对话连接，对方收到了你的回答！");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[ChatContext] connect -> chat");
			return chat;
		}

		@Override
		public String name() {
			return "对话状态";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> chat = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8275105486954890417L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.content().sendTxtTo("chat context", t, userid);
			u.output("已经建立对话连接，对方收到了你的回答！");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[ChatContext] chat -> chat");
			return chat;
		}

		@Override
		public String name() {
			return "对话状态";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public ChatContext(String wxid, String openid, String userid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.userid = userid;
		this.state = connect;
	}
	

	@Override
	public State<Context<CorpusService>> exit() {
		return init;
	}

}
