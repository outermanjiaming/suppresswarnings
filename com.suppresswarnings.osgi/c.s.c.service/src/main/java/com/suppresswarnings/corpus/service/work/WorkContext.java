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

import java.util.HashSet;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class WorkContext extends WXContext {
	WorkHandler handler;
	TodoTask task;
	WorkerUser worker;
	State<Context<CorpusService>> saveAnswer = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1122472681854450463L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String answer = t;
			String answerKey = String.join(Const.delimiter, task.getQuizId(), worker.getType().name(), openid(), time(), random());
			update();
			u.content().data().put(answerKey, answer);
			task.finish(answer);
			boolean reply = handler.done(task, worker.getType());
			if(reply) {
				u.output("已采用你的回复");
			}
			TodoTask todo = handler.want(worker);
			if(todo == null) {
				u.output("现在没有在线任务了，有任务立即通知你。");
			} else {
				task = todo;
				if(worker.getType() == Type.Reply) {
					u.output("请回答：\n    " + task.getQuiz());
				} else if(worker.getType() == Type.Similar) {
					StringBuffer similars = new StringBuffer(task.getQuiz());
					HashSet<String> set = handler.service.aidToSimilars.get(task.getQuizId());
					if(set != null && set.size() > 0) {
						similars.append("\n例如：\n    ");
						set.iterator().forEachRemaining(similar -> similars.append(similar).append("\n    "));
					}
					u.output("同义句：\n    " + similars.toString());
				} else {
					u.output("任务：\n" + task.getQuiz());
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("打卡下班".equals(t)) {
				handler.clockOut(openid());
				return init;
			}
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
	public WorkContext(WorkHandler handler, TodoTask task, WorkerUser worker, String wxid, CorpusService ctx) {
		super(wxid, worker.getOpenId(), ctx);
		this.handler = handler;
		this.task = task;
		this.worker = worker;
		this.state = saveAnswer;
	}

}
