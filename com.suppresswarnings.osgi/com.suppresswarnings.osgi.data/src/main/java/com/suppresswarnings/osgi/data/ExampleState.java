package com.suppresswarnings.osgi.data;

public interface ExampleState extends State<Context<ExampleContent>>{
	ExampleState S0 = new ExampleState(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 5944501582121800681L;

		@Override
		public String name() {
			return "initial state";
		}

		@Override
		public void accept(String t, Context<ExampleContent> u) {
			System.out.println("enter 'login':");
		}

		@Override
		public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
			if ("login".equals(t))
				return S1;
			return S0;
		}};
	ExampleState S1 = new ExampleState(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -126119282585619926L;

		@Override
		public String name() {
			return "S1(start login)";
		}

		@Override
		public void accept(String t, Context<ExampleContent> u) {
			System.out.println("enter your username:");
		}

		@Override
		public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
			if (u.content.checkExist(t))
				return S2;
			return S1F;
		}};
	ExampleState S1F = new ExampleState(){
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
		public void accept(String t, Context<ExampleContent> u) {
			System.out.println("Try again(" + (max - tried) + "): ");
		}

		@Override
		public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
			if (u.content.checkExist(t))
				return S2;
			if (tried > 1) {
				tried = 0;
				return S0;
			}
			tried ++;
			return S1F;
		}};
	ExampleState S2 = new ExampleState(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 4116966909654587392L;

		@Override
		public String name() {
			return "S2(username ok, enter passcode)";
		}

		@Override
		public void accept(String t, Context<ExampleContent> u) {
			u.content.setUsername(t);
			System.out.println("enter your Passcode: ");
		}

		@Override
		public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
			if (u.content.checkPasswd(t)) {
				return Final;
			}
			return S2F;
		}};
	ExampleState S2F = new ExampleState(){
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
		public void accept(String t, Context<ExampleContent> u) {
			System.out.println("Try again(" + (max - tried) + "): ");
		}

		@Override
		public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
			if(u.content.checkPasswd(t)) {
				return Final;
			}
			if (tried > 1) {
				tried = 0;
				return S0;
			}
			tried++;
			return S2F;
		}};
		
		ExampleState Final = new ExampleState(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -3896374555858794910L;

			@Override
			public String name() {
				return "Final(login ok)";
			}

			@Override
			public void accept(String t, Context<ExampleContent> u) {
				if(!u.content.auth()) {
					u.content.setPasscode(t);
					u.content.loginOK();
				}
			}

			@Override
			public State<Context<ExampleContent>> apply(String t, Context<ExampleContent> u) {
				return Final;
			}};
}
