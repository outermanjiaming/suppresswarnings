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
	Counter counter;
	public Counter counter(CorpusService service) {
		if(counter == null) {
			counter = service.counters.get(openid());
			if(counter == null) {
				counter = new Counter(openid());
				service.counters.put(openid(), counter);
			}
		}
		return counter;
	}
	State<Context<CorpusService>> saveAnswer = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1122472681854450463L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if("跳过".equals(t)) {
				u.output("（因为你输入'跳过'）这一句跳过了：" + task.getQuiz());
			} else if("删除这一条".equals(t)){
				String deleteKey = String.join(Const.delimiter, Const.Version.V1, "Corpus", "Delete", time(), openid(), "Quizid", task.getQuizId());
				u.content().data().put(deleteKey, task.getQuiz());
				u.content().data().del(task.getQuizId());
				handler.tasks.remove(task.getQuizId());
				handler.tasks.remove(task.getOpenId());
				u.output("（因为你输入'删除这一条'）这一句被删除了： "  + task.getQuiz());
			} else {
				//lijiaming: save answer and done
				String answer = t;
				String answerKey = String.join(Const.delimiter, task.getQuizId(), worker.getType().name(), openid(), time(), random());
				update();
				u.content().data().put(answerKey, answer);
				task.finish(answer);
				boolean reply = handler.done(task, worker.getType(), counter(u.content()));
				if(reply) {
					u.output("已采用你的回复");
				}
			}
			TodoTask todo = handler.want(worker);
			if(todo == null) {
				u.output("现在没有在线任务了，有任务立即通知你。");
			} else {
				task = todo;
				if(worker.getType() == Type.Reply) {
					StringBuffer replys = new StringBuffer(task.getQuiz());
					HashSet<String> set = handler.service.aidToAnswers.get(task.getQuizId());
					if(set != null && set.size() > 0) {
						replys.append("\n例如：\n    ");
						set.forEach(r -> replys.append(r).append("\n    "));
					}
					u.output("请回答：\n    " + replys.toString());
				} else if(worker.getType() == Type.Similar) {
					StringBuffer similars = new StringBuffer(task.getQuiz());
					HashSet<String> set = handler.service.aidToSimilars.get(task.getQuizId());
					if(set != null && set.size() > 0) {
						similars.append("\n例如：\n    ");
						set.forEach(similar -> similars.append(similar).append("\n    "));
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
				u.output(counter(u.content()).report());
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
