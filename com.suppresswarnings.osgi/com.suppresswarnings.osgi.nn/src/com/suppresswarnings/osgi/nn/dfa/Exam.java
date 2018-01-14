package com.suppresswarnings.osgi.nn.dfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exam {
	Map<String, String> info = new HashMap<String, String>();
	Page<Quiz> current;
	List<Page<Quiz>> pages = new ArrayList<Page<Quiz>>();
	public void addPage(Page<Quiz> page) {
		current = page;
		pages.add(page);
	}
	public void set(String key, String value){
		info.put(key, value);
	}
	@Override
	public String toString() {
		return "Exam [info=" + info + ", current=" + current + ", \n  pages=" + pages + "]";
	}
	
}
