/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.exam;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.work.TodoTask;
import com.suppresswarnings.corpus.service.work.WorkerUser;

public class ExamHandler {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	CorpusService service;
	public Map<String, String> questionToAid = new ConcurrentHashMap<>();
	public Map<String, HashSet<String>> aidToAnswers = new ConcurrentHashMap<>();
	public Map<String, HashSet<String>> aidToSimilars = new ConcurrentHashMap<>();
	ArrayBlockingQueue<TodoTask> similarTasks;
	ArrayBlockingQueue<TodoTask> replyTasks;
	ArrayBlockingQueue<WorkerUser> replyUsers;
	ArrayBlockingQueue<WorkerUser> similarUsers;
	ConcurrentHashMap<String, WorkerUser> workers;
	ConcurrentHashMap<String, TodoTask> tasks;
	AtomicBoolean on = new AtomicBoolean(true);
	
	public ExamHandler(CorpusService service, String wxid) {
		this.service = service;
		this.replyTasks   = new ArrayBlockingQueue<>(100000);
		this.similarTasks = new ArrayBlockingQueue<>(100000);
		this.replyUsers   = new ArrayBlockingQueue<>(10000);
		this.similarUsers = new ArrayBlockingQueue<>(10000);
		this.workers  = new ConcurrentHashMap<>();
		this.tasks    = new ConcurrentHashMap<>();
	}
	
	public boolean done(TodoTask task, Type type) {
		if(type == Type.Reply) {
			String reply = CheckUtil.cleanStr(task.getQuiz());
			String aid = questionToAid.get(reply);
			if(aid == null) {
				questionToAid.put(reply, task.getQuizId());
			} else {
				HashSet<String> hashSet = aidToAnswers.get(aid);
				if(hashSet == null) {
					hashSet = new HashSet<>();
					aidToAnswers.put(aid, hashSet);
				}
				hashSet.add(task.getResult());
			}
		} else if(type == Type.Similar) {
			String similar = task.getResult();
			String quiz = CheckUtil.cleanStr(similar);
			String aid = questionToAid.get(quiz);
			if(aid == null) {
				questionToAid.put(quiz, task.getQuizId());
				return false;
			} else {
				HashSet<String> hashSet = aidToAnswers.get(aid);
				if(hashSet == null) {
					hashSet = new HashSet<>();
					aidToAnswers.put(aid, hashSet);
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
		
		tasks.remove(task.getOpenId());
		tasks.remove(task.getQuizId());
		return true;
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
			}
			return todo;
		} catch (Exception e) {
			logger.error("[ExamHandler] want InterruptedException", e);
			return null;
		}
	}
	
	
	public void close() {
		logger.info("[ExamHandler] closing");
		on.set(false);
		replyTasks.clear();
		similarTasks.clear();
		replyUsers.clear();
		similarUsers.clear();
		workers.clear();
		tasks.clear();
		logger.info("[ExamHandler] closed");
	}

	public String newJob(String quiz, String quizId, String openId) {
		TodoTask task = tasks.get(quizId);
		if(task == null) {
			task = new TodoTask();
			task.setOpenId(openId);
			task.setQuiz(quiz);
			task.setQuizId(quizId);
			task.setTime(System.currentTimeMillis());
			//new task into map
			tasks.put(quizId, task);
		}
		tasks.put(openId, task);
		try {
			replyTasks.put(task);
			similarTasks.put(task);
		} catch (Exception e) {
			logger.error("[ExamHandler] newJob Exception", e);
		}
		if(task.isFinish()) {
			tasks.remove(quizId);
			tasks.remove(openId);
			return task.getResult();
		}
		return null;
	}
	
	public String report(){
		String report = "replyTasks: " + replyTasks.size() + "similarTasks: " + similarTasks.size() + ", replyUsers: " + replyUsers.size() + ", similarUsers: " + similarUsers.size() + ", workers: " + workers.size() + ", on: " + on;
		logger.info("[ExamHandler] report: " + report);
		return report;
	}
}
