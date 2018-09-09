/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.work;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class WorkContext extends WXContext {
	String quiz;
	String quizId;
	WorkHandler handler;
	TodoTask task;
	State<Context<CorpusService>> saveAnswer = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1122472681854450463L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String answer = t;
			String answerKey = String.join(Const.delimiter, quizId, "Reply", openid(), time(), random());
			update();
			u.content().data().put(answerKey, answer);
			task.finish(answer);
			boolean reply = handler.done(task);
			if(reply) {
				u.output("已采用你的回复");
			}
			TodoTask todo = handler.want(openid());
			if(todo == null) {
				u.output("现在没有在线任务了，有任务立即通知你。");
			} else {
				task = todo;
				quiz = todo.getQuiz();
				quizId = todo.getQuizId();
				u.output("请回答：\n" + quiz);
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return saveAnswer;
		}

		@Override
		public String name() {
			return "保存回复";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	public WorkContext(WorkHandler handler, TodoTask task, String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.handler = handler;
		this.task = task;
		this.quiz = task.getQuiz();
		this.quizId = task.getQuizId();
		this.state = saveAnswer;
	}

}
