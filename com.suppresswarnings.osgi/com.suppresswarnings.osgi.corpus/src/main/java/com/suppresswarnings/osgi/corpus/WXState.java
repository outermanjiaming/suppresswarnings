package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public interface WXState {
	State<Context<WXService>> init = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6996218717545604237L;

		@Override
		public void accept(String t, Context<WXService> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
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
	State<Context<WXService>> login = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -5776284883438797671L;

		@Override
		public void accept(String t, Context<WXService> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
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
	State<Context<WXService>> answerQuiz = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 2331875505998021648L;

		@Override
		public void accept(String t, Context<WXService> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
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
	State<Context<WXService>> setThePaper = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -3205749336390343569L;

		@Override
		public void accept(String t, Context<WXService> u) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
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
