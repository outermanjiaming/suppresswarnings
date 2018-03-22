package com.suppresswarnings.osgi.corpus;

import java.util.function.BiConsumer;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.alone.TimeUtil;
import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.data.Const;
import com.suppresswarnings.osgi.user.KEY;

public class WXContext extends Context<WXService> {
	public static final String exit = "exit()";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	String openid;
	
	public WXContext(String openid, WXService ctx) {
		super(ctx);
		this.openid = openid;
	}

	public String openid(){
		return openid;
	}
	public boolean yes(String input, String expect) {
		if(expect == input) return true;
		if(input == null) return false;
		if(expect != null && expect.equals(input)) return true;
		//TODO ner check yes
		String yes = input.trim() + " ";
		if("好 好的 可以 嗯 是 是的 没错 当然 好啊 是啊 可以的 对 确定 确认 ".contains(yes)) return true;
		return false;
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
					u.output("(剩余"+tried+"次)"+prompt);
				}
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if(tried < 1 || exit.equals(t)) {
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
	
	public State<Context<WXService>> tryAgain(int times, BiConsumer<String, Context<WXService>> accept, State<Context<WXService>> from, State<Context<WXService>> to) {
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
					accept.accept(t, u);
				}
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if(tried < 1 || exit.equals(t)) {
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
			u.output("操作失败，请稍后再来一次！");
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
		boolean inited = false;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6996218717545604237L;

		@Override
		public void accept(String t, Context<WXService> u) {
			if(!inited) {
				inited = true;
				String name = u.content().getFromAccount(String.join(Const.delimiter, Version.V1, openid(), KEY.Name.name()));
				String keyLast = String.join(Const.delimiter, Version.V1, openid(), "Last");
				String last = u.content().getFromAccount(keyLast);
				if(last != null) {
					String keyLasts = String.join(Const.delimiter, Version.V1, openid(), "Last", ""+TimeUtil.getNowLeft());
					u.content().saveToAccount(keyLasts, last);
				}
				u.content().saveToAccount(keyLast, TimeUtil.getTimeString(System.currentTimeMillis()));
				if(name != null) {
					u.output(name + "你好。");
				} else {
					u.output("你还没有设置名字，可以输入'我要注册'试试。");
				}
			} else {
				u.output("会采用神经网络和CRF算法来决定回复什么内容");
				u.content().dataService.unknown(((WXContext)u).openid(), t);
			}
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			String openid = ((WXContext)u).openid();
			WXService content = u.content();
			
			if("登录".equals(t)) {
				LoginContext ctx = new LoginContext(openid, content);
				ctx.init(ctx.S0);
				u.content().contextx(openid, ctx, Const.InteractionTTL.userReply);
				return ctx.S0;
			}
			if("出试题".equals(t)) {
				SetThePaper ctx = new SetThePaper(openid, content);
				ctx.init(ctx.P0);
				u.content().contextx(openid, ctx, Const.InteractionTTL.setThePaper);
				return ctx.P0;
			}
			//TODO use map or ner to decide
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
		}
	};
}
