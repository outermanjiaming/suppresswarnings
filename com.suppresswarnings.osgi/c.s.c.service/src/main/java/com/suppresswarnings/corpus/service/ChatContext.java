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

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;

public class ChatContext extends WXContext {
	String userid;
	ChatContext chatContext;

	public void connect(Context<?> ctx) {
		this.chatContext = (ChatContext) ctx;
	}
	
	State<Context<CorpusService>> connect = new State<Context<CorpusService>>() {
		boolean connected = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = -64814572511986326L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(!connected) {
				connected = true;
				logger.info("[chat] atUser " + userid);
				u.content().atUser(userid, t);
				String key = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Chat", "OpenId", openid, "UserId", userid, time(),random());
				u.content().data().put(key, t);
				Context<?> exist = u.content().context(userid);
				if(exist != null && exist instanceof ChatContext) {
					ChatContext that = (ChatContext) exist;
					that.said(t);
					logger.info("[ChatContext] bugfix: don't replace context");
					return;
				}
				ChatContext context = new ChatContext(wxid, userid, openid, u.content());
				u.content().contextx(userid, context , TimeUnit.MINUTES.toMillis(5));
				u.output("已建立对话连接，对方收到了你的消息！");
				
				context.connect(u);
				connect(context);
				chatContext.said(t);
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(!connected) {
				logger.info("[ChatContext] connect -> connect");
				return connect;
			}
			logger.info("[ChatContext] connect -> chat");
			return chat;
		}

		@Override
		public String name() {
			return "连接状态";
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
			u.content().atUser(userid, t);
			String key = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Chat", "OpenId", openid, "UserId", userid, time(),random());
			u.content().data().put(key, t);
			chatContext.said(t);
			
			String exist = u.content().data().get(said());
			if(exist == null) {
				logger.info("[chat context] new reply words: \n" + said() + " \n=> " + t);
				u.content().data().put(said(), t);
			} else {
				logger.info("[chat context] old reply words: \n" + said() + " \n=> " + exist + " \n== " + t);
				u.content().data().put(said(), String.join(Const.delimiter, t, "OpenId", openid(), time(), random()));
			}
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
	
	String said;
	private void said(String t) {
		this.said = t;
	}
	public String said() {
		if(said == null) return "null";
		return said;
	}

	@Override
	public State<Context<CorpusService>> exit() {
		super.exit();
		content().atUser(userid, "断开连接了，下次再聊");
		content().contexts.remove(userid);
		return init;
	}

}
