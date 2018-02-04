package com.suppresswarnings.osgi.data;

public interface ExampleState extends State<ExampleContent>{

	ExampleState S0 = new ExampleState(){

		@Override
		public String name() {
			return "initial state";
		}

		@Override
		public void accept(String t, ExampleContent u) {
			System.out.println("enter 'login':");
		}

		@Override
		public State<ExampleContent> apply(String t, ExampleContent u) {
			if ("login".equals(t))
				return S1;
			return S0;
		}};
	ExampleState S1 = new ExampleState(){

		@Override
		public String name() {
			return "S1(start login)";
		}

		@Override
		public void accept(String t, ExampleContent u) {
			System.out.println("enter your username:");
		}

		@Override
		public State<ExampleContent> apply(String t, ExampleContent u) {
			if (u.checkExist(t))
				return S2;
			return S1F;
		}};
	ExampleState S1F = new ExampleState(){
		final int max = 3;
		int tried = 0;
		@Override
		public String name() {
			return "S1F(username not exist, try again)";
		}

		@Override
		public void accept(String t, ExampleContent u) {
			System.out.println("Try again(" + (max - tried) + "): ");
		}

		@Override
		public State<ExampleContent> apply(String t, ExampleContent u) {
			if (u.checkExist(t))
				return S2;
			if (tried > 1) {
				tried = 0;
				return S0;
			}
			tried ++;
			return S1F;
		}};
	ExampleState S2 = new ExampleState(){

		@Override
		public String name() {
			return "S2(username ok, enter passcode)";
		}

		@Override
		public void accept(String t, ExampleContent u) {
			u.setUsername(t);
			System.out.println("enter your Passcode: ");
		}

		@Override
		public State<ExampleContent> apply(String t, ExampleContent u) {
			if (u.checkPasswd(t)) {
				return Final;
			}
			return S2F;
		}};
	ExampleState S2F = new ExampleState(){
		final int max = 3;
		int tried = 0;
		@Override
		public String name() {
			return "S2F(passcode wrong, try again)";
		}

		@Override
		public void accept(String t, ExampleContent u) {
			System.out.println("Try again(" + (max - tried) + "): ");
		}

		@Override
		public State<ExampleContent> apply(String t, ExampleContent u) {
			if(u.checkPasswd(t)) {
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

			@Override
			public String name() {
				return "Final(login ok)";
			}

			@Override
			public void accept(String t, ExampleContent u) {
				if(!u.auth()) {
					u.setPasscode(t);
					u.loginOK();
				}
			}

			@Override
			public State<ExampleContent> apply(String t, ExampleContent u) {
				return Final;
			}};
}
