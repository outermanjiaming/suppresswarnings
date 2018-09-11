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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Type;

public class WorkCommand implements Runnable {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	ArrayBlockingQueue<TodoTask> todos;
	ArrayBlockingQueue<WorkerUser> users;
	ConcurrentHashMap<String, WorkerUser> workers;
	ConcurrentHashMap<String, TodoTask> tasks;
	Type type;
	AtomicBoolean on;
	WorkHandler handler;
	public WorkCommand(){}
	public WorkCommand(WorkHandler handler,Type type, ArrayBlockingQueue<TodoTask> todos, ArrayBlockingQueue<WorkerUser> users, ConcurrentHashMap<String, WorkerUser> workers, ConcurrentHashMap<String, TodoTask> tasks, AtomicBoolean on) {
		this.handler = handler;
		this.type = type;
		this.todos = todos;
		this.users = users;
		this.workers = workers;
		this.tasks = tasks;
		this.on = on;
	}
	
	@Override
	public void run() {
		logger.info("[WorkCommand] start working... " + type.name());
		while (on.get()) {
			try {
				logger.info("[WorkCommand] working to take one user");
				WorkerUser user = users.take();
				logger.info("[WorkCommand] working got one user: " + user.toString());
				logger.info("[WorkCommand] working to take one task");
				TodoTask todo = todos.take();
				logger.info("[WorkCommand] working got one task: " + todo.toString());
				boolean done = handler.assignJob(user, todo);
				logger.info("[WorkCommand] working task result: " + done);
			} catch (Exception e) {
				logger.error("[WorkCommand] Exception", e);
			}
		}
		logger.info("[WorkCommand] done working... " + type.name());
	}

}
