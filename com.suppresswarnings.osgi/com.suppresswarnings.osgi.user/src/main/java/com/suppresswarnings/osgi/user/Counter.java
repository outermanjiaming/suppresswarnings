package com.suppresswarnings.osgi.user;

import java.util.ArrayList;
import java.util.List;
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
	
	Map<String, AtomicInteger> atomicounter = new ConcurrentHashMap<String, AtomicInteger>();
	Map<String, Long> ttl = new ConcurrentHashMap<String, Long>();
	public boolean initialized() {
		return initialized;
	}
	public void init(Initializer<Counter> init) {
		init.init(this);
		initialized = true;
	}
	public void ttl(String key, long timeToLive) {
		long liveUntil = timeToLive + System.currentTimeMillis();
		ttl.put(key, liveUntil);
	}
	public void set(String key, AtomicInteger current) {
		atomicounter.put(key, current);
	}
	public void remove(String key) {
		atomicounter.remove(key);
		ttl.remove(key);
	}
	public AtomicInteger get(String key){
		return atomicounter.get(key);
	}
	//clear those time out
	public void clean(){
		List<String> delete = new ArrayList<String>();
		long current = System.currentTimeMillis();
		ttl.forEach((key, time) -> {
			if(time < current) {
				delete.add(key);
			}
		});
		for(String key : delete) {
			remove(key);
		}
	}
}
