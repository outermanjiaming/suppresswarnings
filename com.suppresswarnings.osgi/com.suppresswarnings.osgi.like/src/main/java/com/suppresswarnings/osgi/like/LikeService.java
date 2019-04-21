package com.suppresswarnings.osgi.like;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.like.impl.LikeHandlerImpl;
import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Result;
import com.suppresswarnings.osgi.like.model.User;
import com.suppresswarnings.osgi.like.model.Project;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class LikeService implements HTTPService, CommandProvider {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public Map<String, Provider<?>> providers = new HashMap<>();
	public Gson gson = new Gson();
	private LevelDB account, data, token;
	public LikeHandler handler;
	public Map<String, AtomicInteger> counters;
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
		String action = parameter.getParameter("action");
		if("project".equals(action)) {
			String projectid = parameter.getParameter("projectid");
			String code = parameter.getParameter("code");
			String openid = openid(code);
			projectid = sharedProjectid(projectid, openid);
			Page<Project> page = handler.listProjects(true, 2, projectid, openid);
			Result result = new Result(page);
			Map<String, String> extra = new HashMap<>();
			KeyValue kv = user(openid);
			if(kv.key().equals(fake(openid)) || join(openid)) {
				extra.put("game", "show");
			}
			extra.put("face", kv.value());
			extra.put("uname", kv.key());
			result.setExtra(extra);
			return gson.toJson(result);
		} else if("next".equals(action)) {
			String projectid = parameter.getParameter("projectid");
			String code = parameter.getParameter("code");
			Page<Project> page = handler.listProjects(false, 5, projectid, code);
			Result result = new Result(page);
			return gson.toJson(result);
		} else if("like".equals(action)) {
			String projectid = parameter.getParameter("projectid");
			String code = parameter.getParameter("code");
			if(projectid == null || code == null) {
				return gson.toJson(new Result(500, "projectid == null || code == null"));
			}
			String openid = openid(code);
			String count = handler.likeProject(projectid, openid);
			if(count == null) {
				return gson.toJson(new Result(401, "like again"));
			}
			Result result = new Result(count);
			return gson.toJson(result);
		} else if("comment".equals(action)) {
			String projectid = parameter.getParameter("projectid");
			String code = parameter.getParameter("code");
			String comment = parameter.getParameter("comment");
			if(projectid == null || code == null || comment == null) {
				return gson.toJson(new Result(500, "projectid is null || code is null || comment is null"));
			}
			String openid = openid(code);
			String name = parameter.getParameter("name");
			String id = handler.commentProject(comment, projectid, openid, name);
			Result result = new Result(id);
			return gson.toJson(result);
		} else if("user".equals(action)) {
			String code = parameter.getParameter("code");
			String openid = openid(code);
			User user = handler.myself(openid);
			Result result = new Result(user);
			return gson.toJson(result);
		}
		return gson.toJson(new Result(400, "unknown action"));
	}
	
	public String sharedProjectid(String projectid, String openid) {
		logger.info("sharedProjectid: " + projectid + ", " + openid);
		if(projectid != null && projectid.startsWith("T_Like_Share")) {
			String[] share = projectid.split("\\$");
			logger.info("shared project: " + share[1]);
			if(share.length > 2) {
				String origin = share[1];
				String sharer = share[2];
				String time = "" + System.currentTimeMillis();
				data().put(String.join(Const.delimiter, Const.Version.V2, "Project", "Share", projectid, sharer, openid), time);
				data().put(String.join(Const.delimiter, Const.Version.V2, sharer, "Project", "Share", projectid, openid, time), openid);
				account().put(String.join(Const.delimiter, Const.Version.V2, sharer, "Project", "Share", projectid, openid), time);
				return origin;
			}
		}
		return projectid;
	}
	
	public boolean join(String openid) {
		return openid.equals(account().get(String.join(Const.delimiter, Const.Version.V2, "Join", "Game", "Like", openid)));
	}
	
	public String fake(String openid) {
		int len = openid.length();
		return "未关注" + openid.substring(len - 4, len);
	}
	
	public KeyValue user(String openid) {
		String json = account().get(String.join(Const.delimiter, Const.Version.V1, openid, "User"));
		if(openid == null || json == null) {
			return new KeyValue(fake(openid), "http://suppresswarnings.com/suppresswarnings.jpg");
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> map = gson.fromJson(json, Map.class);
		String nickname = (String) map.get("nickname");
		String headimgurl = (String) map.get("headimgurl");
		KeyValue kv = new KeyValue(nickname, headimgurl);
		return kv;
	}
	
	public String openid(String code) {
		String exist = token().get(String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", code));
		logger.info("code -> openid: " + code + " => " + exist);
		return exist;
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
		           if(line.contains("Already")) {
		        	   logger.debug(cmd + " --->: " + line);
		           } else {
		        	   logger.info(cmd + " --->: " + line);
		           }
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
	public void writeLikes() {
		counters.forEach((projectid,value) -> {
			String countProjectLikeKey = String.join(Const.delimiter, Const.Version.V2, "Project", "LikeCount", projectid);
			data().put(countProjectLikeKey, "" + value.get());
		});
	}
	public void activate() {
		logger.info("LikeService activate");
		handler = new LikeHandlerImpl(this);
		counters = new ConcurrentHashMap<>();
		
		service.scheduleWithFixedDelay(()->{
			try {
				logger.info("start to execute git pull");
				gitpull();
				logger.info("start to write likes");
				writeLikes();
			} catch (Exception e) {
				logger.error("Fail to git pull", e);
			}
		}, 3, 10, TimeUnit.SECONDS);
		service.execute(() ->{
			try {
				String head = String.join(Const.delimiter, Const.Version.V2, "Projectid");
				String start = String.join(Const.delimiter, Const.Version.V2, "Projectid", "Project");
				List<String> projectids = new ArrayList<>();
				logger.info("start = " + start);
				account().page(head, start, null, Integer.MAX_VALUE, (k,v) ->{
					projectids.add(v);
				});
				projectids.forEach(projectid ->{
					initLike(projectid);
				});
			} catch (Exception e) {
				logger.error("fail to get counters from leveldb", e);
			}
		});
	}
	public void initLike(String projectid) {
		if(!counters.containsKey(projectid)) {
			String countProjectLikeKey = String.join(Const.delimiter, Const.Version.V2, "Project", "LikeCount", projectid);
			String count = data().get(countProjectLikeKey);
			int initialValue = 1;
			AtomicInteger value = new AtomicInteger(initialValue);
			String head = String.join(Const.delimiter, Const.Version.V2, "Project", "Like", projectid);
			data().page(head, head, null, Integer.MAX_VALUE, (k,v)->{
				value.getAndIncrement();
			});
			if(count == null) {
				data().put(countProjectLikeKey, "" + value.get());
				logger.info("first time initialValue = " + value.get());
				counters.put(projectid, value);
			} else {
				initialValue = Integer.valueOf(count);
				counters.put(projectid, new AtomicInteger(Math.max(initialValue, value.get())));
			}
			
		}
	}
	public int like(String project) {
		initLike(project);
		AtomicInteger count = counters.get(project);
		return count.incrementAndGet();
	}
	
	@Deprecated
	public int dislike(String project) {
		initLike(project);
		AtomicInteger count = counters.get(project);
		return count.decrementAndGet();
	}
	public void deactivate() {
		logger.info("[LikeService] deactivate.");
		service.shutdown();
	}
	public void modified() {
		logger.info("[LikeService] modified.");
	}
	
	public void provide(Provider<?> provider) {
		logger.info("provider: " + provider.description());
		String id = provider.identity();
		Object instance = provider.instance();
		Provider<?> old = providers.put(id, provider);
		logger.info("put new provider: " + instance + " replace if exists: " + old);
	}
	public void clearProvider(Provider<?> provider) {
		logger.info("clear provider: " + provider.description());
		String id = provider.identity();
		Object instance = provider.instance();
		boolean b = providers.remove(id, provider);
		logger.info("remove instance: " + instance + " if found: " + b);
	}
	public LevelDB account(){
		if(account != null) {
			return account;
		}
		account = getOrDefault("Account");
		return account;
	}
	public LevelDB data(){
		if(data != null) {
			return data;
		}
		data = getOrDefault("Data");
		return data;
	}
	public LevelDB token(){
		if(token != null) {
			return token;
		}
		token = getOrDefault("Token");
		return token;
	}
	public LevelDB getOrDefault(String key) {
		logger.info("[LikeService] get leveldb: " + key);
		Provider<?> provider = providers.get(key);
		if(provider == null) {
			logger.error("[LikeService] get null from providers by key: " + key);
			return null;
		}
		LevelDB instance = (LevelDB) provider.instance();
		return instance;
	}
}
