package com.suppresswarnings.osgi.nn.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Context implements Predicate<String> {
	Map<String,String> map = new HashMap<String,String>();
	S state = S.S0;
	
	public boolean authentic() {
		return state == S.Final;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Context [map=");
		builder.append(map);
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

	public void put(String k, String v) {
		map.put(k, v);
	}

	public String get(String key) {
		return map.get(key);
	}
}
