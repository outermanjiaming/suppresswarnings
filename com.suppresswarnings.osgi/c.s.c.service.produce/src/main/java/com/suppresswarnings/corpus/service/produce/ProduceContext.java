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
import java.util.Random;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.work.Counter;
import com.suppresswarnings.corpus.service.work.Quiz;

public class ProduceContext extends WXContext {
	public static final String CMD = "我要上报语料";
	
	String quizId;
	String userId;
	Counter counter;
	Random random = new Random();
	public Counter counter(CorpusService service) {
		if(counter == null) {
			counter = service.counters.get(openid());
			if(counter == null) {
				counter = new Counter(openid());
				service.counters.put(openid(), counter);
			}
		}
		//Counter the quiz answer
		return counter;
	}
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
			//TODO lijiaming: bugfix
			if(quizId == null) {
				quizId = u.content().getTodoQuizid();
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
		String[] FORMAT = {"嗯嗯，%s",
				"该我说了：\n%s", 
				"轮到我了：\n%s",
				"我想一下哈，%s", 
				"你咋说了那么多？\n%s",
				"我们说点别的吧，\n%s",
				"停，%s",
				"呃，%s",
				"不好意思，%s",
				"啥？%s",
				"等一下没事吧，%s",
				"哦，%s",
				"嗯，%s"
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
				//TODO lijiaming: check cmd
				String cmd = u.content().aidToCommand.get(reply);
				if(cmd != null) {
					String keyCMD = String.join(Const.delimiter, Const.Version.V1, openid(), "AIIoT", cmd);
					String code = u.content().account().get(keyCMD);
					//get my things code, which is unique for each things
					if(code != null) {
						String remote = u.content().aiiot(openid(), code, cmd, t, u);
						logger.info("[ProduceContext] remote: " + remote);
					} else {
						logger.info("[ProduceContext] remote: 你还没有绑定设备");
					}
				}
				
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
				//counter for quiz
				counter(u.content()).quiz(System.currentTimeMillis(), t);
				
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
						try {
							Quiz ask = askQuiz.next();
							int pointer = random.nextInt(FORMAT.length);
							u.output(String.format(FORMAT[pointer], ask.getQuiz().value()));
						} catch (Exception e) {
							askQuiz = null;
							u.output("我有点笨，我得想想");
						}
					}
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("打卡下班".equals(t)) {
				u.content().offWork(openid());
				logger.info("[ProduceContext] off work");
				u.output(counter(u.content()).report());
			}
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
