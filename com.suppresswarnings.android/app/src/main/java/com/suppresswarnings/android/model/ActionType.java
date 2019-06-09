package com.suppresswarnings.android.model;

public enum ActionType {
	SWIPE0 {
		@Override
		String action() {
			return "input touchscreen swipe ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "400 400 400 1290 1000" : input.replace(";", " ");
		}

		@Override
		public int what() {
			return 502;
		}
	},SWIPE1 {
		@Override
		String action() {
			return "input touchscreen swipe ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "400 1290 400 400 1000" : input.replace(";", " ");
		}

		@Override
		public int what() {
			return 503;
		}
	},SCOLL {
		@Override
		String action() {
			return "input touchscreen swipe ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "400 790 400 530 1000" : input.replace(";", " ");
		}

		@Override
		public int what() {
			return 504;
		}
	},SCROLL {
		@Override
		String action() {
			return "input touchscreen swipe ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "400 530 400 790 1000" : input.replace(";", " ");
		}

		@Override
		public int what() {
			return 505;
		}
	},CLICK {
		@Override
		String action() {
			return "input tap ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "400 450" : input.replace(";", " ");
		}

		@Override
		public int what() {
			return 506;
		}
	},HOME {
		@Override
		String action() {
			return "input keyevent 3";
		}

		@Override
		String input(String input) {
			return "";
		}

		@Override
		public int what() {
			return 500;
		}
	},BACK {
		@Override
		String action() {
			return "input keyevent 4";
		}

		@Override
		String input(String input) {
			return "";
		}

		@Override
		public int what() {
			return 501;
		}
	},OPEN {
		@Override
		String action() {
			return "am start -n ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "cn.weli.story/cn.etouch.ecalendar.MainActivity" : input;
		}

		@Override
		public int what() {
			return 507;
		}
	},LEFT {
		@Override
		String action() {
			return "input touchscreen swipe ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "500 600 100 600 1000" : input.replace(";", " ");
		}

		@Override
		public int what() {
			return 508;
		}
	},RIGHT {
		@Override
		String action() {
			return "input touchscreen swipe ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "100 600 500 600 1000" : input.replace(";", " ");
		}

		@Override
		public int what() {
			return 509;
		}
	},SLEEP {
		@Override
		String action() {
			return "sleep ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "1" : input;
		}

		@Override
		public int what() {
			return 0;
		}
	},LOOP {
		@Override
		String action() {
			return "for i in {1..";
		}

		@Override
		String input(String input) {
			return empty(input) ? "10}" : input + "}";
		}

		@Override
		public int what() {
			return 601;
		}
	},WHILE {
		@Override
		String action() {
			return "while ";
		}

		@Override
		String input(String input) {
			return empty(input) ? " " : input;
		}

		@Override
		public int what() {
			return 602;
		}
	},JUMP {
		@Override
		String action() {
			return "skip ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "3" : input;
		}

		@Override
		public int what() {
			return 1002;
		}
	},TAP {
		@Override
		String action() {
			return "input tap ";
		}

		@Override
		String input(String input) {
			return empty(input) ? " " : input;
		}

		@Override
		public int what() {
			return 1003;
		}
	},PASTE {
		@Override
		String action() {
			return "input ";
		}

		@Override
		String input(String input) {
			return empty(input) ? " " : input;
		}

		@Override
		public int what() {
			return 1004;
		}
	},INFO {
		@Override
		String action() {
			return "alert ";
		}

		@Override
		String input(String input) {
			return empty(input) ? " " : input;
		}

		@Override
		public int what() {
			return 9999;
		}
	};
	abstract String action();
	abstract String input(String input);
	public abstract int what();
	static boolean empty(String input) {
		return input == null || "null".equals(input.trim()) || input.trim().length() == 0;
	}
}
