package com.suppresswarnings.osgi.corpus;

import java.util.regex.Pattern;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.data.Const;

public interface WXState {
	Pattern mailRegex = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
	
	State<Context<WXService>> init = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6996218717545604237L;

		@Override
		public void accept(String t, Context<WXService> u) {
			u.println("请输入'登录'或者'出试题'：");
			u.content().dataService.unknown(((WXContext)u).openid(), t);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			String openid = ((WXContext)u).openid();
			//TODO use map or ner to decide
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
			return this;
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
