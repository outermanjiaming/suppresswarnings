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

import java.util.Set;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.wx.WXuser;


public class WXContext extends Context<CorpusService> {
	public static final String exit = "exit()";
	String openid;
	String wxid;
	WXuser user;
	public final State<Context<CorpusService>> reject = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1154267650726164000L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("暂时无权查看，请联系管理员。（本次对话结束）");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "无权查看";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	public final State<Context<CorpusService>> init = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4836433217450201449L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			Set<String> commands = u.content().factories.keySet();
			if(commands.size() < 1) {
				u.output("稍等，我现在还没有准备好！");
			} else {
				u.appendLine("我目前处于内测开发阶段。请关注 http://SuppressWarnings.com/");
			}
			if(exit(t, "exit()")) {
				u.appendLine("上一阶段对话已经结束。");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			String command = CheckUtil.cleanStr(t.trim());
			ContextFactory<CorpusService> cf = u.content().factories.get(command);
			if(cf == null) {
				String exchange = u.content().globalCommand(command);
				if(exchange != null) {
					cf = u.content().factories.get(exchange);
				}
			}
			
			if(cf != null) {
				Context<CorpusService> context = cf.getInstance(wxid(), openid(), u.content());
				if(cf.ttl() != ContextFactory.forever) {
					u.content().contextx(openid(), context, cf.ttl());
				}
				return context.state();
			}
		
			return this;
		}

		@Override
		public String name() {
			return "微信上下文初始化状态";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public State<Context<CorpusService>> set(State<Context<CorpusService>> start) {
		return new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2352514490121930101L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				start.accept(t, u);
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return start.apply(t, u);
			}

			@Override
			public String name() {
				return "set: " + start.name();
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
	}
	public WXContext(String wxid, String openid, CorpusService ctx) {
		super(ctx);
		this.wxid = wxid;
		this.openid = openid;
		this.state = init;
	}
	public WXuser user() {
		if(user == null) user = content().getWXuserByOpenId(openid());
		return user;
	}
	public String openid(){
		return openid;
	}
	public String wxid() {
		return wxid;
	}
	
	@Override
	public State<Context<CorpusService>> exit() {
		return init;
	}
}