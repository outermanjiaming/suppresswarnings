package com.suppresswarnings.osgi.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
	private static final class Holder{
		static final Counter Instance = new Counter();
	}
	public static Counter getInstance(){
		return Holder.Instance;
	}
	volatile boolean initialized = false;
	Map<String, AtomicInteger> userCounter = new ConcurrentHashMap<String, AtomicInteger>();
	Map<String, Long> ttl = new ConcurrentHashMap<String, Long>();
	
	public void init(Initializer<Counter> init) {
		init.init(this);
	}
	public void ttl(String key, long timeToLive) {
		long liveUntil = timeToLive + System.currentTimeMillis();
		ttl.put(key, liveUntil);
	}
	public void set(String key, AtomicInteger current) {
		userCounter.put(key, current);
	}
	public AtomicInteger get(String key){
		return userCounter.get(key);
	}
	//clear those time out
	public void clean(){
		
	}
}
