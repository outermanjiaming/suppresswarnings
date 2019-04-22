package c.s.c.service.cashout;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class CashoutContextFactory implements ContextFactory<CorpusService> {

	@Override
	public String command() {
		return CashoutContext.CMD;
	}

	@Override
	public String description() {
		return "申请我要提现";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(2);
	}

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new CashoutContext(wxid, openid, content);
	}

}
