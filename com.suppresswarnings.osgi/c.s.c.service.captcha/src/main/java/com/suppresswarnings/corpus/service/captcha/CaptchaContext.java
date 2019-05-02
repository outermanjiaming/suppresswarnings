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
	State<Context<CorpusService>> captcha = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4072909633512715215L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "CaptchaList");
			u.content().account().page(key, key, null, 1000, (k, v) ->{
				String number = v;
				String text = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Text"));
				String stamp = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Time"));
				long report = Long.valueOf(stamp);
				if(System.currentTimeMillis() - report > TimeUnit.HOURS.toMillis(1)) {
					u.output("一个小时内没有收到验证码\n手机号：" + number);
					return;
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				u.output("当前时间：" + format.format(new Date()) + "\n更新时间：" + format.format(new Date(report)) +"\n手机号：" + number+ "\n验证码：" + text);
			});
			u.output("如果没有验证码，请确认手机号没错，输入：刷新");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			logger.info("[captcha] input: " + t);
			if(CMD.equals(t) || t.startsWith("SCAN_")) {
				return captcha;
			}
			
			if("刷新".equals(t)) {
				return captcha;
			}
			
			if(t.length() > 4 && Pattern.compile("\\d+").matcher(t.substring(0, 4)).matches()) {
				return update;
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
	
	
	State<Context<CorpusService>> update = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3851369518154268501L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			
			String[] textNumber = t.split("\\s+");
			if(textNumber.length == 1) textNumber = new String[] {t, "13727872757"};
			String text = textNumber[0];
			String number = textNumber[1];
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "CaptchaList", number);
			u.content().account().put(key, number);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Text"), text);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, "Time"), time());
			
			u.content().token().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Captcha", number, time()), text);
			u.output("请按如下格式输入：\n${验证码} [${手机号}]\n比如：544464 13727872757");
			u.output("已记录：" + text + " " + number);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.length() > 4 && Pattern.compile("\\d+").matcher(t.substring(0, 4)).matches()) {
				return update;
			}
			return init;
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
		this.state = captcha;
	}

}
