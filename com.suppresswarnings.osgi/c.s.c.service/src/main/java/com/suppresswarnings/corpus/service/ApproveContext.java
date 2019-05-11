package com.suppresswarnings.corpus.service;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;

@SuppressWarnings("unused")
public class ApproveContext extends WXContext {
	String message;
	String key;

	State<Context<CorpusService>> approve = new State<Context<CorpusService>>() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7662344284808100547L;

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("同意".equals(t)) {
				return agree;
			} else if("拒绝".equals(t)) {
				return refuse;
			}
			return this;
		}
		
		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("待审批：");
			u.output(key);
			u.output(message);
			u.output("请输入：同意/拒绝");
		}
		
		@Override
		public String name() {
			return "CEO审批";
		}
		
		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> refuse = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5223665807439517873L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String authKey = String.join(Const.delimiter, Const.Version.V1, "Approve", key);
			u.content().account().put(authKey, "Refuse");
			u.content().account().put(
					String.join(Const.delimiter, Const.Version.V1, "Info", "Refused", key), 
					String.join(Const.delimiter, openid(), "Refused", time(), message));
			u.output("审批拒绝");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "审批拒绝";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> agree = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5223665807439517873L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String authKey = String.join(Const.delimiter, Const.Version.V1, "Approve", key);
			u.content().account().put(authKey, "Approved");
			u.content().account().put(
					String.join(Const.delimiter, Const.Version.V1, "Info", "Approved", key), 
					String.join(Const.delimiter, openid(), "Approved", time(), message));
			u.output("审批同意");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return init;
		}

		@Override
		public String name() {
			return "审批同意";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public ApproveContext(KeyValue keyValue, String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.message = keyValue.value();
		this.key  = keyValue.key();
		this.state(approve);
	}

}
