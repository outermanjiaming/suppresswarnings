package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.data.Const;
import com.suppresswarnings.osgi.user.KEY;
import com.suppresswarnings.osgi.user.User;

public class InviteContext extends WXContext {
	State<Context<WXService>> first = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 4211819763208662020L;

		@Override
		public void accept(String t, Context<WXService> u) {
			u.output("输入'查询'当前已经发出去的邀请码\n输入'生成'得到新的邀请码");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if("查询".equals(t)) return query;
			if("生成".equals(t)) return start;
			return retry;
		}

		@Override
		public String name() {
			return "邀请";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<WXService>> retry = tryAgain(3, first, first, init);
	State<Context<WXService>> query = new State<Context<WXService>>(){
		int todo = 0;
		/**
		 * 
		 */
		private static final long serialVersionUID = 4150733117445301958L;

		@Override
		public void accept(String t, Context<WXService> u) {
			String key = String.join(Const.delimiter, Version.V1, openid(), KEY.Invite.name());
			String invite = u.content().getFromAccount(key);
			if(invite == null) {
				u.output("当前没有邀请码，可以输入'生成'获得邀请码");
				todo = 1;
				return;
			}
			String[] codes = invite.split("\\.");
			StringBuffer sb = new StringBuffer();
			for(String code : codes) {
				String keyUid = String.join(Const.delimiter, Version.V1, openid(), KEY.Invite.name(), code);
				String uid = u.content().getFromAccount(keyUid);
				if(uid == null) {
					sb.append("[未使用]").append(code).append("\n");
					continue;
				}
				String name = u.content().getFromAccount(String.join(Const.delimiter, Version.V1, uid, KEY.Name.name()));
				if(name == null) {
					sb.append("[已注册]").append(code).append("\n");
					continue;
				}
				sb.append("[").append(name).append("]").append(code).append("\n");
			}
			u.output(sb.toString());
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(todo == 1) return first.apply(t, u);
			return init;
		}

		@Override
		public String name() {
			return "查看邀请码";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<WXService>> start = new State<Context<WXService>>(){
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3798324613884195822L;
		
		@Override
		public void accept(String t, Context<WXService> u) {
			String limit = String.join(Const.delimiter, Version.V1, openid(), KEY.Invite.name(), KEY.Limit.name());
			String value = content().getFromAccount(limit);
			if(value == null) {
				failed = -1;
				u.output("不好意思，你的邀请名额用完了");
				return;
			}
			String leader = String.join(Const.delimiter, Version.V1, "Leader", openid);
			String valid = content().getFromAccount(leader);
			if(valid == null) {
				failed = -2;
				u.output("目前只有Leader用户才可以邀请");
				return;
			}
			
			int left = Integer.valueOf(value);
			if(left > 0) {
				u.output(msg4left(left) + "，你确定使用一个邀请名额吗？（是/否）");
				return;
			}
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(failed < 0) {
				failed = 0;
				return fail;
			}
			if(yes(t, "是")) {
				return invite;
			}
			return init;
		}

		@Override
		public String name() {
			return "是否邀请";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<WXService>> invite = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 7260437970360262365L;

		@Override
		public void accept(String t, Context<WXService> u) {
			User user = new User(openid());
			String invite = content().accountService.invite(user);
			if(invite == null) {
				failed = -3;
				u.output("邀请失败，请联系你的Leader");
				return;
			}
			u.output(invite);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if(failed < 0) {
				failed = 0;
				return fail;
			}
			return done;
		}

		@Override
		public String name() {
			return "邀请";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<WXService>> done = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -7591893265687742503L;

		@Override
		public void accept(String t, Context<WXService> u) {
			u.output("请将邀请码给你的朋友，使用方法请查看攻略http://suppresswarnings.com/walkthrough/invite.html");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			return init;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public boolean finish() {
			return true;
		}
	};
	
	int failed = 0;
	
	public InviteContext(String openid, WXService ctx) {
		super(openid, ctx);
		init(first);
	}
	
	public String msg4left(int left) {
		if(left < 3) return "剩余" + left + "个邀请名额";
		if(left < 10) return "还有" + left + "个邀请名额";
		return "还有很多邀请名额";
	}
}
