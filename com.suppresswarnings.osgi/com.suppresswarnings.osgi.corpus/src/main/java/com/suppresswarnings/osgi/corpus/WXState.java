package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public interface WXState {
	
	interface LoginState extends State<Context<WXService>> {
		LoginState S0 = new LoginState() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6235372432091017107L;

			@Override
			public String name() {
				return "S0(init)";
			}

			@Override
			public boolean finish() {
				return true;
			}

			@Override
			public void accept(String t, Context<WXService> u) {
				u.println("enter 'login'");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if ("exit()".equals(t))
					return WXState.init;
				return this;
			}
		};

		LoginState S1 = new LoginState() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -126119282585619926L;

			@Override
			public String name() {
				return "S1(start login)";
			}

			@Override
			public void accept(String t, Context<WXService> u) {
				u.println("enter your username:");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if (((LoginContext)u).checkExist(t))
					return S2;
				return S1F;
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		LoginState S1F = new LoginState() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4987184559097336635L;
			final int max = 3;
			int tried = 0;

			@Override
			public String name() {
				return "S1F(username not exist, try again)";
			}

			@Override
			public void accept(String t, Context<WXService> u) {
				u.println("Try again(" + (max - tried) + "): ");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if (((LoginContext)u).checkExist(t))
					return S2;
				if (tried > 1) {
					tried = 0;
					return S0;
				}
				tried++;
				return S1F;
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		LoginState S2 = new LoginState() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4116966909654587392L;

			@Override
			public String name() {
				return "S2(username ok, enter passcode)";
			}

			@Override
			public void accept(String t, Context<WXService> u) {
				((LoginContext)u).setUsername(t);
				u.println("enter your Passcode: ");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if (((LoginContext)u).checkPasswd(t)) {
					return Final;
				}
				return S2F;
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		LoginState S2F = new LoginState() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4029889747169188917L;
			final int max = 3;
			int tried = 0;

			@Override
			public String name() {
				return "S2F(passcode wrong, try again)";
			}

			@Override
			public void accept(String t, Context<WXService> u) {
				u.println("Try again(" + (max - tried) + "): ");
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				if (((LoginContext)u).checkPasswd(t)) {
					return Final;
				}
				if (tried > 1) {
					tried = 0;
					return S0;
				}
				tried++;
				return S2F;
			}

			@Override
			public boolean finish() {
				return false;
			}
		};

		LoginState Final = new LoginState() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3896374555858794910L;

			@Override
			public String name() {
				return "Final(login ok)";
			}

			@Override
			public void accept(String t, Context<WXService> u) {
				if (!((LoginContext)u).auth()) {
					((LoginContext)u).setPasscode(t);
					((LoginContext)u).loginOK();
					u.println("Congratuations!");
				} else {
					u.println("You've already login");
				}
			}

			@Override
			public State<Context<WXService>> apply(String t, Context<WXService> u) {
				return Final;
			}

			@Override
			public boolean finish() {
				return true;
			}
		};
	}
	
	State<Context<WXService>> init = new State<Context<WXService>>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -6996218717545604237L;

		@Override
		public void accept(String t, Context<WXService> u) {
			u.println("请输入'登录'：");
			u.content().dataService.unknown(((WXContext)u).openid(), t);
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			String openid = ((WXContext)u).openid();
			//TODO use map or ner to decide
			if("登录".equals(t)) {
				LoginContext ctx = new LoginContext(openid, u.content(), LoginState.S0);
				u.content().contexts.put(openid, ctx);
				return LoginState.S0;
			}
			return this;
		}

		@Override
		public String name() {
			return "init";
		}

		@Override
		public boolean finish() {
			return false;
		}};
}
