/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.work;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
	String openid;
	AtomicInteger quizCounter;
	AtomicInteger replyCounter;
	AtomicInteger similarCounter;
	AtomicInteger existCounter;
	long firstTime;
	long lastTime;
	HashSet<String> texts;
	long now = System.currentTimeMillis();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Counter(String openid) {
		this.openid = openid;
		this.quizCounter = new AtomicInteger(0);
		this.replyCounter = new AtomicInteger(0);
		this.similarCounter = new AtomicInteger(0);
		this.existCounter = new AtomicInteger(0);
		this.firstTime = now;
		this.lastTime = now;
		this.texts = new HashSet<>();
	}
	
	public void quiz(long that, String text) {
		increase(quizCounter, that, text);
	}
	
	public void reply(long that, String text) {
		increase(replyCounter, that, text);
	}
	
	public void similar(long that, String text) {
		increase(similarCounter, that, text);
	}
	
	public int increase(AtomicInteger counter, long time, String text) {
		if(time > lastTime) lastTime = time;
		if(time < firstTime) firstTime = time;
		
		if(!texts.add(text)) {
			existCounter.incrementAndGet();
		}
		return counter.incrementAndGet();
	}
	
	public String report(){
		StringBuffer sb = new StringBuffer();
		sb.append("\n最近一次：").append(format.format(new Date(lastTime)));
		sb.append("\n重复数量：").append(existCounter.get());
		sb.append("\n提问数量：").append(quizCounter.get());
		sb.append("\n回答数量：").append(replyCounter.get());
		sb.append("\n同义句数量：").append(similarCounter.get());
		sb.append("\n总计：").append(sum() + ", 重复度：" + repetition());
		return sb.toString();
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public AtomicInteger getQuizCounter() {
		return quizCounter;
	}

	public void setQuizCounter(AtomicInteger quizCounter) {
		this.quizCounter = quizCounter;
	}

	public AtomicInteger getReplyCounter() {
		return replyCounter;
	}

	public void setReplyCounter(AtomicInteger replyCounter) {
		this.replyCounter = replyCounter;
	}

	public AtomicInteger getSimilarCounter() {
		return similarCounter;
	}

	public void setSimilarCounter(AtomicInteger similarCounter) {
		this.similarCounter = similarCounter;
	}

	public AtomicInteger getExistCounter() {
		return existCounter;
	}

	public void setExistCounter(AtomicInteger existCounter) {
		this.existCounter = existCounter;
	}

	public long getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(long firstTime) {
		this.firstTime = firstTime;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public HashSet<String> getTexts() {
		return texts;
	}

	public void setTexts(HashSet<String> texts) {
		this.texts = texts;
	}
	
	public int sum(){
		return quizCounter.get() + replyCounter.get() + similarCounter.get();
	}
	
	public float repetition(){
		float exist = existCounter.floatValue();
		float sum = sum();
		return exist / sum;
	}
}
