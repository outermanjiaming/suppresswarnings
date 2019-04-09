package com.suppresswarnings.corpus.service.game;

public class WaitingRoom {
	volatile boolean state;
	String key;
	Ally me;
	Ally you;
	public WaitingRoom(String key, Ally me) {
		this.key = key;
		this.me = me;
	}
	
	public void join(Ally you) {
		this.you = you;
	}
	
	public String key() {
		return key;
	}
	
}
