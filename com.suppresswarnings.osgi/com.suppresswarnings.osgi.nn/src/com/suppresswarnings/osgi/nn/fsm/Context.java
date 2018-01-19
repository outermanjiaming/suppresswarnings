package com.suppresswarnings.osgi.nn.fsm;

import java.util.function.Predicate;

public class Context implements Predicate<String> {
	String username;
	String passcode;
	S state = S.S0;
	
	public boolean authentic() {
		return state == S.Final;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Context [username=");
		builder.append(username);
		builder.append(", passcode=");
		builder.append(passcode);
		builder.append(", state=");
		builder.append(state);
		builder.append("] authentic: ");
		builder.append(authentic());
		return builder.toString();
	}

	@Override
	public boolean test(String in) {
		state = state.to(in, this);
		state.accept(in, this);
		return authentic();
	}
}
