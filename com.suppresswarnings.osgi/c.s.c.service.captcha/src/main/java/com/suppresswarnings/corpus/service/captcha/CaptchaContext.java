package com.suppresswarnings.corpus.service.captcha;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class CaptchaContext extends WXContext {
	public static final String[] AUTH = {"VIP"};
	public static final String CMD = "我要验证码";
	String number = null;
	State<Context<CorpusService>> captcha = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4072909633512715215L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			number = t;
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "CaptchaList", number);
			String exist = u.content().account().get(key);
			if(exist == null) {
				u.output("你输入的手机号不存在，请确认：" + number);
				return;
			}
			String text = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Text"));
			String stamp = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Time"));
			long report = Long.valueOf(stamp);
			if(System.currentTimeMillis() - report > TimeUnit.MINUTES.toMillis(10)) {
				u.output("10分钟内没有收到验证码\n手机号：" + number);
				return;
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			u.output("当前时间：" + format.format(new Date()) + "\n更新时间：" + format.format(new Date(report)) +"\n手机号：" + number+ "\n验证码：" + text);
			u.output("如果已过期请再次输入手机号：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[captcha] input: " + t);
			if(t.length() > 4 && Pattern.compile("\\d+").matcher(t.substring(0, 4)).matches()) {
				return captcha;
			}
			
			return init;
		}

		@Override
		public String name() {
			return "验证码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3851369518154268501L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("「素朴网联」为用户提供免费共享的VIP会员，请问你要登录哪个账号？请输入手机号：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t) || t.startsWith("SCAN_")) {
				return enter;
			}
			return captcha;
		}

		@Override
		public String name() {
			return "更新验证码";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	

	public CaptchaContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = enter;
	}

}
