package com.suppresswarnings.osgi.corpus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.suppresswarnings.osgi.data.DataService;

public class Content {
	Map<String, String> cacheString = new ConcurrentHashMap<String, String>();
	Map<String, byte[]> cacheBytes = new ConcurrentHashMap<String, byte[]>();
	DataService dataService;
	public void set(String name, byte[] bytes) {
		cacheBytes.put(name, bytes);
	}
}
