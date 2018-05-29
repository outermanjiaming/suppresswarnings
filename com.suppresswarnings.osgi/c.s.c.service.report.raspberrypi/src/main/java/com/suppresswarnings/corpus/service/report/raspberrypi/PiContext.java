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
				u.output("没有查到上报数据");
				return;
			}
			long report = Long.valueOf(stamp);
			if(System.currentTimeMillis() - report > TimeUnit.HOURS.toMillis(1)) {
				u.output("近一小时内没有上报\n需要'刷新'吗？\n你也可以输入'退出'");
				done = false;
				return;
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			u.output("当前时间：" + format.format(new Date()) + "\n上报时间：" + format.format(new Date(report)) + "\n上报数据：" + lastReport);
			return;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
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
	
	public PiContext(String openid, CorpusService ctx) {
		super(openid, ctx);
		this.state = my;
	}

}
