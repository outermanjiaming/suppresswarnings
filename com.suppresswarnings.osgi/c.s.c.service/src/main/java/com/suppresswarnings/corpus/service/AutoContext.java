/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service;

import java.util.HashSet;
import java.util.Iterator;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class AutoContext extends WXContext {
	String key;
	String cmd;
	HashSet<String> answers;
	Iterator<String> iterator;
	final State<Context<CorpusService>> from;
	State<Context<CorpusService>> auto = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 671358229666447461L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(iterator == null) {
				u.output("这话没法接，稍等我想一下");
				return;
			}
			if(iterator.hasNext()) {
				u.output(iterator.next());
			} else {
				iterator = answers.iterator();
				if(iterator.hasNext()) {
					u.output(iterator.next());
				} else {
					u.output("我也不知道怎么说了");
					iterator = null;
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return auto;
		}

		@Override
		public String name() {
			return "自动回复";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	public AutoContext(State<Context<CorpusService>> from, String cmd, String aid, HashSet<String> answers, String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.cmd = cmd;
		this.key = aid;
		if(answers == null) answers = new HashSet<>();
		this.answers = answers;
		if(answers.size() > 0) {
			this.iterator = answers.iterator();
		}
		this.state = auto;
		this.from = from;
	}

}
