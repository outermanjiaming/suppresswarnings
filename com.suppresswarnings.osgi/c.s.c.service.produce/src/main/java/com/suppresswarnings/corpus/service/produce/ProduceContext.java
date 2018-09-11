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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.work.Quiz;

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
				String taskKey = String.join(Const.delimiter, Const.Version.V1, "Task", "Quiz", "Reply");
				quizId = u.content().data().get(taskKey);
				if(quizId == null) {
					u.output("无话题可说");
					return;
				}
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
				quizId = t.substring("SCAN_".length());
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
		Iterator<Quiz> askQuiz = null;
		int pointer = 0;
		String[] FORMAT = {"对了，有人曾经对我说：%s，我也是半天没想到怎么回答",
				"难倒我了，就像上次，别人说：%s，我该咋说", "好吧，又把我问到了，还有人说：%s，我能说什么"};
		int count = 2;
		Map<String, AutoContext> contexts = new HashMap<>();
		/**
		 * 
		 */
		private static final long serialVersionUID = -5837800412553276597L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String reply = CheckUtil.cleanStr(t);
			String aid = u.content().questionToAid.get(reply);
			if(aid != null) {
				HashSet<String> answers = u.content().aidToAnswers.get(aid);
				if(answers != null && answers.size() > 0) {
					AutoContext context = contexts.get(aid);
					if(context == null) {
						context = new AutoContext(answer, reply, aid, answers, wxid(), openid(), u.content());
						contexts.put(aid, context);
					}
					context.test(t);
					u.output(context.output());
					return;
				}
			}
			
			if(aid == null) {
				//save new question
				String answerKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Answer", openid(), time(), random());
				u.content().data().put(answerKey, t);
				String crewKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Crew", openid());
				u.content().data().put(crewKey, time());
				aid = answerKey;
			}
			//save
			u.content().questionToAid.put(reply, aid);
			//send task
			String result = u.content().youGotMe(openid(), t, aid);
			if(result != null) {
				HashSet<String> answers = u.content().aidToAnswers.get(aid);
				if(answers == null) {
					answers = new HashSet<>();
					u.content().aidToAnswers.put(aid, answers);
				}
				answers.add(result);
				u.output(result);
			} else {
				//count to 2
				//fetch a task todo
				count --;
				if(count < 0) {
					count = 2;
					if(askQuiz == null) {
						askQuiz = u.content().assimilatedQuiz.iterator();
					}
					if(askQuiz.hasNext()) {
						Quiz ask = askQuiz.next();
						u.output(String.format(FORMAT[pointer], ask.getQuiz().value()));
						pointer ++;
						if(pointer >= FORMAT.length) {
							pointer = 0;
						}
					}
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return answer;
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
