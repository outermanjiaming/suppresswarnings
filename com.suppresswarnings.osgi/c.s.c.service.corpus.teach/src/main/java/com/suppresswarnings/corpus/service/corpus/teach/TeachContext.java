/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.corpus.teach;

import java.util.ArrayList;
import java.util.List;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class TeachContext extends WXContext {
	public static final String CMD = "我要教你";
	StringBuffer Q = new StringBuffer();
	StringBuffer A = new StringBuffer();
	List<String> qa = new ArrayList<String>();
	
	State<Context<CorpusService>> teach = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1211826950299529420L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("教学规则：\n1.你先问我问题，然后我再请教你怎么回答。\n2.你随时都可以说'我要退出'从而停止。\n3.说错了没有关系，你可以重复一次。\n\n好了现在开始，你随便问我什么：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				return this;
			}
			return question;
		}

		@Override
		public String name() {
			return "教学入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};

	State<Context<CorpusService>> question = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7798331468520579709L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			Q.setLength(0);
			Q.append(t);
			u.output("好的，请问我该怎么回答？");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				return init;
			}
			return reply;
		}

		@Override
		public String name() {
			return "自己提问";
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
		private static final long serialVersionUID = -7798331468520579709L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			A.setLength(0);
			A.append(t);
			qa.add("问：" + Q.toString());
			qa.add("答：" + A.toString());
			qa.add("----Just a 分割线----");
			u.output("好的，这个问题知道了。\n你可以继续问我其他问题：");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(exit(t, "我要退出")) {
				for(String line : qa) {
					u.appendLine(line);
				}
				return init;
			}
			return question;
		}

		@Override
		public String name() {
			return "自己回复";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	public TeachContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = teach;
	}

}
