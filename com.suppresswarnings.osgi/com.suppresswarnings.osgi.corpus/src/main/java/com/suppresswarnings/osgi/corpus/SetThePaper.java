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
	public SetThePaper(String openid, WXService ctx, State<Context<WXService>> s) {
		super(openid, ctx, s);
	}

	State<Context<WXService>> P0 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 3306648002814207076L;

		@Override
		public void accept(String t, Context<WXService> u) {
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
		
	State<Context<WXService>> P1 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6846950248413795401L;

		@Override
		public void accept(String t, Context<WXService> u) {
			SetThePaper.this.question = t;
			u.println(Const.SetThePaper.title[1]);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return P2;
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
	State<Context<WXService>> P2 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6468804431168262654L;

		@Override
		public void accept(String t, Context<WXService> u) {
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
			// TODO Auto-generated method stub
			return false;
		}};
	State<Context<WXService>> P3 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -1283468465948114153L;

		@Override
		public void accept(String t, Context<WXService> u) {
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
	State<Context<WXService>> P4 = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 6857306106971801340L;

		@Override
		public void accept(String t, Context<WXService> u) {
			SetThePaper.this.classify = t;
			u.println(Const.continueTitle);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(Const.yes.equals(t)) return P1;
			if(Const.no.equals(t)) return WXState.init;
			if(Const.exit.equals(t)) return WXState.init;
			return this;
		}

		@Override
		public String name() {
			return "完成";
		}

		@Override
		public boolean finish() {
			return false;
		}};
	State<Context<WXService>> Final = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 6857306106971801340L;

		@Override
		public void accept(String t, Context<WXService> u) {
			SetThePaper.this.classify = t;
			u.println(Const.continueTitle);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(Const.yes.equals(t)) return P1;
			if(Const.no.equals(t)) return WXState.init;
			if(Const.exit.equals(t)) return WXState.init;
			return this;
		}

		@Override
		public String name() {
			return "完成";
		}

		@Override
		public boolean finish() {
			return false;
		}};
}
