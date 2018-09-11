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

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.CorpusService;

public class WorkHandler {
	public static final String SENDOK = "{\"errcode\":0,\"errmsg\":\"ok\"}";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	CorpusService service;
	String wxid;
	ArrayBlockingQueue<TodoTask> similarTasks;
	ArrayBlockingQueue<TodoTask> replyTasks;
	ArrayBlockingQueue<WorkerUser> replyUsers;
	ArrayBlockingQueue<WorkerUser> similarUsers;
	ConcurrentHashMap<String, WorkerUser> workers;
	ConcurrentHashMap<String, TodoTask> tasks;
	ExecutorService executor;
	AtomicBoolean on = new AtomicBoolean(true);
	
	public WorkHandler(CorpusService service, String wxid) {
		this.service = service;
		this.wxid = wxid;
		this.replyTasks   = new ArrayBlockingQueue<>(100000);
		this.similarTasks = new ArrayBlockingQueue<>(100000);
		this.replyUsers   = new ArrayBlockingQueue<>(10000);
		this.similarUsers = new ArrayBlockingQueue<>(10000);
		this.workers  = new ConcurrentHashMap<>();
		this.tasks    = new ConcurrentHashMap<>();
	}
	
	public boolean lineUp(WorkerUser user) {
		boolean wait = false;
		if(user.getType() == Type.Reply){
			wait = replyUsers.offer(user);
		} else {
			wait = similarUsers.offer(user);
		}
		logger.info("[WorkHandler] want user: " + user.toString() + " is free and wait here: " + wait);
		return wait;
	}
	public boolean clockOut(String openid) {
		WorkerUser user = workers.get(openid);
		if(user == null) {
			return true;
		}
		user.setBusy();
		workers.remove(openid);
		return true;
	}
	public boolean clockIn(String openid, Type type) {
		WorkerUser user = workers.get(openid);
		if(user ==  null) {
			user = new WorkerUser();
			user.setOpenId(openid);
			workers.put(openid, user);
		}
		user.setFree();
		user.setType(type);
		return lineUp(user);
	}
	public boolean done(TodoTask task, Type type) {
		if(type == Type.Reply) {
			String reply = CheckUtil.cleanStr(task.getQuiz());
			String aid = service.questionToAid.get(reply);
			if(aid == null) {
				service.questionToAid.put(reply, task.getQuizId());
			} else {
				HashSet<String> hashSet = service.aidToAnswers.get(aid);
				if(hashSet == null) {
					hashSet = new HashSet<>();
					service.aidToAnswers.put(aid, hashSet);
				}
				hashSet.add(task.getResult());
			}
		} else if(type == Type.Similar) {
			//TODO maybe bug, like the quiz has its replys already
			String similar = task.getResult();
			String quiz = CheckUtil.cleanStr(similar);
			String aid = service.questionToAid.get(quiz);
			if(aid == null) {
				service.questionToAid.put(quiz, task.getQuizId());
				return false;
			} else {
				HashSet<String> hashSet = service.aidToAnswers.get(aid);
				if(hashSet == null) {
					hashSet = new HashSet<>();
					service.aidToAnswers.put(aid, hashSet);
					return false;
				} else {
					Iterator<String> iter = hashSet.iterator();
					if(iter.hasNext()) {
						String answer = iter.next();
						task.setResult(answer);
					} else {
						return false;
					}
				}
			}
		}
		
		TodoTask todo = tasks.get(task.getOpenId());
		if(task.equals(todo)) {
			long time = todo.getTime();
			if(System.currentTimeMillis() - time > TimeUnit.MINUTES.toMillis(3)) {
				logger.info("[WorkHandler done] it's too late: " + task.toString());
				return false;
			}
			String result = service.sendTxtTo("online reply", task.getResult(), task.getOpenId());
			if(SENDOK.equals(result)) {
				return true;
			}
		}
		tasks.remove(task.getOpenId());
		tasks.remove(task.getQuizId());
		return false;
	}
	
	public TodoTask want(WorkerUser worker) {
		try {
			TodoTask todo = null;
			if(worker.getType() == Type.Reply){
				todo = replyTasks.poll();
			} else {
				todo = similarTasks.poll();
			}
			if(todo == null) {
				worker.setFree();
				lineUp(worker);
			}
			return todo;
		} catch (Exception e) {
			logger.error("[WorkHandler] want InterruptedException", e);
			return null;
		}
	}
	
	public void working() {
		on.set(true);
		executor = Executors.newFixedThreadPool(2);
		WorkCommand reply = new WorkCommand(this, Type.Reply, replyTasks, replyUsers, workers, tasks, on);
		WorkCommand similar = new WorkCommand(this, Type.Reply, similarTasks, similarUsers, workers, tasks, on);
		executor.execute(reply);
		executor.execute(similar);
	}
	
	public void close() {
		logger.info("[WorkHandler] closing");
		on.set(false);
		executor.shutdownNow();
		replyTasks.clear();
		similarTasks.clear();
		replyUsers.clear();
		similarUsers.clear();
		workers.clear();
		tasks.clear();
		logger.info("[WorkHandler] closed");
	}
	public void batchJob(String quiz, String quizId, Type typeNullForBoth) {
		TodoTask task = new TodoTask();
		task.setOpenId("");
		task.setQuiz(quiz);
		task.setQuizId(quizId);
		task.setTime(System.currentTimeMillis());
		try {
			if(typeNullForBoth == null) {
				replyTasks.put(task);
				similarTasks.put(task);
			} else if(typeNullForBoth == Type.Reply) {
				replyTasks.put(task);
			} else if(typeNullForBoth == Type.Similar) {
				similarTasks.put(task);
			}
		} catch (Exception e) {
			logger.error("[WorkHandler] newJob Exception", e);
		}
	}
	public String newJob(String quiz, String quizId, String openId) {
		TodoTask task = tasks.get(quizId);
		if(task == null) {
			task = new TodoTask();
			task.setOpenId(openId);
			task.setQuiz(quiz);
			task.setQuizId(quizId);
			task.setTime(System.currentTimeMillis());
			ReentrantLock lock = new ReentrantLock();
			Condition waiting = lock.newCondition();
			task.setLock(lock);
			task.setWaiting(waiting);
			//new task into map
			tasks.put(quizId, task);
		}
		tasks.put(openId, task);
		try {
			replyTasks.put(task);
			similarTasks.put(task);
		} catch (Exception e) {
			logger.error("[WorkHandler] newJob Exception", e);
		}
		ReentrantLock lock = task.getLock();
		lock.lock();
		try {
			task.getWaiting().await(4, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.error("[WorkHandler] new job waiting Exception", e);
		} finally {
			lock.unlock();
		}
		if(task.isFinish()) {
			tasks.remove(quizId);
			tasks.remove(openId);
			return task.getResult();
		}
		return null;
	}
	
	public void oldJob(TodoTask todo, Type type) {
		try {
			logger.info("[WorkHandler] user is busy, oldJob put task back");
			if(type == Type.Reply) {
				replyTasks.put(todo);
			} else if(type == Type.Similar) {
				similarTasks.put(todo);
			}
			logger.info("[WorkHandler] put back to: " + type);
		} catch (Exception e) {
			logger.error("[WorkHandler] oldJob put task back Exception", e);
		}
	}
	
	public boolean assignJob(WorkerUser user, TodoTask todo) {
		if(user.isBusy()) {
			oldJob(todo, user.type);
			return false;
		}
		user.setBusy();
		String type = user.getType() == Type.Reply ? "请回答：\n" : "同义句：\n";
		String result = service.sendTxtTo("working", type + todo.getQuiz(), user.getOpenId());
		if(SENDOK.equals(result)) {
			String fromOpenIdKey = String.join(Const.delimiter, Const.Version.V1, "WXID", "Token", "973rozg");
			String wxid = service.account().get(fromOpenIdKey);
			WorkContext context = new WorkContext(this, todo, user, wxid, service);
			service.context(user.getOpenId(), context);
			tasks.put(todo.getQuizId(), todo);
			return true;
		} else {
			oldJob(todo, user.type);
			lineUp(user);
			return false;
		} 
		
	}
	
	public String report(){
		String report = "replyTasks: " + replyTasks.size() + "similarTasks: " + similarTasks.size() + ", replyUsers: " + replyUsers.size() + ", similarUsers: " + similarUsers.size() + ", workers: " + workers.size() + ", on: " + on;
		logger.info("[WorkHandler] report: " + report);
		return report;
	}

	public void forgetIt(String openId) {
		tasks.remove(openId);
	}
}
