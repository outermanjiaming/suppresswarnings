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
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class WorkHandler {
	public static final String SENDOK = "{\"errcode\":0,\"errmsg\":\"ok\"}";
	Random random = new Random();
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	CorpusService service;
	String wxid;
	LinkedBlockingDeque<TodoTask> similarTasks;
	LinkedBlockingDeque<TodoTask> replyTasks;
	LinkedBlockingDeque<WorkerUser> replyUsers;
	LinkedBlockingDeque<WorkerUser> similarUsers;
	ConcurrentHashMap<String, WorkerUser> workers;
	ConcurrentHashMap<String, TodoTask> tasks;
	public Map<String, Long> informs;
	ExecutorService executor;
	AtomicBoolean on = new AtomicBoolean(true);
	
	public WorkHandler(CorpusService service, String wxid) {
		this.service = service;
		this.wxid = wxid;
	}
	
	public boolean lineUp(WorkerUser user) {
		boolean wait = false;
		if(user.getType() == Type.Reply){
			wait = replyUsers.offer(user);
		} else {
			wait = similarUsers.offer(user);
		}
		logger.info("[WorkHandler] want user: " + user.toString() + " is free and wait here: " + wait + ", type: " + user.getType().name());
		return wait;
	}
	public boolean clockOut(String openid) {
		WorkerUser user = workers.get(openid);
		if(user == null) {
			logger.info("[WorkHandler] user has already off work");
			return true;
		}
		user.setBusy();
		workers.remove(openid);
		logger.info("[WorkHandler] user is off work now");
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
	public boolean done(TodoTask task, Type type, Counter counter) {
		long now = System.currentTimeMillis();
		long time = task.getTime();
		String result = task.getResult();
		
		if(type == Type.Reply) {
			counter.reply(now, result);
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
			counter.reply(now, task.getResult());
			String quiz = CheckUtil.cleanStr(result);
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
			//it is the same last one, reply immediately
			String ret = "";
			if(now - time > TimeUnit.MINUTES.toMillis(10)) {
				logger.info("[WorkHandler done] it's too late: " + task.toString());
				ret = service.sendTxtTo("online reply " + task.getOpenId(), "sorry久等了，\n" + task.getResult(), task.getOpenId());
				return false;
			} else {
				ret = service.sendTxtTo("online reply " + task.getOpenId(), task.getResult(), task.getOpenId());
			}
			
			if(SENDOK.equals(ret)) {
				return true;
			}
		} else {
			//TODO lijiaming: just send reply to them
			service.sendTxtTo("online reply", task.getResult(), task.getOpenId());
		}
		tasks.remove(task.getOpenId());
		tasks.remove(task.getQuizId());
		return false;
	}
	
	/**
	 * pollLast is the key
	 * @param worker
	 * @return
	 */
	public TodoTask want(WorkerUser worker) {
		try {
			TodoTask todo = null;
			if(worker.getType() == Type.Reply){
				todo = replyTasks.pollLast();
			} else {
				todo = similarTasks.pollLast();
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
		logger.info("[WorkHandler] working start");
		on.set(true);
		executor = Executors.newFixedThreadPool(12);
		replyTasks   = new LinkedBlockingDeque<>(1000000);
		similarTasks = new LinkedBlockingDeque<>(1000000);
		replyUsers   = new LinkedBlockingDeque<>(10000);
		similarUsers = new LinkedBlockingDeque<>(10000);
		workers  = new ConcurrentHashMap<>();
		tasks    = new ConcurrentHashMap<>();
		informs = new ConcurrentHashMap<String, Long>();
		WorkCommand reply = new WorkCommand(this, Type.Reply, replyTasks, replyUsers, workers, tasks, on);
		WorkCommand similar = new WorkCommand(this, Type.Similar, similarTasks, similarUsers, workers, tasks, on);
		for(int i=0;i<5;i++) {
			executor.execute(reply);
		}
		for(int i=0;i<5;i++) {
			executor.execute(similar);
		}
		logger.info("[WorkHandler] working ready");
	}
	
	public void close() {
		if(!on.get()) {
			logger.info("[WorkHandler] closed already");
			return;
		}
		logger.info("[WorkHandler] closing");
		on.set(false);
		executor.shutdownNow();
		replyTasks.clear();
		similarTasks.clear();
		replyUsers.clear();
		similarUsers.clear();
		workers.clear();
		tasks.clear();
		informs.clear();
		logger.info("[WorkHandler] closed");
	}
	
	public void batchJob(String quiz, String quizId, Type typeNullForBoth) {
		TodoTask task = new TodoTask();
		task.setOpenId("");
		task.setQuiz(quiz);
		task.setQuizId(quizId);
		task.setTime(0);
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
		String result = service.sendTxtTo("working " + user.getOpenId(), type + todo.getQuiz(), user.getOpenId());
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
		String report = "等待回复: " + replyTasks.size() + "\n剩余同义句: " + similarTasks.size() + "\n空闲用户-回复: " + replyUsers.size() + "\n空闲用户-同义句: " + similarUsers.size() + "\n正在工作用户: " + workers.size() + ", 运行中: " + on;
		logger.info("[WorkHandler] report: " + report);
		return report;
	}

	public void forgetIt(String openId) {
		if(tasks != null) {
			TodoTask task = tasks.remove(openId);
			if(task != null) tasks.remove(task.getQuizId());
		}
	}

	public int informUsersExcept(String message, Map<String, WXuser> users) {
		Set<String> set = workers.keySet();
		AtomicInteger count = new AtomicInteger(0);
		users.forEach((openid, user) ->{
			if(set.contains(openid)) {
				logger.info("[corpus] inform users except: " + openid);
			} else {
				
				Long lasttime = informs.get(openid);
				
				boolean need = false;
				if(lasttime == null) {
					lasttime = System.currentTimeMillis();
					informs.put(openid, lasttime);
					need = true;
				}
				
				if(System.currentTimeMillis() - lasttime > TimeUnit.HOURS.toMillis(1)) {
					need = true;
				}
				
				if(need) {
					count.incrementAndGet();
					String ret = service.sendTxtTo("Inform Users " + openid, message, openid);
					logger.info("[corpus] inform this user: " + openid + ", ret: " + ret);
				}
			}
		});
		return count.get();
	}
}
