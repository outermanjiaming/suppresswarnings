package com.suppresswarnings.osgi.nn.dfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Page<T> {
	Map<String, String> info = new HashMap<String, String>();
	List<T> data = new ArrayList<T>();
	public void add(T quiz) {
		data.add(quiz);
	}

	public void set(String key, String value){
		info.put(key, value);
	}

	@Override
	public String toString() {
		return "\n\tPage [info=" + info + ", data=" + data + "]";
	}
	
}
