package com.suppresswarnings.osgi.nn.dfa;

import java.util.HashMap;
import java.util.Map;

public class Quiz {
	Map<String, String> info = new HashMap<String, String>();
	public void set(String key, String value){
		info.put(key, value);
	}
	@Override
	public String toString() {
		return "\n\t\tQuiz [info=" + info + "]";
	}
	
}
