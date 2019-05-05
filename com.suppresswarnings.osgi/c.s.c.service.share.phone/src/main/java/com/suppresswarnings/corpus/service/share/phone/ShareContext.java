package com.suppresswarnings.corpus.service.share.phone;

import java.util.regex.Pattern;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.aiiot.Things;

public class ShareContext extends WXContext {
	public static final String[] AUTH = {"VIP"};
	public static final String CMD = "我要手机号";
	String code = "P_Things_Texts_201905041230";
	String numbers = null;
	String number = null;
	State<Context<CorpusService>> captcha = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4072909633512715215L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			number = t;
			
			if(numbers.contains(number)) {
				String capt = call(u.content(), "获取第三方手机短信验证码", number);
				u.output(capt.replace(";", "\n"));
			} else {
				u.output("你输入的手机号不存在，请确认：" + number);
				return;
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
			String ret = call(u.content(), "获取第三方手机号码", "获取第三方手机号码");
			u.output("「素朴网联」使用须知\n" + 
					"在使用前你应该知晓下面的电话号码短信内容所有人都可以查看\n" + 
					"请不要用这个电话号码接收重要内容\n" + 
					"下面的电话号码只用于注册一些网站，防止被骚扰\n" + 
					"他人可以通过此电话号码找回密码，所以注册时应注意个人信息\n" + 
					"由此造成经济损失概不负责\n" + 
					"为了让大家共享，请勿修改密码\n" + 
					"在使用时即代表以上条款已同意");
			u.output("免费共享的手机号："+ret+"请输入手机号：");
			numbers = ret;
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
	
	public String call(CorpusService service, String cmd, String input) {
		Things thing = service.aiiot.things.get(code);
		StringBuffer ret = new StringBuffer();
		logger.info("[Captcha] remoteCall "+ thing);
		if(thing == null) {
			logger.info("[Captcha] remoteCall but thing is null for code: " + code);
			ret.append("手机号暂时不可用");
		}
		if(thing.isClosed()) {
			logger.info("[Captcha] thing is closed: " + thing.toString());
			ret.append("手机号暂时不好用");
		}
		logger.info("[Captcha] remoteCall("+openid()+", "+thing+", "+cmd + "" + input+")");
		String result = thing.execute(cmd, input);
		if(result != null) ret.append(result);
		return ret.toString();
	}

	public ShareContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(enter);
	}

}
