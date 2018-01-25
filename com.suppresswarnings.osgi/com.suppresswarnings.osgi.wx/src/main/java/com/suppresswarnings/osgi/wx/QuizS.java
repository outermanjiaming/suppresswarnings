package com.suppresswarnings.osgi.wx;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public interface QuizS extends State {
	String YES = "YES";
	String NO = "NO";
	String ASK = "ASK";
	String QUIZ = "QUIZ";
	String STOP = "$$";
	
	State S1 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("不知道");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(YES.equals(in)) return S4;
			if(QUIZ.equals(in)) return S5;
			if(ASK.equals(in)) return S3;
			if(NO.equals(in)) return S2;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "Init";
		}
	};
	State S2 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("不想答题就不出题了。");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(QUIZ.equals(in)) return S6;
			if(ASK.equals(in)) return S7;
			if(NO.equals(in)) return S4;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "No";
		}
	};
	State S3 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("你要我帮你决定吗？");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(YES.equals(in)) return S9;
			if(QUIZ.equals(in)) return S8;
			if(NO.equals(in)) return S7;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "Ask";
		}
	};
	State S4 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("好，我出题了：");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(QUIZ.equals(in)) return S5;
			if(ASK.equals(in)) return S3;
			if(NO.equals(in)) return S3;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "Yes";
		}
	};
	State S5 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("好，请看题：");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(ASK.equals(in)) return S8;
			if(NO.equals(in)) return S2;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "Quiz";
		}
	};
	State S6 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("知道了，不出题。");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(ASK.equals(in)) return S10;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "No Quiz";
		}
	};
	State S7 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("你问我？那要看你的心情。");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(QUIZ.equals(in)) return S10;
			if(NO.equals(in)) return S6;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "No Ask";
		}
	};
	State S8 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("问我吗？你说要不要出题。");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "Ask Quiz";
		}
	};
	State S9 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("你是问要我出题吗？");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(QUIZ.equals(in)) return S8;
			if(NO.equals(in)) return S3;
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "Ask Yes";
		}
	};
	State S10 = new State() {
		
		@Override
		public void accept(String t, Context<?> u) {
			u.accept("既然你不想要答题，那就等你心情好了再说。");
		}
		
		@Override
		public State to(String in, Context<?> context) {
			if(STOP.equals(in)) return Final;
			return this;
		}
		
		@Override
		public String name() {
			return "Ask No Quiz";
		}
	};
}
