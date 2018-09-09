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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;

public class WorkHandler {
	public static final String SENDOK = "{\"errcode\":0,\"errmsg\":\"ok\"}";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	CorpusService service;
	String wxid;
	ArrayBlockingQueue<TodoTask> todoTasks;
	ArrayBlockingQueue<WorkerUser> waitUsers;
	ConcurrentHashMap<String, WorkerUser> workers;
	ConcurrentHashMap<String, TodoTask> tasks;
	transient boolean on = true;
	
	public WorkHandler(CorpusService service, String wxid) {
		this.service = service;
		this.wxid = wxid;
		this.todoTasks = new ArrayBlockingQueue<>(100000);
		this.waitUsers = new ArrayBlockingQueue<>(100);
		this.workers = new ConcurrentHashMap<>();
		this.tasks = new ConcurrentHashMap<>();
	}
	
	public boolean lineUp(WorkerUser user) {
		boolean wait = waitUsers.offer(user);
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
	public boolean clockIn(String openid) {
		WorkerUser user = workers.get(openid);
		if(user ==  null) {
			user = new WorkerUser();
			user.setOpenId(openid);
			user.setFree();
			workers.put(openid, user);
		}
		return lineUp(user);
	}
	public boolean done(TodoTask task) {
		TodoTask todo = tasks.get(task.getOpenId());
		if(task.equals(todo)) {
			String result = service.sendTxtTo("online reply", task.getResult(), task.getOpenId());
			if(SENDOK.equals(result)) {
				tasks.remove(task.getOpenId());
				tasks.remove(task.getQuizId());
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
				return true;
			}
		}
		return false;
	}
	public TodoTask want(String openid) {
		try {
			TodoTask todo = todoTasks.poll();
			if(todo == null) {
				WorkerUser user = workers.get(openid);
				if(user == null) {
					user = new WorkerUser();
					user.setOpenId(openid);
					user.setFree();
					workers.put(openid, user);
				} else {
					user.setFree();
				}
				lineUp(user);
			}
			return todo;
		} catch (Exception e) {
			logger.error("[WorkHandler] want InterruptedException", e);
			return null;
		}
	}
	
	public void working() {
		Thread workingThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				int count = 3;
				while(on) {
					try {
						logger.info("[WorkHandler] working to take one user");
						WorkerUser user = waitUsers.take();
						logger.info("[WorkHandler] working got one user: " + user.toString());
						logger.info("[WorkHandler] working to take one task");
						TodoTask todo = todoTasks.take();
						logger.info("[WorkHandler] working got one task: " + todo.toString());
						boolean done = assignJob(user, todo);
						logger.info("[WorkHandler] working task result: " + done);
					} catch (Exception e) {
						logger.error("[WorkHandler] working Exception", e);
						count --;
						if(count < 0) {
							on = false;
							logger.error("[WorkHandler] working lijiaming: Shutdown WorkHandler");
						}
					}
				}
			}
		}, "WorkHandler-Thread");
		workingThread.start();
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
			todoTasks.put(task);
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
	public void oldJob(TodoTask todo) {
		try {
			logger.info("[WorkHandler] user is busy, assignJob put task back");
			todoTasks.put(todo);
		} catch (Exception e) {
			logger.error("[WorkHandler] assignJob put task back Exception", e);
		}
	}
	public boolean assignJob(WorkerUser user, TodoTask todo) {
		if(user.isBusy()) {
			oldJob(todo);
			return false;
		}
		user.setBusy();
		String result = service.sendTxtTo("working", "请回答：\n" + todo.getQuiz(), user.getOpenId());
		if(SENDOK.equals(result)) {
			String fromOpenIdKey = String.join(Const.delimiter, Const.Version.V1, "WXID", "Token", "973rozg");
			String wxid = service.account().get(fromOpenIdKey);
			WorkContext context = new WorkContext(this, todo, wxid, user.getOpenId(), service);
			service.context(user.getOpenId(), context);
			tasks.put(todo.getQuizId(), todo);
			return true;
		} else {
			oldJob(todo);
			lineUp(user);
			return false;
		} 
		
	}
	
	public String report(){
		String report = "todoTasks: " + todoTasks.size() + ", waitUsers: " + waitUsers.size() + ", workers: " + workers.size() + ", on: " + on;
		logger.info("[WorkHandler] report: " + report);
		return report;
	}

	public void forgetIt(String openId) {
		tasks.remove(openId);
	}
}
