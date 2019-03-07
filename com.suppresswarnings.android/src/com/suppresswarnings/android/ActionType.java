package com.suppresswarnings.android;

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
	},SWIPE1 {
		@Override
		String action() {
			return "input touchscreen swipe ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "400 1290 400 400 1000" : input.replace(";", " ");
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
	},CLICK {
		@Override
		String action() {
			return "input tap";
		}

		@Override
		String input(String input) {
			return empty(input) ? " 400 400" : input.replace(";", " ");
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
	},BACK {
		@Override
		String action() {
			return "input keyevent 4";
		}

		@Override
		String input(String input) {
			return "";
		}
	},OPEN {
		@Override
		String action() {
			return "am start -n ";
		}

		@Override
		String input(String input) {
			return empty(input) ? "cn.weli.story/cn.etouch.ecalendar.MainActivity" : input.replace(";", " ");
		}
	};
	abstract String action();
	abstract String input(String input);
	static boolean empty(String input) {
		return input == null || "null".equals(input.trim()) || input.trim().length() == 0;
	}
}
