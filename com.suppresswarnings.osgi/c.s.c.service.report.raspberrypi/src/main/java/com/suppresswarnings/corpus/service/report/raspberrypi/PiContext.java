/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.report.raspberrypi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class PiContext extends WXContext {
	public static final String CMD = "我的树莓派";
	State<Context<CorpusService>> howto = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -145962847604138147L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("“我的服务端”为了方便开发者查看重要日志，开发者在程序中调用接口，就可以通过公众号收到上报数据。其中“我的暗号”和“我的令牌”每个人都可以在上一个步骤中生成使用。http://suppresswarnings.com/wx.http?action=report&token=我的暗号&openid=我的令牌&msg=上报数据");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return server;
		}

		@Override
		public String name() {
			return "使用说明";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> secret = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -145962847604138147L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("正在更新暗号，旧的暗号立刻失效。");
			String tokenKey  = String.join(Const.delimiter, Const.Version.V2, openid(), "Token");
			String token = u.content().account().get(tokenKey);
			if(u.content().isNull(token)) {
				u.output("第一次生成暗号");
			} else {
				u.output("暗号已经失效：" + token);
				u.content().account().put(String.join(Const.delimiter, Const.Version.V2, "Info", "Server", "Token", token), "None");
				u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "Token", time()), token);
			}
			
			int length = openid().length();
			if(length < 5) {
				u.output("openid数据异常");
				return;
			}
			token = random() + openid().substring(length - 5, length - 1);
			u.content().account().put(tokenKey, token);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V2, "Info", "Server", "Token", token), openid());
			u.output("为你生成新的暗号：");
			u.output(token);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return server;
		}

		@Override
		public String name() {
			return "我的暗号";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> server = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -145962847604138147L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("“我的服务端”功能方便开发者观察重点日志，具体使用方式，请输入：使用说明");
			String reportKey = String.join(Const.delimiter, Const.Version.V2, openid(), "Server", "Report");
			String tokenKey  = String.join(Const.delimiter, Const.Version.V2, openid(), "Token");
			String token = u.content().account().get(tokenKey);
			if(u.content().isNull(token)) {
				u.output("你还没有暗号，请输入：我的暗号");
			} else {
				String report = u.content().token().get(reportKey);
				if(u.content().isNull(report)) {
					u.output("你还没有上报数据，具体使用方式，请输入：使用说明");
				} else {
					u.output("这是最新的上报数据：");
					u.output(report);
				}
				
				u.output("你的暗号：");
				u.output(token);
				u.output("如需更新暗号，请输入：我的暗号");
			}
			u.output("你的令牌：");
			u.output(openid());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(secret.name().equals(t)) return secret;
			if(server.name().equals(t)) return server;
			if(howto.name().equals(t)) return howto;
			return init;
		}

		@Override
		public String name() {
			return "我的服务端";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> my = new State<Context<CorpusService>>() {
		boolean done = false;
		/**
		 * 
		 */
		private static final long serialVersionUID = -145962847604138147L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			done = true;
			String existKey = String.join(Const.delimiter, Const.Version.V1, "Report", "Exist", openid());
			String token = u.content().token().get(existKey);
			if(token == null) {
				int length = openid().length();
				if(length < 5) {
					u.output("openid数据异常");
					return;
				}
				token = random() + openid().substring(length - 5, length - 1);
				
				String firstKey = String.join(Const.delimiter, Const.Version.V1, "Report", "Token", token);
				String first = "" + System.currentTimeMillis();
				u.content().token().put(existKey, token);
				u.content().token().put(firstKey, first);
				
				u.output("你是第一次，为你生成token: " + token + "\n如何使用token请到\nsuppresswarnings.com\n查看攻略");
				return;
			}
			
			String lastKey = String.join(Const.delimiter, Const.Version.V1, "Report", "Token", token);
			String stamp = u.content().token().get(lastKey);
			String lastReportKey = String.join(Const.delimiter, Const.Version.V1, "Report", "Msg", token, stamp);
			String lastReport = u.content().token().get(lastReportKey);
			if(lastReport == null) {
				u.output("没有查到上报数据\n你的token是：" + token);
				return;
			}
			long report = Long.valueOf(stamp);
			if(System.currentTimeMillis() - report > TimeUnit.HOURS.toMillis(1)) {
				u.output("你的token是："+token+"\n近一小时内没有上报\n需要'刷新'吗？\n你也可以输入'退出'");
				done = false;
				return;
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			u.output("当前时间：" + format.format(new Date()) + "\n上报时间：" + format.format(new Date(report)) + "\n上报数据：" + lastReport);
			return;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(secret.name().equals(t)) return secret;
			if(server.name().equals(t)) return server;
			if(yes(t, "刷新")) {
				return my;
			}
			if(done || exit(t, "退出")) {
				return init.apply(t, u);
			}
			return my;
		}

		@Override
		public String name() {
			return "树莓派查询";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	public PiContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = my;
	}

}
