package com.suppresswarnings.osgi.corpus.register;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.corpus.WXContext;
import com.suppresswarnings.osgi.corpus.WXService;

public class RegisterContext extends WXContext {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	int type;
	String email;
	String inviteCode;
	public RegisterContext(String openid, WXService ctx) {
		super(openid, ctx);
	}

	@Override
	public void log(String msg) {
		logger.info("[Register] " + msg);
	}
	
	State<Context<WXService>> R0 = new State<Context<WXService>>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -42020084619924710L;
		@Override
		public void accept(String t, Context<WXService> u) {
			u.output("请输入邀请码：");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return R1;
		}

		@Override
		public String name() {
			return "注册";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<WXService>> R1 = new State<Context<WXService>>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -5001268758009417564L;
		int tried = 3;
		String invite = null;
		@Override
		public void accept(String t, Context<WXService> u) {
			String result = u.content().register(openid(), t);
			if("success".equals(result)) {
				invite = t;
				u.output("邀请码有效。\n请问怎么称呼您？");
			} else {
				u.output("邀请码无效(还可以重试" + tried + "次)");
			}
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(invite == null) {
				if(tried <= 1) {
					tried = 3;
					return init;
				}
				tried --;
				return this;
			}
			return R2;
		}

		@Override
		public String name() {
			return "invite";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<WXService>> R2 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 3678629729364336623L;

		@Override
		public void accept(String t, Context<WXService> u) {
			log("just log it: " + t);
			u.output("注册成功！");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return init;
		}

		@Override
		public String name() {
			return "done";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
}
