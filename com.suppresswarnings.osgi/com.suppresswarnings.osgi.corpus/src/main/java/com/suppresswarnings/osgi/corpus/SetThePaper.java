package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.data.Const;
/**
 * String.join(Const.delimiter, Version.V1, Const.data, Const.TextDataType.setthepapar, time, openid);
 * @author lijiaming
 *
 */
public class SetThePaper extends WXContext {
	String question;
	String answer;
	String keywords;
	String classify;
	public static final String format = "1.问题【%s】\n2.示例【%s】\n3.关键词【%s】\n4.类别【%s】\n"; 
	public SetThePaper(String openid, WXService ctx) {
		super(openid, ctx);
	}
	
	@Override
	public void log(String msg) {
		content().logger.info("[SetThePaper] " + msg);
	}

	public String confirm(){
		return String.format(format, question, answer, keywords, classify);
	}

	State<Context<WXService>> P0 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 3306648002814207076L;

		@Override
		public void accept(String t, Context<WXService> u) {
			log("P0: " + t);
			u.println(Const.SetThePaper.title[0]);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return P1;
		}

		@Override
		public String name() {
			return "请出题";
		}

		@Override
		public boolean finish() {
			return false;
		}};
		State<Context<WXService>> P0F = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -6846950248413795401L;

			@Override
			public void accept(String t, Context<WXService> u) {
				log("alter P0: " + t);
				u.println(Const.SetThePaper.title[0]);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				question = t;
				return P4F;
			}

			@Override
			public String name() {
				return "修改问题";
			}

			@Override
			public boolean finish() {
				return false;
			}};	
	State<Context<WXService>> P1 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6846950248413795401L;

		@Override
		public void accept(String t, Context<WXService> u) {
			log("question: " + question + "=" + t);
			SetThePaper.this.question = t;
			u.println(Const.SetThePaper.title[1]);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return P2;
		}

		@Override
		public String name() {
			return "问题";
		}

		@Override
		public boolean finish() {
			return false;
		}};
		State<Context<WXService>> P1F = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -6468804431168262654L;

			@Override
			public void accept(String t, Context<WXService> u) {
				log("alter question: " + question + "=" + t);
				u.println(Const.SetThePaper.title[1]);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				answer = t;
				return P4F;
			}

			@Override
			public String name() {
				return "修改回复";
			}

			@Override
			public boolean finish() {
				return false;
			}};
		
	State<Context<WXService>> P2 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6468804431168262654L;

		@Override
		public void accept(String t, Context<WXService> u) {
			log("answer: " + answer + "=" + t);
			SetThePaper.this.answer = t;
			u.println(Const.SetThePaper.title[2]);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return P3;
		}

		@Override
		public String name() {
			return "示例回复";
		}

		@Override
		public boolean finish() {
			return false;
		}};
		State<Context<WXService>> P2F = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -1283468465948114153L;

			@Override
			public void accept(String t, Context<WXService> u) {
				log("alter answer: " + answer + "=" + t);
				u.println(Const.SetThePaper.title[2]);
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				keywords = t;
				return P4F;
			}

			@Override
			public String name() {
				return "修改关键词";
			}

			@Override
			public boolean finish() {
				return false;
			}};	
	State<Context<WXService>> P3 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -1283468465948114153L;

		@Override
		public void accept(String t, Context<WXService> u) {
			log("keywords: " + keywords + "=" + t);
			SetThePaper.this.keywords = t;
			u.println(Const.SetThePaper.title[3]);			
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return P4;
		}

		@Override
		public String name() {
			return "关键词";
		}

		@Override
		public boolean finish() {
			return false;
		}};
		State<Context<WXService>> P3F = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 6857306106971801340L;

			@Override
			public void accept(String t, Context<WXService> u) {
				log("alter keywords: " + keywords + "=" + t);
				u.println(Const.SetThePaper.title[3]);			
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				classify = t;
				return P4F;
			}

			@Override
			public String name() {
				return "修改分类";
			}

			@Override
			public boolean finish() {
				return false;
			}};
	State<Context<WXService>> P4 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 6857306106971801340L;

		@Override
		public void accept(String t, Context<WXService> u) {
			log("classify: " + classify + "=" + t);
			SetThePaper.this.classify = t;
			u.println(confirm() + "修改请回复数字，确认无误请回复'yes'");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(Const.yes.equals(t)) return Finish;
			if(Const.exit.equals(t)) return WXState.init;
			if("1".equals(t)) return P0F;
			if("2".equals(t)) return P1F;
			if("3".equals(t)) return P2F;
			if("4".equals(t)) return P3F;
			return P4;
		}

		@Override
		public String name() {
			return "分类";
		}

		@Override
		public boolean finish() {
			return false;
		}};
		State<Context<WXService>> P4F = new State<Context<WXService>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 4995876025548080237L;

			@Override
			public void accept(String t, Context<WXService> u) {
				log("just confirm");
				u.println(confirm() + "修改请回复数字，确认无误请回复'yes'");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if(Const.yes.equals(t)) return Finish;
				if(Const.exit.equals(t)) return WXState.init;
				if("1".equals(t)) return P0F;
				if("2".equals(t)) return P1F;
				if("3".equals(t)) return P2F;
				if("4".equals(t)) return P3F;
				return P4F;
			}

			@Override
			public String name() {
				return "分类";
			}

			@Override
			public boolean finish() {
				return false;
			}};
	State<Context<WXService>> Finish = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 6857306106971801340L;

		@Override
		public void accept(String t, Context<WXService> u) {
			String kquestion = String.join(Const.delimiter, Version.V1, Const.data, Const.TextDataType.setthepapar, Const.SetThePaper.question, time(), openid);
			u.content().dataService.save(kquestion, question);
			
			String kanswer = String.join(Const.delimiter, Version.V1, Const.data, Const.TextDataType.setthepapar, Const.SetThePaper.answer, time(), openid);
			u.content().dataService.save(kanswer, answer);
			
			String kkeywords = String.join(Const.delimiter, Version.V1, Const.data, Const.TextDataType.setthepapar, Const.SetThePaper.keywords, time(), openid);
			u.content().dataService.save(kkeywords, keywords);
			
			String kclassify = String.join(Const.delimiter, Version.V1, Const.data, Const.TextDataType.setthepapar, Const.SetThePaper.classify, time(), openid);
			u.content().dataService.save(kclassify, classify);
			log("finish, data saved: " + confirm());
			u.println("本次出题已经完成:" + confirm() + Const.continueTitle);
			update();
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(Const.yes.equals(t)) return P0;
			if(Const.no.equals(t)) return WXState.init;
			return WXState.init;
		}

		@Override
		public String name() {
			return "继续|完成";
		}

		@Override
		public boolean finish() {
			return false;
		}};
}
