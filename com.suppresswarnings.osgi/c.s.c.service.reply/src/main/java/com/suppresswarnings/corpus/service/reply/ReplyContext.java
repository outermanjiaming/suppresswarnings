/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.reply;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class ReplyContext extends WXContext {
	public static final String CMD = "我要回答问题";
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {
		int alarm = 0;
		/**
		 * 
		 */
		private static final long serialVersionUID = 4687592239209693234L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			alarm ++;
			u.output("您已进入回答问题场景，接下来我说一句，您回一句。请问可以吗？");
			if(alarm > 2) {
				u.appendLine("其实您说'可以'就行了，接下来为您进入答题场景，请认真答题好吗？");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			//TODO lijiaming: save unknown words
			update();
			String yesKey = String.join(Const.delimiter, Const.Version.V1, "TODO", "Okay", openid(), time(), random());
			u.content().data().put(yesKey, t);
			
			if(yes(t, "可以")) return getQuiz;
			if(alarm > 2) {
				alarm = 0;
				return getQuiz;
			}
			return enter;
		}

		@Override
		public String name() {
			return "回答问题引导";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	KeyValue current = null;
	KeyValue next = null;
	State<Context<CorpusService>> getQuizOld = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 12052606984693252L;
		Iterator<KeyValue> iterator = null;
		String quizId = null;
		
		
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			String nextKey = String.join(Const.delimiter, Const.Version.V1, openid(), "Task", "Quiz", "Reply", "Next");
			if(iterator != null && iterator.hasNext()) {
				//1.get one
				//2.save next
				next = iterator.next();
				u.content().data().put(nextKey, next.key());
				u.output(current.value());
			} else {
				List<KeyValue> list = new ArrayList<>();
				if(quizId == null) {
					String taskKey = String.join(Const.delimiter, Const.Version.V1, "Task", "Quiz", "Reply");
					quizId = u.content().data().get(taskKey);
				}
				String head = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Answer");
				String nextReply = u.content().data().get(nextKey);
				if(nextReply == null) {
					nextReply = head;
				}
				nextReply = u.content().data().page(head, nextReply, null, 300, (k, v) -> {
					if(!k.contains("Reply") && !k.contains("Similar")) {
						list.add(new KeyValue(k, v));
					}
				});
				
				if(list.size() < 1) {
					u.content().data().put(nextKey, head);
					u.output("恭喜你，现在没有剩余任何问题");
				} else {
					current = list.remove(0);
					iterator = list.iterator();
					next = iterator.next();
					u.output(current.value());
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return reply;
		}

		@Override
		public String name() {
			return "出题";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> getQuiz = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5400821751033110118L;
		Iterator<KeyValue> iterator = null;
		String quizId = null;
		boolean finished = false;
		
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(iterator != null && iterator.hasNext()) {
				//1.get one
				//2.save next
				next = iterator.next();
				u.output(current.value());
			} else {
				List<KeyValue> list = new ArrayList<>();
				if(quizId == null) {
					String taskKey = String.join(Const.delimiter, Const.Version.V1, "Task", "Quiz", "Reply");
					quizId = u.content().data().get(taskKey);
				}
				if(quizId != null && iterator != null && !iterator.hasNext()) {
					u.content().fillQuestionsAndAnswers(u.content().questionToAid, u.content().aidToAnswers, quizId);
				}
				u.content().questionToAid.forEach((quiz, aid) ->{
					HashSet<String> set = u.content().aidToAnswers.get(aid);
					if(set == null || set.size() < 2) {
						KeyValue kv = new KeyValue(aid, quiz);
						list.add(kv);
					}
				});
				
				if(list.size() < 1) {
					u.output("恭喜你，现在没有剩余任何问题");
					finished = true;
				} else {
					current = list.remove(0);
					iterator = list.iterator();
					next = iterator.next();
					u.output(current.value());
				}
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finished) return init.apply(t, u);
			return reply;
		}

		@Override
		public String name() {
			return "出题";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> reply = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5471448779515595187L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String answer = t;
			String questionKey = current.key();
			String answerKey = String.join(Const.delimiter, questionKey, "Reply", openid(), time(), random());
			update();
			u.content().data().put(answerKey, answer);
			current = next;
			getQuiz.accept(t, u);
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return getQuiz.apply(t, u);
		}

		@Override
		public String name() {
			return "答题";
		}

		@Override
		public boolean finish() {
			return false;
		}};
	public ReplyContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = enter;
	}

}
