/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.produce;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class ProduceContext extends WXContext {
	public static final String CMD = "我要上报语料";
	
	String quizId;
	String userId;
	
	State<Context<CorpusService>> produce = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7444460271465288643L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(quizId == null) {
				u.output("请通过扫码进入");
				return;
			}
			String quizKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId);
			String quizOpenIdKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "OpenId");
			userId = u.content().data().get(quizOpenIdKey);
			String quiz = u.content().data().get(quizKey);
			String crewKey = String.join(Const.delimiter, Const.Version.V1, userId, "Crew", openid());
			String exist = u.content().account().get(crewKey);
			if(exist == null) {
				u.content().account().put(crewKey, quizId);
			}
			u.output(quiz);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.startsWith("SCAN_")) {
				quizId = t.substring(5);
				return produce;
			}
			if(CMD.equals(t)) {
				return produce;
			}
			return answer;
		}

		@Override
		public String name() {
			return "生产语料";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> answer = new State<Context<CorpusService>>() {
		boolean first = true;
		/**
		 * 
		 */
		private static final long serialVersionUID = -5837800412553276597L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String reply = t;
			String answerKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Answer", openid(), time(), random());
			u.content().data().put(answerKey, reply);
			String crewKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Crew", openid());
			u.content().data().put(crewKey, time());
			update();
			if(first) {
				first = false;
				u.output("谢谢，语料收集任务已经完成。准备进入下一阶段。");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			//TODO lijiaming: save unknown words
			update();
			String yesKey = String.join(Const.delimiter, Const.Version.V1, "TODO", "Next", openid(), time(), random());
			u.content().data().put(yesKey, t);
			
			ContextFactory<CorpusService> cf = u.content().factories.get("我要回答问题");
			if(cf != null) {
				Context<CorpusService> context = cf.getInstance(wxid(), openid(), u.content());
				u.content().context(openid(), context);
				return context.state();
			}
			return init;
		}

		@Override
		public String name() {
			return "答题";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	public ProduceContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = produce;
	}

}
