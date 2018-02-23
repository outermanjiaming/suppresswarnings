package com.suppresswarnings.osgi.corpus.register;

import java.text.DecimalFormat;
import java.util.Random;

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
	Random rand = new Random();
	DecimalFormat format = new DecimalFormat("0000");
	String code = null;
	State<Context<WXService>> p0, p1, p2, p3, finish;
	public RegisterContext(String openid, WXService ctx) {
		super(openid, ctx);
		
		p0 = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2774499166678852821L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output("注册成为不同类型的用户：1.游客，2.用户（邮箱），3.领导（邀请码），请回复数字：");
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
				u.output("请问怎么称呼您？");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				log("[lijiaming]需要记录该用户的名字-openid" + openid() + "-" + t);
				//regular process: if not ok, go to fix it, else move to next state
				return finish;
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
				u.output("请输入您的邮箱：");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				//regular process: if not ok, go to fix it, else move to next state
				code = format.format(rand.nextInt(10000));
				log("[lijiaming]发送验证码到邮箱，下一步对比验证码。" + code + " -> " + t);
				return finish;
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
		
		p3 = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2700676692412417289L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output("请输入邀请码：");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				//check invite code, if used or not exist, prompt it. else register it.
				log("[lijiaming]邀请码->openid：" + t + " -> " + openid());
				return finish;
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
		
		finish = new State<Context<WXService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6720551759514120192L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output("注册完成，您的类型是：" + type);
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
				return false;
			}
			
		};
		init(p0);
	}

	@Override
	public void log(String msg) {
		logger.info("[Register] " + msg);
	}
	
	
	
}
