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
import java.util.Iterator;
import java.util.List;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class ReplyContext extends WXContext {
	public static final String CMD = "我要回答问题";
	public static final String[] AUTH = {"Reply"};
	
	State<Context<CorpusService>> online = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4687592239209693234L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			boolean x = u.content().iWantJob(openid(), Type.Reply);
			if(x) {
				u.output("打卡成功，一会儿有在线任务优先派发给您,\n不想接任务就输入「打卡下班」，\n如果发现错误数据就输入「删除这一条」，\n如果不知道怎么答就输入「跳过」");
			} else {
				u.output("现在暂时没有在线任务，\n不想接任务就输入「打卡下班」，\n如果发现错误数据就输入「删除这一条」，\n如果不知道怎么答就输入「跳过」");
			}
			int informed = u.content().informUsers("hi，我通过学习，现在会回答好多事情了，你想和我说什么？");
			u.output("你上线之后通知了 " + informed + " 个用户");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("打卡下班".equals(t)) {
				boolean off = u.content().offWork(openid());
				logger.info("[SimilarContext] off work: " + openid() + "= " + off);
				return init;
			}
			return online;
		}

		@Override
		public String name() {
			return "在线回答问题";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4687592239209693234L;
		boolean first = false;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("接下来您扮演机器人，我说一句，您回一句");
			getQuiz.accept(t, u);
			first = true;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(t.startsWith("SCAN_")) {
				return this;
			}
			if(first) {
				first = false;
				return reply;
			}
			return this;
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
	
	State<Context<CorpusService>> getQuiz = new State<Context<CorpusService>>() {

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
				u.output("请回答：\n"+current.value());
			} else {
				List<KeyValue> list = new ArrayList<>();
				if(quizId == null) {
					quizId = u.content().getTodoQuizid();
				}
				String head = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Answer");
				String nextReply = u.content().data().get(nextKey);
				if(nextReply == null) {
					nextReply = head;
				}
				nextReply = u.content().data().page(head, nextReply, null, Integer.MAX_VALUE, (k, v) -> {
					String z = k.substring(head.length());
					if(!z.contains("Reply") && !z.contains("Similar")) {
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
					u.output("请回答：\n" + current.value());
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
		}
	};
	public ReplyContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = online;
	}

}
