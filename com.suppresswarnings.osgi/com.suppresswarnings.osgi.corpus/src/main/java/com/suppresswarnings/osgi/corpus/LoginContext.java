package com.suppresswarnings.osgi.corpus;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

/**
 * each active user has a new LoginContext, period
 * @author lijiaming
 *
 */
public class LoginContext extends WXContext {
	String passcode;
	String username;
	boolean auth = false;
	public void loginOK() {
		this.auth = true;
	}
	public boolean auth() {
		return auth;
	} 
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}
	public boolean checkExist(String username) {
		if(content().accountService.exist(username)) return true;
		return false;
	}
	public boolean checkPasswd(String passwd) {
		if(username != null) {
			String uid = content().accountService.login(username, passwd);
			if(uid != null)	{
				return true;
			}
		}
		return false;
	}
	public LoginContext(String openid, WXService ctx) {
		super(openid, ctx);
	}
	
	public static void main(String[] args) {
		System.out.println(WXState.mailRegex.matcher("lijiaming@suppresswarnings.com").matches());
	}
	
	
	long loginExpire = TimeUnit.HOURS.toMillis(2);
	State<Context<WXService>> S0 = new State<Context<WXService>>() {

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
			return false;
		}

		@Override
		public void accept(String t, Context<WXService> u) {
			u.println("enter 'login'");
		}

		@Override
		public State<Context<WXService>> apply(String t, Context<WXService> u) {
			if ("exit()".equals(t))
				return WXState.init;
			else if("login".equals(t))
				return S1;
			return this;
		}
	};

	State<Context<WXService>> S1 = new State<Context<WXService>>() {

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
	State<Context<WXService>> S1F = new State<Context<WXService>>() {
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
	State<Context<WXService>> S2 = new State<Context<WXService>>() {

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
	State<Context<WXService>> S2F = new State<Context<WXService>>() {
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

	State<Context<WXService>> Final = new State<Context<WXService>>() {

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
