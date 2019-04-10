package com.suppresswarnings.corpus.service.game;

import java.util.ArrayList;
import java.util.List;

import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.work.Quiz;

public class WaitingRoom {
	
	volatile int state;
	String key;
	Ally me;
	Ally you;
	List<Quiz> quiz = new ArrayList<>();
	public WaitingRoom(String key, Ally me, List<Quiz> quiz) {
		this.key = key;
		this.me = me;
		this.quiz = quiz;
		this.state = 0;
	}
	
	public void join(Ally you) {
		this.you = you;
		this.state = 1;
	}
	
	public boolean full() {
		return state == 1;
	}
	public boolean finish(CorpusService service) {
		boolean finish = me.finish() && you.finish();
		if(finish) {
			me.complete(service, you.qa);
			you.complete(service, me.qa);
		}
		return finish;
	}
	public String key() {
		return key;
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
