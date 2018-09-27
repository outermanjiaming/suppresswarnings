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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.work.Quiz;
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
	public List<Quiz> assimilatedQuiz = new ArrayList<>();
	long lastTime = System.currentTimeMillis();
	
	public ExamHandler(CorpusService service, String wxid) {
		this.service = service;
		this.replyTasks   = new ArrayBlockingQueue<>(100000);
		this.similarTasks = new ArrayBlockingQueue<>(100000);
		this.replyUsers   = new ArrayBlockingQueue<>(10000);
		this.similarUsers = new ArrayBlockingQueue<>(10000);
		this.workers  = new ConcurrentHashMap<>();
		this.tasks    = new ConcurrentHashMap<>();
	}
	public List<Quiz> allQuiz(){
		String start = String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus","Quiz", "T_Corpus_classExam_1536649615642_3131", "Answer");
		
		List<Quiz> allQuiz = new ArrayList<>();
		service.data().page(start, start, null, Integer.MAX_VALUE, (t, u) -> {
			String left = t.substring(start.length());
			if(!left.contains("Similar") && !left.contains("Reply")) {
				Quiz quiz = new Quiz(t, u);
				allQuiz.add(quiz);
			}
		});
		//fill the reply and similar into each quiz
		allQuiz.forEach(quiz -> {
			
			String replyKey = String.join(Const.delimiter, quiz.getQuiz().key(), "Reply");
			service.data().page(replyKey, replyKey, null, Integer.MAX_VALUE, (t, u) -> {
				String left = t.substring(replyKey.length());
				if(!left.contains("Similar") && !left.contains("Reply")) {
					quiz.reply(t, u);
				}
			});
			
			String similarKey = String.join(Const.delimiter, quiz.getQuiz().key(), "Similar");
			service.data().page(similarKey, similarKey, null, Integer.MAX_VALUE, (t, u) -> {
				String left = t.substring(similarKey.length());
				if(!left.contains("Similar") && !left.contains("Reply")) {
					quiz.similar(t, u);
				}
			});
		});
		
		assimilatedQuiz.clear();
		//assimilate quiz
		allQuiz.forEach(quiz -> {
			boolean assimilated = false;
			for(int i=0;i<assimilatedQuiz.size();i++) {
				Quiz host = assimilatedQuiz.get(i);
				if(host.assimilate(quiz)) {
					logger.info("[fillQuestionsAndAnswers] assimilate: " + host.toString());
					assimilated = true;
					break;
				}
			}
			if(!assimilated) {
				assimilatedQuiz.add(quiz);
			}
		});
		logger.info("[fillQuestionsAndAnswers] done assimilate");
		Collections.shuffle(assimilatedQuiz);
		logger.info("[fillQuestionsAndAnswers] shuffle assimilate");
		assimilatedQuiz.forEach(quiz -> {
			logger.info("[fillQuestionsAndAnswers] assimilated quiz: " + quiz.toString());
			this.questionToAid.put(CheckUtil.cleanStr(quiz.getQuiz().value()), quiz.getQuiz().key());
			
			HashSet<String> answers = new HashSet<>();
			quiz.getReply().forEach(reply -> {
				answers.add(reply.value());
			});
			
			this.aidToAnswers.put(quiz.getQuiz().key(), answers);
			
			HashSet<String> similars = new HashSet<>();
			quiz.getSimilar().forEach(similar -> {
				String value = similar.value();
				similars.add(value);
				this.questionToAid.put(CheckUtil.cleanStr(value), quiz.getQuiz().key());
			});
			
			this.aidToSimilars.put(quiz.getQuiz().key(), similars);
		});
		
		return assimilatedQuiz;
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
