package com.suppresswarnings.osgi.corpus.data;

import java.util.function.BiConsumer;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.corpus.WXContext;
import com.suppresswarnings.osgi.corpus.WXService;
import com.suppresswarnings.osgi.data.Const;

public class QueryContext extends WXContext {
	State<Context<WXService>> start, chain;
	BiConsumer<String, Context<WXService>> save = new BiConsumer<String, Context<WXService>>(){

		@Override
		public void accept(String t, Context<WXService> u) {
			String key = String.join(Const.delimiter, Version.V1, Const.data, "accost", time());
			int ret = u.content().saveToData(key, t);
			update();
			if(ret == 1) {
				u.output("好，接着搭讪我：");
			} else {
				u.output("No，输入exit()退出，或继续搭讪：");
			}
		}
		
	};
	public QueryContext(String openid, WXService ctx) {
		super(openid, ctx);
		start = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 2980305493661554802L;

			@Override
			public void accept(String t, Context<WXService> u) {
				u.output("请搭讪我：");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				return chain;
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
		
		chain = tryAgain(100, save, start, init);
		init(start);
	}
	
}
