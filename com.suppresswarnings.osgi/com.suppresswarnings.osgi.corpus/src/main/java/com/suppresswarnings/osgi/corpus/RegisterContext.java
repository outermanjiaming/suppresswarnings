package com.suppresswarnings.osgi.corpus;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.data.Const;
import com.suppresswarnings.osgi.user.KEY;

public class RegisterContext extends WXContext {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	int type;
	String name;
	String email;
	String inviteCode;
	String code = null;
	public static final String l0 = "恭喜恭喜，您的邀请码有效！", u0 = "恭喜恭喜，您的邮箱验证通过了！", f0 = "注册完成", e0 = "验证码已经发送至您的邮箱，请查收并输入验证码：", q0 = "注册成为不同类型的用户：1.游客，2.用户（邮箱），3.领导（邀请码），请回复数字：", q1 = "请问怎么称呼您？", q1f = "名称不合格，请输入正确的名字：", q2="请输入您的邮箱：", q2f = "邮箱错误，请输入正确的邮箱：", q3 = "请输入邀请码：", q3f = "邀请码错误，请输入正确的邀请码：", q4 = "", q5 = "";
	final State<Context<WXService>> p0, p1, p1try, visitor, user, leader, emailCode, p2, p2try, p3, p3try;
	public RegisterContext(String openid, WXService ctx) {
		super(openid, ctx);
		visitor = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3395785178764716725L;

			@Override
			public void accept(String t, Context<WXService> u) {
				name = t;
				String key = String.join(Const.delimiter, Version.V1, openid(), KEY.Name.name());
				u.content().saveToAccount(key, name);
				output(f0);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				return init;
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return true;
			}};
		user = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4356193396265517401L;

			@Override
			public void accept(String t, Context<WXService> u) {
				String key = String.join(Const.delimiter, Version.V1, openid(), "Email");
				u.content().saveToAccount(key, email);
				output(u0 + q1);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				return p1.apply(t, u);
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return false;
			}};
		leader = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5283486014726414025L;

			@Override
			public void accept(String t, Context<WXService> u) {
				String key = String.join(Const.delimiter, Version.V1, openid(), "Leader");
				u.content().saveToAccount(key, time());
				output(l0 + q2);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				return p2.apply(t, u);
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return false;
			}};
		p0 = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2774499166678852821L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output(q0);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				type = Integer.valueOf(t);
				if("1".equals(t)) return p1;
				if("2".equals(t)) return p2;
				if("3".equals(t)) return p3;
				return this;
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		p1 = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 789106111555582891L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output(q1);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				RequireChain checkLength = new RequireLength(1, 32);
				if(checkLength.agree(t)) {
					return visitor;
				}
				return p1try;
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		p1try = tryAgain(3, q1f, p1, fail);
		emailCode = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2700676692412417289L;

			@Override
			public void accept(String t, Context<WXService> u) {
				email = t;
				code = content().kaptcha(email);
				output(e0);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if(code.equals(t)) {
					return user;
				}
				return fail;
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		p2 = new State<Context<WXService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3678629729364336623L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output(q2);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				//regular process: if not ok, go to fix it, else move to next state
				RequireChain checkmail = new RequireEmail();
				if(checkmail.agree(t)) {
					return emailCode;
				} else {
					return p2try;
				}
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		p2try = tryAgain(3, q2f, p2, fail);
		p3 = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2700676692412417289L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output(q3);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				//check invite code, if used or not exist, prompt it. else register it.
				log("[lijiaming]邀请码->openid：" + t + " -> " + openid());
				String result = content().registerLeader(openid(), t, u);
				if(WXService.SUCCESS.equals(result)){
					return leader;
				}
				return p3try;
			}

			@Override
			public String name() {
				return null;
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		p3try = tryAgain(3, q3f, p3, fail);
		init(p0);
	}

	@Override
	public void log(String msg) {
		logger.info("[Register] " + msg);
	}
	
}
