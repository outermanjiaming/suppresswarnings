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
				u.output("你是第一次，为你生成token: " + token);
				u.content().token().put(existKey, token);
				String firstKey = String.join(Const.delimiter, Const.Version.V1, "Report", "Token", token);
				u.content().token().put(firstKey, "create");
				u.appendLine("如何使用token请到http://suppresswarnings.com查看攻略");
				return;
			}
			
			String lastKey = String.join(Const.delimiter, Const.Version.V1, "Report", "Token", token);
			String stamp = u.content().token().get(lastKey);
			String lastReport = String.join(Const.delimiter, Const.Version.V1, "Report", "Msg", token, stamp);
			if(lastReport == null) {
				u.output("没有查到上报数据");
				return;
			}
			long report = Long.valueOf(stamp);
			if(System.currentTimeMillis() - report > TimeUnit.HOURS.toMillis(6)) {
				u.output("你的树莓派最近没有上报，可以输入 刷新 或 退出");
				done = false;
				return;
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			u.output("上报时间：" + format.format(new Date(report)) + "\n上报数据：\n" + lastReport);
			return;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(done || exit(t, "退出")) {
				return init;
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
	
	public PiContext(String openid, CorpusService ctx) {
		super(openid, ctx);
		this.state = my;
	}

}
