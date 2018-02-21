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
		int tried = 3;
		@Override
		public void accept(String t, Context<WXService> u) {
			u.output("测试阶段，重复进入，剩余（" + tried + "）次");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(tried <= 0) {
				tried = 3;
				return init;
			}
			tried --;
			return this;
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
}
