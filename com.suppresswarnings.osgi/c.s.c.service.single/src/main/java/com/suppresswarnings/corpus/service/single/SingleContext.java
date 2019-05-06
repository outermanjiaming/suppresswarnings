package com.suppresswarnings.corpus.service.single;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class SingleContext extends WXContext {
	public static final String CMD = "我是单身";
	
	State<Context<CorpusService>> single = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Tag", "Single"), time());
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Tag", "Single", openid()), openid());
			AtomicInteger count = new AtomicInteger(0);
			String start = String.join(Const.delimiter, Const.Version.V1, "Tag", "Single");
			u.content().account().page(start, start, null, Integer.MAX_VALUE, (k,v) ->{
				count.incrementAndGet();
			});
			count.addAndGet(u.content().users.size());
			u.content().data().put(String.join(Const.delimiter, Const.Version.V1, "Tag", "Single", time(), random()),  openid());
			
			u.output("「素朴网联」已经拥有了"+count.get()+"，让大家可以向未来对象提出问题，或者回答未来对象的问题！请输入：\n我要男朋友\n我要女朋友");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				return single;
			}
			if(girlfriend.name().equals(t)) {
				return girlfriend;
			}
			if(boyfriend.name().equals(t)) {
				return boyfriend;
			}
			u.output("你输入的对吗？请阅读上面那条信息。");
			return init;
		}

		@Override
		public String name() {
			return CMD;
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> question = new State<Context<CorpusService>>() {
		List<String> quiz = new ArrayList<>();
		boolean finish = false;
		
		String[] quizs = {
				"你最想问什么？",
				"你还想问什么？",
				"再问一个更尖锐的问题？",
				"最后问一个无关紧要的问题？"
		};
		int index = 0;
		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			finish = (index >= quizs.length);
			if(finish) {
				u.output("为了让你更容易被发现，请用一句话介绍自己：");
			} else {
				u.output(quizs[index]);
			}
			u.output("你说：" + t);
			
			index++;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish()) {
				quiz.clear();
				return finished;
			}
			return this;
		}

		@Override
		public String name() {
			return "我要提问";
		}

		@Override
		public boolean finish() {
			return finish;
		}
	};
	
	State<Context<CorpusService>> finished = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8031796296634830830L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("太好了，未来对象正在寻找你！");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "完成";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	State<Context<CorpusService>> answer = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8031796296634830830L;
		List<String> quiz = new ArrayList<>();
		Iterator<String> iterator = null;
		boolean finish = false;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(quiz.isEmpty()) {
				quiz.add("你喜欢什么颜色？");
				quiz.add("你想要生几个孩子？");
				quiz.add("你会做家务吗？");
				iterator = quiz.iterator();
				u.output("接下来回答一些大家找对象都关注的问题：");
			}
			if(iterator.hasNext()) u.output(iterator.next());
			else u.output("完成！你的未来对象觉得OK的话，会和你联系哦");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish()) {
				quiz.clear();
				return finished;
			}
			finish = !iterator.hasNext();
			return this;
		}

		@Override
		public String name() {
			return "我要回答";
		}

		@Override
		public boolean finish() {
			return finish;
		}
		
	};
	
	State<Context<CorpusService>> girlfriend = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你的未来女朋友也正在寻找你，她向你提了几个问题，你也可以向她提几个问题，请输入：\n我要提问\n我要回答");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(question.name().equals(t)) return question;
			if(answer.name().equals(t)) return answer;
			return girlfriend;
		}

		@Override
		public String name() {
			return "我要女朋友";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> boyfriend = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你想要一个什么样的男朋友？");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return question;
		}

		@Override
		public String name() {
			return "我要男朋友";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public SingleContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(single);
	}

}
