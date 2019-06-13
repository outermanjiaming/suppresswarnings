package com.suppresswarnings.corpus.service.activation;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

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
			if(!t.startsWith("SCAN_")) {
				u.output("请扫码获取激活码，谢谢关注「素朴网联」");
			} else {
				String code = t.substring("SCAN_".length());
				String openidKey = String.join(Const.delimiter, Const.Version.V1, "Openid", "Activate", "Scene", code);
				String owner = u.content().account().get(openidKey);
				if(!u.content().isNull(owner)) {
					u.content().atUser(owner, user().getNickname() + "从你的二维码获取了激活码");
					u.content().uniqueKey("获取激活码:" + openid());
					u.content().uniqueKey("提供激活码:" + owner);
				}
				
				boolean ok = false;
				if(exist == null || "None".equals(exist)) {
					ok = true;
				} else {
					String identity = u.content().account().get(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Bind", exist));
					String expireKey = String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Expire", identity);
					String expireAt = u.content().account().get(expireKey);
					try {
						long expire = Long.parseLong(expireAt);
						if(expire > System.currentTimeMillis()) {
							ok = false;
						} else {
							ok = true;
						}
					} catch (Exception e) {
						ok = true;
					}
				}
				
				if(ok) {
					String activationCode = u.content().generateActivateCode(openid());
					u.output(activationCode);
				} else {
					u.output("激活码有效期为24小时，失效之后才能继续购买");
				}
			}
			
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
