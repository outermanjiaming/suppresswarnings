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
	public static final String CMD = "我要验证码";
	String numbers = null;
	String number = null;

	State<Context<CorpusService>> captcha = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8860395301948178002L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			number = t;
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "CaptchaList", number);
			String exist = u.content().account().get(key);
			if(exist == null) {
				u.output("你输入的手机号不存在，请确认：" + number);
				return;
			} else {
				String text = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Text"));
				String stamp = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Time"));
				long report = Long.valueOf(stamp);
				if(System.currentTimeMillis() - report > TimeUnit.MINUTES.toMillis(10)) {
					u.output("10分钟内没有收到验证码\n请确认你使用该账号登录了爱奇艺会员，手机号：" + number);
					return;
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				u.output("当前时间：" + format.format(new Date()) + "\n更新时间：" + format.format(new Date(report)) +"\n手机号：" + number+ "\n验证码：");
				u.content().atUser(openid(), text);
			}
			
			u.output("如果未收到验证码或者验证码已过期，请再次输入手机号：");
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
			return "输出验证码";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3851369518154268501L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "CaptchaList");
			StringBuffer sb = new StringBuffer();
			u.content().account().page(key, key, null, 100, (k, v) ->{
				String phone = k.substring(key.length() + Const.delimiter.length());
				sb.append(phone).append(";");
			});
			u.output("「素朴网联」提供免费共享的爱奇艺会员，账号："+sb.toString()+"，选择一个账号登录爱奇艺会员获取验证码，然后");
			u.output("输入手机号：");
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
