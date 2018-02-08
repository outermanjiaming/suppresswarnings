package com.suppresswarnings.osgi.wx;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public class QuizS {
	String YES = "YES";
	String NO = "NO";
	String ASK = "ASK";
	String QUIZ = "QUIZ";
	String STOP = "$$";
	
	State<Context<String>> S1 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -4531797244824385817L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("enter 'login':");
		}

		@Override
		public State<Context<String>> apply(String in, Context<String> u) {
			if(YES.equals(in)) return S4;
			if(QUIZ.equals(in)) return S5;
			if(ASK.equals(in)) return S3;
			if(NO.equals(in)) return S2;
			return S1;
		}
		
		@Override
		public String name() {
			return "Init";
		}
		
		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<String>> S2 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7149475906563012414L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("不想答题就不出题了。");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			if(QUIZ.equals(in)) return S6;
			if(ASK.equals(in)) return S7;
			if(NO.equals(in)) return S4;
			return this;
		}
		
		@Override
		public String name() {
			return "No";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S3 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -4039678180700274514L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("你要我帮你决定吗？");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			if(YES.equals(in)) return S9;
			if(QUIZ.equals(in)) return S8;
			if(NO.equals(in)) return S7;
			return this;
		}
		
		@Override
		public String name() {
			return "Ask";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S4 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 6557304240036673713L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("好，我出题了：");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			if(QUIZ.equals(in)) return S5;
			if(ASK.equals(in)) return S3;
			if(NO.equals(in)) return S3;
			return this;
		}
		
		@Override
		public String name() {
			return "Yes";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S5 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -5347609199431057073L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("好，请看题：" + u.content());
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			if(ASK.equals(in)) return S8;
			if(NO.equals(in)) return S2;
			return this;
		}
		
		@Override
		public String name() {
			return "Quiz";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S6 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2739351895831478992L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("知道了，不出题。");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			if(ASK.equals(in)) return S10;
			return this;
		}
		
		@Override
		public String name() {
			return "No Quiz";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S7 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6129208829309792515L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("你问我？那要看你的心情。");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			if(QUIZ.equals(in)) return S10;
			if(NO.equals(in)) return S6;
			return this;
		}
		
		@Override
		public String name() {
			return "No Ask";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S8 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 337124734816193694L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("问我吗？你说要不要出题。");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			return this;
		}
		
		@Override
		public String name() {
			return "Ask Quiz";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S9 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3756507400594142955L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("你是问要我出题吗？");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			if(QUIZ.equals(in)) return S8;
			if(NO.equals(in)) return S3;
			return this;
		}
		
		@Override
		public String name() {
			return "Ask Yes";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<String>> S10 = new State<Context<String>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 6982755831535691970L;

		@Override
		public void accept(String t, Context<String> u) {
			u.println("既然你不想要答题，那就等你心情好了再说。");
		}
		
		@Override
		public State<Context<String>> apply(String in, Context<String> context) {
			return this;
		}
		
		@Override
		public String name() {
			return "Ask No Quiz";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
}
