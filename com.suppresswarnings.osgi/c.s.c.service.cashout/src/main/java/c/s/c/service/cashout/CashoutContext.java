package c.s.c.service.cashout;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class CashoutContext extends WXContext {
	public static final String CMD = "我要提现";
	public static final String[] AUTH = {"Cashout"};
	String request;
	String realValue;
	State<Context<CorpusService>> cashout = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 649411267915208874L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String key = String.join(Const.delimiter, Const.Version.V2, openid(), "Requesting", "Cashout");
			String requesting = u.content().account().get(key);
			if(requesting == null || "Done".equals(requesting)) {
				//ok
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
				String hourly = sdf.format(new Date());
				u.content().account().put(key, time());
				request = String.join(Const.delimiter, Const.Version.V2, "Cashout", "Request", hourly, time(), openid());
				u.content().account().put(request, openid());
				u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "Cashout", "Request", hourly), time());
				u.output("恭喜你，提现请求已经发出，财务部门正在审核，预计24小时内审核到账");
			} else {
				//is doing
				u.output("你的提现请求正在审核，预计24小时内审核到账");
			}
			u.output("如果要补充实名信息请输入：" + real.name());
			
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t) || t.startsWith("SCAN_")) {
				return cashout;
			}
			
			if(realValue == null || real.name().equals(t)) {
				return real;
			}
			return init;
		}

		@Override
		public String name() {
			return "提现入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> real = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7051846342563147949L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你还没有实名认证，提现需要实名信息（微信要求），请输入该微信号对应的真实姓名和身份证，比如：张三,430481199801107112");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			realValue = t;
			u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "RealValue", time()), t);
			u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "RealValue"), realValue);
			return cashout;
		}

		@Override
		public String name() {
			return "实名认证";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};

	public CashoutContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = cashout;
		this.realValue = ctx.account().get(String.join(Const.delimiter, Const.Version.V2, openid(), "RealValue"));
	}

}
