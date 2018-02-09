package com.suppresswarnings.osgi.corpus;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.data.AbsContent;
import com.suppresswarnings.osgi.data.DataService;
import com.suppresswarnings.osgi.data.TTL;

/**
 * Content with TTL
 * @author lijiaming
 *
 */
public class Content extends AbsContent implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8841351499573607963L;
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	LinkedBlockingQueue<TTL> ttl = new LinkedBlockingQueue<TTL>(100000);
	Map<String, Context<?>> contexts = new ConcurrentHashMap<String, Context<?>>();
	Map<String, String> cacheString = new ConcurrentHashMap<String, String>();
	Map<String, byte[]> cacheBytes = new ConcurrentHashMap<String, byte[]>();
	Map<String, TTL> keepAlive = new ConcurrentHashMap<String, TTL>();
	DataService dataService;
	public void set(String name, byte[] bytes) {
		cacheBytes.put(name, bytes);
	}
	public void set(String name, String value) {
		cacheString.put(name, value);
	}
	public void setx(String name, byte[] bytes, long timeToLiveMillis) {
		expire(name, timeToLiveMillis);
		cacheBytes.put(name, bytes);
	}
	public void setx(String name, String value, long timeToLiveMillis) {
		expire(name, timeToLiveMillis);
		cacheString.put(name, value);
	}
	private void expire(String name, long timeToLiveMillis) {
		long now = System.currentTimeMillis();
		TTL e = new TTL(now + timeToLiveMillis, name);
		TTL old = keepAlive.remove(name);
		if(old != null) {
			if(old.marked()) {
				ttl.remove(old);
				ttl.offer(e);
			} else {
				if(old.ttl() < e.ttl()) {
					ttl.remove(old);
					ttl.offer(e);
				}
				//don't offer this TTL since it is still short than old one
			}
		} else {
			ttl.offer(e);
		}
	}
	
	public void clear(){
		long now = System.currentTimeMillis();
		logger.info("[content] clean TTL("+ttl.size()+"): " + ttl);
		ttl.removeIf(out -> {
			if(out.ttl() < now) {
				if(out.marked()) {
					logger.info("[content] remove key: " + out.key());
					cacheString.remove(out.key());
					cacheBytes.remove(out.key());
					keepAlive.remove(out.key());
					return true;
				} else {
					out.mark();
					keepAlive.put(out.key(), out);
				}
			}
			return false;
		});
		logger.info("[content] clean TTL("+ttl.size()+"): " + ttl);
	}
	
	public Context<?> get(String openid) {
		return contexts.get(openid);
	}
	public void put(String openid, Context<?> context) {
		contexts.put(openid, context);
	}
	
	public void init(){
		
	}
	public static void main(String[] args) {
		Content content = new Content();
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
				int r = random.nextInt(5000);
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
