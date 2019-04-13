package com.suppresswarnings.osgi.like;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class LikeService implements HTTPService, CommandProvider {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	long start = System.currentTimeMillis();
	ScheduledExecutorService service = Executors.newScheduledThreadPool(3, new ThreadFactory() {
		AtomicInteger integer = new AtomicInteger(1);
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "LikeService-Thread-" + integer.getAndIncrement());
		}
	});
	
	@Override
	public String getHelp() {
		StringBuffer sb = new StringBuffer();
		sb.append("like list --show all like project");
		return sb.toString();
	}
	
	public void _like(CommandInterpreter ci) {
		String arg = ci.nextArgument();
		logger.info("command like " + arg);
		String ret = arg + " => " + new Date(start).toString() + " -> " + new Date().toString();
		ci.println(ret);
	}

	@Override
	public String getName() {
		return "like.http";
	}

	@Override
	public String start(Parameter parameter) throws Exception {
		logger.info("like request: " + parameter);
		return "like";
	}
	
	String gitpull() throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();
		List<String> commands = new ArrayList<String>();  
		commands.add("git");  
		commands.add("pull");  
		processBuilder.command(commands);  
		processBuilder.directory(new File("/usr/share/nginx/suppresswarnings/com.suppresswarnings.html/src/html"));  
		processBuilder.redirectErrorStream(true);  
		Process process = processBuilder.start();  
		StringBuilder result = new StringBuilder();  
		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));  
		try {  
			String cmd = processBuilder.command().toString();
		    String line;  
		    while ((line = reader.readLine()) != null) {  
		           result.append(line);  
		           logger.info(cmd + " --->: " + line);  
		       }  
		} catch (Exception e) {  
			logger.warn("failed to read output from process", e);  
		} finally {  
		    reader.close();  
		} 
		process.waitFor();  
		int exit = process.exitValue();  
		if (exit != 0) { 
			logger.error("fail to execute command git pull");
		}
		
		return result.toString();  
	}

	public void activate() {
		logger.info("LikeService activate");
		service.scheduleWithFixedDelay(()->{
			try {
				logger.info("start to execute git pull");
				gitpull();
			} catch (Exception e) {
				logger.error("Fail to git pull", e);
			}
		}, 3, 10, TimeUnit.SECONDS);
	}
	public void deactivate() {
		logger.info("[LikeService] deactivate.");
	}
		public void modified() {
			logger.info("[LikeService] modified.");
		}
}
