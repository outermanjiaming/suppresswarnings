/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.corpus.accost;

import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;

public class AccostContext extends WXContext {
	State<Context<CorpusService>> my = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -573575076883181758L;
		int times = 3;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output(times + ": 闲聊个啥？");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(times -- <= 0) {
				times = 3;
				return init;
			}
			return this;
		}

		@Override
		public String name() {
			return "accost";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public AccostContext(String openid, CorpusService ctx) {
		super(openid, ctx);
		this.state = my;
	}
}
