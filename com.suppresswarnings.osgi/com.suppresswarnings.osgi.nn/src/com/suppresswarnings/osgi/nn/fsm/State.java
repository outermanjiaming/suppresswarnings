package com.suppresswarnings.osgi.nn.fsm;

public enum State {
	S0 {
		@Override
		public void accept(String in, Context context) {
			System.out.println("输入Login登录");
		}

		@Override
		public State to(String in, Context context) {
			if("login".equals(in)) return S1;
			return S0;
		}
	},S1 {
		@Override
		public void accept(String in, Context context) {
			System.out.println("Username: ");
		}

		@Override
		public State to(String in, Context context) {
			if(in.length() > 5) return S2;
			return S1F;
		}
	},S1F {
		int tried = 0;
		@Override
		public void accept(String in, Context context) {
			System.out.println("Try again("+(3-tried)+"): " );
		}

		@Override
		public State to(String in, Context context) {
			if(in.length() > 5) return S2;
			if(tried > 1) {
				tried = 0;
				return S0;
			}
			tried ++;
			return S1F;
		}
		
	},S2 {
		
		@Override
		public void accept(String in, Context context) {
			context.username = in;
			System.out.println("Passcode: " );
		}

		@Override
		public State to(String in, Context context) {
			String auth = "passcode"+context.username;
			if(auth.equals(in)) {
				return Final;
			}
			return S2F;
		}
	}, S2F{
		int tried = 0;
		@Override
		public void accept(String in, Context context) {
			System.out.println("Try again("+(3-tried)+"): " );
		}

		@Override
		public State to(String in, Context context) {
			String auth = "passcode"+context.username;
			if(auth.equals(in)) {
				return Final;
			}
			if(tried > 1) {
				tried = 0;
				return S0;
			}
			tried ++;
			return S2F;
		}
		
	},Final {
		boolean authorized = false;
		@Override
		public void accept(String in, Context context) {
			if(!authorized) {
				context.passcode = in;
				authorized = true;
			}
			System.out.println(context.username + ": Authorized");
		}

		@Override
		public State to(String in, Context context) {
			if(authorized) {
				throw new RuntimeException("stop try");
			}
			return Final;
		}
	};
	public abstract void accept(String in, Context context);
	public abstract State to(String in, Context context);
}
