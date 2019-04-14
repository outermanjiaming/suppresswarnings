package com.suppresswarnings.corpus.service.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.work.Quiz;

public class Guard implements Runnable {
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
	
	public synchronized Ally joinOrCreateWaitingRoom(String openid, String ownerid, String qrScene) {
		WaitingRoom room = current.get();
		Ally ally;
		if(room == null) {
			List<Quiz> quiz = service.getQuiz(3);
			Ally me = new Ally();
			me.openid = openid;
			me.ownerid = ownerid;
			me.qrScene = qrScene;
			room = new WaitingRoom(roomKey(), me, quiz); 
			me.setRoom(room);
			ally = me;
			current.compareAndSet(null, room);
			logger.info("create waiting room: " + room);
		} else {
			Ally you = new Ally();
			you.openid = openid;
			you.ownerid = ownerid;
			you.qrScene = qrScene;
			if(you.equals(room.me)) {
				logger.info("only one ally: " + room);
				ally = room.me;
			} else {
				room.join(you);
				you.setRoom(room);
				ally = you;
				current.compareAndSet(room, null);
				logger.info("join waiting room: " + room);
			}
		}
		active.put(room.key(), room);
		return ally;
	}

	@Override
	public String toString() {
		return "Guard [current=" + current + ", active=" + active + ", service=" + service + ", start=" + start + "]";
	}

	@Override
	public void run() {
		logger.info("guard schedule every 10s to check waiting room finished and remove completed ones");
		List<String> remove = new ArrayList<>();
		active.forEach((key, room) ->{
			try {
				if(room.finish(service)) {
					if(room.finish()) {
						remove.add(key);
						logger.info("to remove room " + key);
					} else {
						logger.info("3min after finish will be cleared");
					}
				}
			} catch (Exception e) {
				logger.error("检查活跃房间时异常", e);
			}
		});
		remove.forEach(key -> {
			try {
				WaitingRoom room = active.remove(key);
				room.clear();
			} catch (Exception e) {
				logger.error("删除完成房间时异常", e);
			}
		});
	}
	
}
