package com.suppresswarnings.osgi.corpus.pi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.corpus.WXContext;
import com.suppresswarnings.osgi.corpus.WXService;

public class PiContext extends WXContext {
	State<Context<WXService>> pi0, tryAgain, query, refresh, generate, done;
	public PiContext(String openid, WXService ctx) {
		super(openid, ctx);
		pi0 = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 6698135440849825751L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output("输入'查询'则显示你的树莓派最近上报的数据\n输入'生成'则显示上报所需的token\n输入'exit()'退出（通用命令）");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if("查询".equals(t)) return query;
				if("生成".equals(t)) return generate;
				if("exit()".equals(t)) return init;
				return tryAgain;
			}

			@Override
			public String name() {
				return "树莓派";
			}

			@Override
			public boolean finish() {
				return false;
			}};
		tryAgain = tryAgain(3, "输入不正确，换个命令重试：", pi0, fail);
		query = new State<Context<WXService>>(){
			long lasttime = 0;
			int times = 3;
			/**
			 * 
			 */
			private static final long serialVersionUID = 6698135440849825751L;

			@Override
			public void accept(String t, Context<WXService> u) {
				if(times < 1) {
					u.output("不能查询太多次，请稍后再输入'我的树莓派'。");
					return;
				}
				
				long gap = System.currentTimeMillis() - lasttime;
				if(gap < TimeUnit.MINUTES.toMillis(2)) {
					u.output("不能太频繁，请在2分钟之后再试试。");
					return;
				}
				
				String key = "001.RaspberryPi." + openid();
				String token = u.content().getFromToken(key);
				if(token == null) {
					u.output("你还没有生成token，输入'生成'");
					return;
				}
				String last = "001.Token.RaspberryPi." + token;
				String stamp = u.content().getFromToken(last);
				if(stamp == null) {
					u.output("你的树莓派还没有上报IP，token: " + token);
					return;
				}
				String ipconfig = "001.IP.RaspberryPi." + token + "." + stamp;
				String value = u.content().getFromToken(ipconfig);
				if(value == null) {
					u.output("你的树莓派上报的数据为null, stamp: " + stamp);
					return;
				}
				long report = Long.valueOf(stamp);
				if(System.currentTimeMillis() - report > TimeUnit.HOURS.toMillis(6)) {
					u.output("你的树莓派最近没有上报，可以输入'刷新'");
					return;
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				u.output("这是你的树莓派在" + format.format(new Date(report)) + "上报的数据：\n" + value );
				lasttime = System.currentTimeMillis();
				return;
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if("生成".equals(t)) return generate;
				if("刷新".equals(t)) return refresh;
				if("exit()".equals(t)) return init;
				return done;
			}

			@Override
			public String name() {
				return "查询树莓派IP";
			}

			@Override
			public boolean finish() {
				return false;
			}};
		refresh = tryAgain(1, query, query, init);
		generate = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -8505606877718539224L;

			@Override
			public void accept(String t, Context<WXService> u) {
				String map = "001.RaspberryPi." + openid();
				String exist = u.content().getFromToken(map);
				if(exist != null) {
					u.output("你的token已经存在: " + exist);
					return;
				}
				String token = random() + openid().substring(5);
				String limit = "001.Limit.Token.RaspberryPi." + token;
				String uid   = "001.UID." + token;
				
				u.content().saveToToken(limit, "10");
				u.content().saveToToken(uid, openid());
				u.content().saveToToken(map, token);
				u.output("（请根据攻略使用）这是你的token：" + token);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if("exit()".equals(t)) return init;
				return done;
			}

			@Override
			public String name() {
				return "生成token";
			}

			@Override
			public boolean finish() {
				return false;
			}};
		done = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 4636839550418143558L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output("请根据http://SuppressWarnings.com/walkthrough/raspberrypi.html查看使用攻略。");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				return init;
			}

			@Override
			public String name() {
				return "使用说明";
			}

			@Override
			public boolean finish() {
				return true;
			}};
		init(pi0);
	}

}
