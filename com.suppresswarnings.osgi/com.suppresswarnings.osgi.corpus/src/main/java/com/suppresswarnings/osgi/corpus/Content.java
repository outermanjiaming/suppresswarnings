package com.suppresswarnings.osgi.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.suppresswarnings.osgi.data.DataService;
import com.suppresswarnings.osgi.data.TTL;

public class Content {
	volatile boolean clean = false;
	List<TTL> ttl = new ArrayList<TTL>();
	Map<String, String> cacheString = new ConcurrentHashMap<String, String>();
	Map<String, byte[]> cacheBytes = new ConcurrentHashMap<String, byte[]>();
	DataService dataService;
	public void set(String name, byte[] bytes) {
		cacheBytes.put(name, bytes);
	}
	
	public synchronized void clear(){
		clean = true;
	}
}
