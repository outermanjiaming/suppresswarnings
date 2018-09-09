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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TodoTask {
	String openId;
	String quizId;
	String quiz;
	String result;
	long time;
	long finishTime;
	transient int state = 0;
	ReentrantLock lock;
	Condition waiting;
	
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public long getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public String getQuizId() {
		return quizId;
	}
	public void setQuizId(String quizId) {
		this.quizId = quizId;
	}
	public String getQuiz() {
		return quiz;
	}
	public void setQuiz(String quiz) {
		this.quiz = quiz;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public Condition getWaiting() {
		return waiting;
	}
	public void setWaiting(Condition waiting) {
		this.waiting = waiting;
	}
	public ReentrantLock getLock() {
		return lock;
	}
	public void setLock(ReentrantLock lock) {
		this.lock = lock;
	}
	public void finish(String reply) {
		this.finishTime = System.currentTimeMillis();
		this.result = reply;
		this.state = 1;
		lock.lock();
		try {
			waiting.signalAll();
		} finally {
			lock.unlock();
		}
	}
	public boolean isFinish() {
		return this.state == 1;
	}
	@Override
	public String toString() {
		return "TodoTask [openId=" + openId + ", quizId=" + quizId + ", quiz=" + quiz + ", time=" + time + ", waiting=" + waiting + "]";
	}
	
}
