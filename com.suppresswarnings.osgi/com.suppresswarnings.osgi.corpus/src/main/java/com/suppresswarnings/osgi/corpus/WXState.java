package com.suppresswarnings.osgi.corpus;

import java.io.IOException;

import com.suppresswarnings.osgi.data.Context;
import com.suppresswarnings.osgi.data.State;
import com.suppresswarnings.osgi.data.StateClassLoader;

public interface WXState {

	State<Context<Content>> s0 = new State<Context<Content>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6663852950137316513L;

		@Override
		public void accept(String t, Context<Content> u) {
			u.println(name() + ": " + t + " -> " + u);
		}

		@Override
		public State<Context<Content>> apply(String t, Context<Content> u) {
			return s1;
		}

		@Override
		public String name() {
			return "s0";
		}

		@Override
		public boolean finish() {
			return true;
		}
		
	};
	
	State<Context<Content>> s1 = new State<Context<Content>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -5263084485232647554L;

		@Override
		public void accept(String t, Context<Content> u) {
			u.println(name() + ": " + t + " -> " + u);
		}

		@SuppressWarnings("unchecked")
		@Override
		public State<Context<Content>> apply(String t, Context<Content> u) {
			StateClassLoader loader = new StateClassLoader(Thread.currentThread().getContextClassLoader());
			try {
				byte[] bytes = u.content().cacheBytes.get("nana");
				return (State<Context<Content>>) loader.loadState(bytes, "nana");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return s0;
		}

		@Override
		public String name() {
			return "s1";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
}
