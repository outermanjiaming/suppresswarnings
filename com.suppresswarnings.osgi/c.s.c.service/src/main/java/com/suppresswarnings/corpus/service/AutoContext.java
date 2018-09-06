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

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;

public class AutoContext extends WXContext {
	String key;
	String cmd;
	HashSet<String> answers;
	Iterator<String> iterator;
	State<Context<CorpusService>> auto = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 671358229666447461L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(iterator == null) {
				u.output("这话没法接，你可以输入“"+teach.name()+"”");
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
			if(cmd.equals(t)) {
				return auto;
			}
			if("我来教你".equals(t)) {
				return teach;
			}
			return init.apply(t, u);
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
	State<Context<CorpusService>> teach = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8362444463454109741L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("主人说：“" + cmd + "”,\n我怎么答：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return saveAnswer;
		}

		@Override
		public String name() {
			return "我来教你";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	State<Context<CorpusService>> saveAnswer = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1122472681854450463L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String answer = t;
			String answerKey = String.join(Const.delimiter, key, "Reply", openid(), time(), random());
			update();
			u.content().data().put(answerKey, answer);
			answers.add(answer);
			iterator = answers.iterator();
			u.output("以后知道怎么回答了");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init.apply(t, u);
		}

		@Override
		public String name() {
			return "保存回复";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public AutoContext(String cmd, String aid, HashSet<String> answers, String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.cmd = cmd;
		this.key = aid;
		if(answers == null) answers = new HashSet<>();
		this.answers = answers;
		if(answers.size() > 0) {
			this.iterator = answers.iterator();
		}
		this.state = auto;
	}

}
