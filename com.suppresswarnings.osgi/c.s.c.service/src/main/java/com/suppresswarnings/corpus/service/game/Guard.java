package com.suppresswarnings.corpus.service.game;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.CorpusService;

public class Guard {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	AtomicReference<WaitingRoom> current = new AtomicReference<WaitingRoom>(null);
	Map<String, WaitingRoom> active = new ConcurrentHashMap<>();
	CorpusService service;
	long start;
	public Guard(CorpusService service) {
		this.service = service;
		this.start = System.currentTimeMillis();
		logger.info("create Guard from " + start);
	}
	
	public String roomKey() {
		return service.uniqueKey("Waiting.Room");
	}
	
	public synchronized Ally joinOrCreateWaitingRoom(Context<CorpusService> context, String openid, String ownerid, String qrScene) {
		WaitingRoom room = current.get();
		Ally ally;
		if(room == null) {
			Ally me = new Ally();
			me.context = context;
			me.openid = openid;
			me.ownerid = ownerid;
			me.qrScene = qrScene;
			room = new WaitingRoom(roomKey(), me); 
			me.room = room;
			ally = me;
			current.compareAndSet(null, room);
			logger.info("create waiting room: " + room);
		} else {
			Ally you = new Ally();
			you.context = context;
			you.openid = openid;
			you.ownerid = ownerid;
			you.qrScene = qrScene;
			room.join(you);
			you.room = room;
			ally = you;
		}
		room = current.get();
		active.put(room.key(), room);
		return ally;
	}

	@Override
	public String toString() {
		return "Guard [current=" + current + ", active=" + active + ", service=" + service + ", start=" + start + "]";
	}
	
	
}
