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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
				quizId = u.content().getTodoQuizid();
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
	public static final int bear = 2;
	State<Context<CorpusService>> answer = new State<Context<CorpusService>>() {
		Iterator<Quiz> askQuiz = null;
		int pointer = 0;
		String[] FORMAT = {"有的主人说：%s",
				"换个话题，比如说：%s", 
				"又把我问倒了，你可以说：%s",
				"我想一下哈，%s", 
				"你咋说了那么多？你试试说：%s",
				"我们说点别的，比如：%s",
				"你觉得我安静好不好，%s",
				"不好意思，呃，%s",
				"刚刚思想开小差了，%s",
				"对不起哈，你试一下这样说：%s"
				};
		int count = bear;
		Map<String, AutoContext> contexts = new HashMap<>();
		/**
		 * 
		 */
		private static final long serialVersionUID = -5837800412553276597L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String reply = CheckUtil.cleanStr(t);
			String aid = u.content().questionToAid.get(reply);
			logger.info("[ProduceContext] after clean: " + reply + " = " + aid);
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
					count = bear;
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
				count = bear;
			} else {
				//count to 2
				//fetch a task todo
				count --;
				if(count < 0) {
					count = bear;
					if(askQuiz == null) {
						List<Quiz> quizs = u.content().assimilatedQuiz;
						Collections.shuffle(quizs);
						askQuiz = quizs.iterator();
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
