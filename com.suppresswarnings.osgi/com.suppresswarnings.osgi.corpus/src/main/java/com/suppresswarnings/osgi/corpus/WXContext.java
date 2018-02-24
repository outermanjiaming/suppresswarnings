package com.suppresswarnings.osgi.corpus;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.data.Const;

public class WXContext extends Context<WXService> {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	String openid;
	
	public WXContext(String openid, WXService ctx) {
		super(ctx);
		this.openid = openid;
	}

	public String openid(){
		return openid;
	}

	@Override
	public void log(String msg) {
		logger.info("[WXContext] " + msg);
	}
	public State<Context<WXService>> tryAgain(int times, String prompt, State<Context<WXService>> from, State<Context<WXService>> to) {
		return new State<Context<WXService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5956897883051354629L;
			int tried = times;
			
			@Override
			public void accept(String t, Context<WXService> u) {
				if(tried < 1) {
					to.accept(t, u);
				} else { 
					output("(剩余"+tried+"次)"+prompt);
				}
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if(tried < 1) {
					tried = times;
					return to.apply(t, u);
				}
				tried --;
				return from.apply(t, u);
			}

			@Override
			public String name() {
				return "重试";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
	}
	
	public State<Context<WXService>> fail = new State<Context<WXService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5869050323055999637L;

		@Override
		public void accept(String t, Context<WXService> u) {
			output("操作失败，请稍后再来一次！");
			//TODO log this openid and why
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return init.apply(t, u);
		}

		@Override
		public String name() {
			return "fail";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public State<Context<WXService>> init = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6996218717545604237L;

		@Override
		public void accept(String t, Context<WXService> u) {
			u.output("新年快乐，狗年旺旺！");
			u.content().dataService.unknown(((WXContext)u).openid(), t);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			String openid = ((WXContext)u).openid();
			
			if("登录".equals(t)) {
				LoginContext ctx = new LoginContext(openid, u.content());
				ctx.init(ctx.S0);
				u.content().contextx(openid, ctx, Const.InteractionTTL.userReply);
				return ctx.S0;
			}
			if("出试题".equals(t)) {
				SetThePaper ctx = new SetThePaper(openid, u.content());
				ctx.init(ctx.P0);
				u.content().contextx(openid, ctx, Const.InteractionTTL.setThePaper);
				return ctx.P0;
			}
			//TODO use map or ner to decide
			WXService content = u.content();
			ContextFactory factory = content.command(t);
			if(factory != null) {
				Context<WXService> context = factory.getInstance(openid, content);
				if(factory.ttl() != ContextFactory.forever) {
					content.contextx(openid, context, factory.ttl());
				}
				return context.state();
			}
			
			return init;
		}

		@Override
		public String name() {
			return "init";
		}

		@Override
		public boolean finish() {
			return false;
		}};
}
