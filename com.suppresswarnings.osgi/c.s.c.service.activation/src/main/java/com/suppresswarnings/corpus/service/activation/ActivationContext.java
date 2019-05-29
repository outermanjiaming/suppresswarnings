package com.suppresswarnings.corpus.service.activation;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.wx.WXnews;

public class ActivationContext extends WXContext {
	String code = "P_Pay_Software_1551101705933_317";
	public static final String CMD = "我要激活码";
	State<Context<CorpusService>> activation = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -732329740211218857L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String key = String.join(Const.delimiter, Const.Version.V1, "Info", "Activate", "Software", openid());
			String exist = u.content().account().get(key);
			logger.info("[activation] exist: " + exist);
			if(exist == null || "None".equals(exist)) {
				String activationCode = u.content().generateActivateCode(openid());
				u.output(activationCode);
			} else {
				WXnews news = new WXnews();
				news.setTitle("购买激活码");
				news.setDescription("点击进入支付页面，支付完成之后可以得到激活码！");
				news.setUrl("http://suppresswarnings.com/payment.html?state=" + code);
				Gson gson = new Gson();
				String json = gson.toJson(news);
				u.output("news://" + json);
			}
			
			String activationCode = u.content().generateActivateCode(openid());
			u.content().atUser(openid(), "本次试用免费获得激活码，下次觉得好用请支付购买。" + activationCode);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.startsWith("SCAN_")) {
				code = t.substring("SCAN_".length());
				return activation;
			}
			
			if(CMD.equals(t)) {
				return activation;
			}
			return init;
		}

		@Override
		public String name() {
			return CMD;
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public ActivationContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(activation);
	}

}
