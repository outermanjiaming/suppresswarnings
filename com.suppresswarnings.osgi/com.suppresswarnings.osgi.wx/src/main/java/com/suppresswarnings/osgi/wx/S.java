package com.suppresswarnings.osgi.wx;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

public interface S extends State {
	S S0 = new S() {
		public String name(){return "S";}
		@Override
		public void accept(String in, Context<?> context) {
			System.out.println("enter 'login' to log on");
		}

		@Override
		public State to(String in, Context<?> context) {
			if ("login".equals(in))
				return S1;
			return S0;
		}
	};
	S S1 = new S() {
		public String name(){return "S1";}
		@Override
		public void accept(String in, Context<?> context) {
			System.out.println("Username: ");
		}

		@Override
		public State to(String in, Context<?> context) {
			if (in.length() > 5)
				return S2;
			return S1F;
		}
	};
	S S1F = new S() {
		public String name(){return "S1F";}
		int tried = 0;

		@Override
		public void accept(String in, Context<?> context) {
			System.out.println("Try again(" + (3 - tried) + "): ");
		}

		@Override
		public State to(String in, Context<?> context) {
			if (in.length() > 5)
				return S2;
			if (tried > 1) {
				tried = 0;
				return S0;
			}
			tried++;
			return S1F;
		}
	};
	S S2 = new S() {
		public String name(){return "S2";}
		@Override
		public void accept(String in, Context<?> context) {
			context.accept(in);
			System.out.println("Passcode: ");
		}

		@Override
		public State to(String in, Context<?> context) {
			String auth = "passcode";
			if (auth.equals(in)) {
				return Final;
			}
			return S2F;
		}
	};
	S S2F = new S() {
		public String name(){return "S2F";}
		int tried = 0;

		@Override
		public void accept(String in, Context<?> context) {
			System.out.println("Try again(" + (3 - tried) + "): ");
		}

		@Override
		public State to(String in, Context<?> context) {
			String auth = "passcode";
			if (auth.equals(in)) {
				return Final;
			}
			if (tried > 1) {
				tried = 0;
				return S0;
			}
			tried++;
			return S2F;
		}
	};
}