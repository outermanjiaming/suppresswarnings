package com.suppresswarnings.corpus.service.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.work.Quiz;

public class WaitingRoom {
	
	volatile int state;
	String key;
	Ally me;
	Ally you;
	long endTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
	List<Quiz> quiz = new ArrayList<>();
	Map<String, String> map = new ConcurrentHashMap<>();
	List<KeyValue> lastWill = new ArrayList<>();
	public WaitingRoom(String key, Ally me, List<Quiz> quiz) {
		this.key = key;
		this.me = me;
		this.quiz = quiz;
		this.state = 0;
	}
	
	public void join(Ally you) {
		this.you = you;
		this.state = 1;
		this.map.put(you.openid, me.openid);
		this.map.put(me.openid, you.openid);
	}
	
	public void chat(String openid, String t) {
		String to = map.get(openid);
		lastWill.add(new KeyValue(to, t));
	}
	
	public boolean full() {
		return state == 1;
	}
	public boolean finish(CorpusService service) {
		if(state == 2) return true;
		boolean finish = me != null && you != null && me.finish() && you.finish();
		if(finish) {
			state = 2;
			me.complete(service, you.qa);
			you.complete(service, me.qa);
			for(KeyValue kv : lastWill) {
				service.atUser(kv.key(), kv.value());
			}
		}
		return finish;
	}
	public String key() {
		return key;
	}

	
	
	public boolean finish() {
		return state == 2 && System.currentTimeMillis() - endTime > TimeUnit.MINUTES.toMillis(3);
	}
	
	@Override
	public String toString() {
		return "WaitingRoom [state=" + state + ", key=" + key + ", me=" + me + ", you=" + you + ", quiz=" + quiz + "]";
	}

	public void clear() {
		quiz.clear();
		me.clear();
		you.clear();
		me = null;
		you = null;
	}
}
