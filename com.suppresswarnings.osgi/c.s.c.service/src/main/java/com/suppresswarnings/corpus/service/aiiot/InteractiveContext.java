package com.suppresswarnings.corpus.service.aiiot;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class InteractiveContext extends WXContext {
	boolean first = true;
	String cmd;
	String code;
	String info;
	
	State<Context<CorpusService>> interactive = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8334335946427068106L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(first) {
				first = false;
				u.output("你进入了交互式命令控制物联网，你接下来的输入直接发送给：" + info);
			}
			String ret = u.content().aiiot.remoteCall(wxid(), openid(), code, cmd, t, u);
			u.output(ret);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			
			return interactive;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public InteractiveContext(String wxid, String openid, CorpusService ctx, String cmd, String code, String info) {
		super(wxid, openid, ctx);
		this.cmd = cmd;
		this.code = code;
		this.info = info;
		this.state = interactive;
	}

}
