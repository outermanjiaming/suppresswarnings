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
				u.content().account().put(String.join(Const.delimiter, Const.Version.V2, "Cashout", "Request", hourly, time(), openid()), openid());
				u.content().account().put(String.join(Const.delimiter, Const.Version.V2, openid(), "Cashout", "Request", hourly), time());
				u.output("恭喜你，提现请求已经发出，财务部门正在审核，预计24小时内审核到账");
			} else {
				//is doing
				u.output("你的提现请求正在审核，预计24小时内审核到账");
			}
			
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				return cashout;
			}
			if(t.startsWith("SCAN_")) {
				return cashout;
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

	public CashoutContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = cashout;
	}

}
