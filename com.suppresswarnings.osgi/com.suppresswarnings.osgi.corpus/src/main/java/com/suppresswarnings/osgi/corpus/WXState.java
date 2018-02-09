package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public interface WXState {
	State<Context<Content>> init = new State<Context<Content>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6996218717545604237L;

		@Override
		public void accept(String t, Context<Content> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<Content>> apply(String t, Context<Content> u) {
			return login;
		}

		@Override
		public String name() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean finish() {
			// TODO Auto-generated method stub
			return false;
		}};
	State<Context<Content>> login = new State<Context<Content>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -5776284883438797671L;

		@Override
		public void accept(String t, Context<Content> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<Content>> apply(String t, Context<Content> u) {
			return answerQuiz;
		}

		@Override
		public String name() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean finish() {
			// TODO Auto-generated method stub
			return false;
		}};
	State<Context<Content>> answerQuiz = new State<Context<Content>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 2331875505998021648L;

		@Override
		public void accept(String t, Context<Content> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<Content>> apply(String t, Context<Content> u) {
			return setThePaper;
		}

		@Override
		public String name() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean finish() {
			// TODO Auto-generated method stub
			return false;
		}};
	State<Context<Content>> setThePaper = new State<Context<Content>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -3205749336390343569L;

		@Override
		public void accept(String t, Context<Content> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<Content>> apply(String t, Context<Content> u) {
			return init;
		}

		@Override
		public String name() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean finish() {
			// TODO Auto-generated method stub
			return false;
		}};
}
