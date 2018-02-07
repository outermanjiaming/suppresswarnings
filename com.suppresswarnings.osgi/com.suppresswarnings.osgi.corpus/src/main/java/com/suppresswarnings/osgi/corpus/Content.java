package com.suppresswarnings.osgi.corpus;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.data.DataService;
import com.suppresswarnings.osgi.data.TTL;

public class Content implements Runnable {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	LinkedBlockingDeque<TTL> ttl = new LinkedBlockingDeque<TTL>(100000);
	Map<String, String> cacheString = new ConcurrentHashMap<String, String>();
	Map<String, byte[]> cacheBytes = new ConcurrentHashMap<String, byte[]>();
	DataService dataService;
	public void set(String name, byte[] bytes) {
		cacheBytes.put(name, bytes);
	}
	public void set(String name, String value) {
		cacheString.put(name, value);
	}
	public void setx(String name, byte[] bytes, long timeToLiveMillis) {
		cacheBytes.put(name, bytes);
		expire(name, timeToLiveMillis);
	}
	public void setx(String name, String value, long timeToLiveMillis) {
		cacheString.put(name, value);
		expire(name, timeToLiveMillis);
	}
	public void expire(String name, long timeToLiveMillis) {
		long now = System.currentTimeMillis();
		TTL e = new TTL(now + timeToLiveMillis, name);
		ttl.add(e);
	}
	
	public void clear(){
		long now = System.currentTimeMillis();
		System.out.println("[content] clean TTL("+ttl.size()+"): " + ttl);
		ttl.removeIf(out -> {
			if(out.ttl() < now) {
				System.out.println("[content] remove key: " + out.key());
				cacheString.remove(out.key());
				cacheBytes.remove(out.key());
				return true;
			}
			return false;
		});
	}
	
	public static void main(String[] args) {
		Content content = new Content();
		content.set("a", new byte[1]);
		content.expire("a", 5000);
		content.set("b", new byte[1]);
		content.expire("b", 3);
		content.setx("c", new byte[1], 10000);
		JFrame frame = new JFrame("content");
		frame.setSize(250, 650);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Vision vision = new Vision(content);
		vision.setOpaque(false);
		frame.getContentPane().add(vision);
		frame.setVisible(true);
		ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
		service.scheduleAtFixedRate(content, 100, 3000, TimeUnit.MILLISECONDS);
		service.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				Random random = new Random();
				int r = random.nextInt(15000);
				content.setx("key" + r, "value"+r, r);
			}
		}, 200, 1400, TimeUnit.MILLISECONDS);
		service.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				frame.repaint();
			}
		}, 300, 100, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void run() {
		long start = System.currentTimeMillis();
		logger.info("[content] run clean start");
		clear();
		logger.info("[content] run clean end: " + (System.currentTimeMillis() - start));
	}
}
