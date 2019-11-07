package com.suppresswarnings.osgi.like;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.suppresswarnings.osgi.like.impl.DrawHandlerImpl;
import com.suppresswarnings.osgi.like.impl.LikeHandlerImpl;
import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Result;
import com.suppresswarnings.osgi.like.model.User;
import com.suppresswarnings.osgi.like.model.Project;
import com.suppresswarnings.osgi.like.model.Quiz;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class LikeService implements HTTPService, CommandProvider {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public Map<String, Provider<?>> providers = new HashMap<>();
	public Gson gson = new Gson();
	private LevelDB account, data, token;
	public LikeHandler handler;
	public DrawHandler drawHandler;
	public Map<String, AtomicInteger> counters;
	public static final long STOP_THE_WORLD = 2000000000000l;
	public static final String STUPID = "oAAug4oI8JS7hk6_LXyzctHx9efM";
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
	public boolean checkVIP(String appid, String code, String openid) {
		logger.info("amivip ? " + appid + " " + code + " " + openid);
		String vipKey = String.join(Const.delimiter, Const.Version.V2, "VIP", "ExpireAt", openid);
		String vip = account().get(vipKey);
		if(isNull(vip)) {
			return false;
		} else {
			try {
				long expiredAt = Long.parseLong(vip);
				if(expiredAt - System.currentTimeMillis() > 0) {
					return true;
				}
			} catch(Exception e) {
				logger.info("error while check vip");
			}
			return false;
		}
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
			String openid = openid(code);
			account().put(String.join(Const.delimiter, Const.Version.V2, "Join", "Game", "Like", openid), ""+System.currentTimeMillis());
			Page<Project> page = handler.listProjects(false, 5, projectid, openid);
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
		} else if("draw".equals(action)) {
			String todo = parameter.getParameter("todo");
			String appid = parameter.getParameter("appid");
			String code = parameter.getParameter("code");
			String openid = parameter.getParameter("openid");
			if("insert".equals(todo)) {
				String category = parameter.getParameter("category");
				String chapter = parameter.getParameter("chapter");
				String userid = parameter.getParameter("userid");
				String question = parameter.getParameter("question");
				String type = parameter.getParameter("type");
				String optionsA = parameter.getParameter("optionsA");
				String optionsB = parameter.getParameter("optionsB");
				String optionsC = parameter.getParameter("optionsC");
				String optionsD = parameter.getParameter("optionsD");
				String right = parameter.getParameter("right");
				String explain = parameter.getParameter("explain");
				String id = drawHandler.insert(userid, category == null ? "-1":category, chapter == null ? "-1":chapter, question, type, optionsA, optionsB, optionsC, optionsD, right, explain);
				return gson.toJson(new Result(id));
			} else if("list".equals(todo)) {
				String category = parameter.getParameter("category");
				String chapter = parameter.getParameter("chapter");
				String userid = parameter.getParameter("userid");
				List<String> list = drawHandler.list(userid, category == null ? "-1":category, chapter == null ? "":chapter);
				return gson.toJson(new Result(list));
			} else if("select".equals(todo)) {
				String category = parameter.getParameter("category");
				String chapter = parameter.getParameter("chapter");
				String userid = parameter.getParameter("userid");
				String id = parameter.getParameter("id");
				if(id != null) {
					Quiz data = drawHandler.select(userid, category, chapter, id);
					return gson.toJson(new Result(data));
				} 
			} else if("amivip".equals(todo)) {
				if(checkVIP(appid, code, openid)) {
					return gson.toJson(new Result(new String[] {"VIP", appid, openid}));
				} else {
					return gson.toJson(new Result(500, "VIP error"));
				}
			} 
		} else if("location".equals(action)) {
			String todo = parameter.getParameter("todo");
			String appid = parameter.getParameter("appid");
			String locationid = parameter.getParameter("locationid");
			String code = parameter.getParameter("code");
			String openid = parameter.getParameter("openid");
			logger.info("code: " + code + ",openid: " + openid);
			if("free".equals(todo)) {
				String key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Free");
				String free = account().get(key);
				if("false".equals(free)) {
					logger.info("user " + openid + " want a charged location " + locationid);
					return gson.toJson(new Result(502, "这是一个收费地点"));
				}
				return gson.toJson(new Result("free"));
			} else if("amivip".equals(todo)) {
				if(checkVIP(appid, code, openid)) {
					return gson.toJson(new Result(new String[] {"VIP", appid, openid}));
				} else {
					return gson.toJson(new Result(500, "VIP error"));
				}
			} else if("search".equals(todo)) {
				String search = parameter.getParameter("search");
				String[] words = search.split("\\s+");
				String head = String.join(Const.delimiter, Const.Version.V2, "Location", "List");
				List<String> list = new ArrayList<>();
				List<String> ids = new ArrayList<>();
				account().page(head, head, null, 10000, (k, id) -> {
					if(k.contains(id)) ids.add(id);
				});
				for(String id: ids) {
					for(String word : words) {
						String key = String.join(Const.delimiter, Const.Version.V2, "Location", id, "Address");
						String value = account().get(key);
						if(value.contains(word)) {
							list.add(id);
							break;
						}
						key = String.join(Const.delimiter, Const.Version.V2, "Location", id, "Name");
						value = account().get(key);
						if(value.contains(word)) {
							list.add(id);
							break;
						}
						key = String.join(Const.delimiter, Const.Version.V2, "Location", id, "Description");
						value = account().get(key);
						if(value.contains(word)) {
							list.add(id);
							break;
						}
					}
				}
				return gson.toJson(new Result(list));
			} else if("delete".equals(todo)) {
				if(STUPID.equals(openid)) {
					account().put(String.join(Const.delimiter, Const.Version.V2, "Location", "List", locationid), locationid + ".Delete." + System.currentTimeMillis());
					account().put(String.join(Const.delimiter, Const.Version.V2, "Location", "Delete", "" + System.currentTimeMillis(), openid), locationid);
				} else {
					logger.info("user delete location");
				}
				return gson.toJson(new Result("delete"));
			} else if("charge".equals(todo)) {
				if(STUPID.equals(openid)) {
					String now = "" + System.currentTimeMillis();
					String tellAdminsKey = String.join(Const.delimiter, Const.Version.V1, "Info", "TellAdmins", openid, now, appid);
					String free = parameter.getParameter("free");
					String key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Free");
					account().put(key, free);
					if("false".equals(free)) {
						account().put(String.join(Const.delimiter, Const.Version.V1, "Sell","Goods", locationid, "Price"), "100");
						logger.info("create goods location: " + locationid);
						account().put(tellAdminsKey, "创建收费地点");
					}
				} else {
					logger.info("user charge location");
				}
				return gson.toJson(new Result("delete"));
			} else if("comments".equals(todo)) {
				String curr = parameter.getParameter("curr");
				String head = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Comments");
				if(curr == null || curr.trim().length() < 1) {
					curr = head;
				}
				List<String> comments = new ArrayList<String>();
				data().page(head, curr, null, 20, (k,v) ->{
					comments.add(v);
				});
				return gson.toJson(new Result(comments));
			} else if("comment".equals(todo)) {
				long now = System.currentTimeMillis();
				long curr = STOP_THE_WORLD - now;
				String key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Comments", ""+curr, openid);
				String comment = parameter.getParameter("comment");
				String value = now +";" + openid + ";[审核之后显示];" + comment;
				logger.info("comment needs to be approved: " + value);
				data().put(String.join(Const.delimiter, Const.Version.V2, "Location", openid, "Comments", locationid, ""+ now), comment);
				data().put(key, value);
				return gson.toJson(new Result(value));
			} else if("star".equals(todo)) {
				String key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Rate", openid);
				String rate = parameter.getParameter("rate");
				float value = 1;
				try {
					value = Float.parseFloat(rate);
					if(value > 5) value = 5;
					if(value < 0) value = 1;
				} catch(Exception e) {
					value = 1;
				}
				data().put(key, "" + value);
				return gson.toJson(new Result("" + value));
			} else if("publish".equals(todo)) {
				locationid = System.currentTimeMillis() + Const.delimiter + new Random().nextInt(99999);
				String now = "" + System.currentTimeMillis();
				String tellAdminsKey = String.join(Const.delimiter, Const.Version.V1, "Info", "TellAdmins", openid, now, appid);
				account().put(String.join(Const.delimiter, Const.Version.V2, "Location", "List", locationid), locationid);
				String key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Openid");
				String value = parameter.getParameter("openid"); 
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Appid");
				value = parameter.getParameter("appid"); 
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Image");
				value = parameter.getParameter("image"); 
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Free");
				value = parameter.getParameter("free"); 
				if("false".equals(value)) {
					account().put(String.join(Const.delimiter, Const.Version.V1, "Sell","Goods", locationid, "Price"), "100");
					logger.info("create goods location: " + locationid);
					account().put(tellAdminsKey, "创建收费地点");
				} else {
					value = "true";
				}
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Latitude");
				value = parameter.getParameter("latitude"); 
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Longitude");
				value = parameter.getParameter("longitude"); 
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Address");
				value = parameter.getParameter("address"); 
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Name");
				value = parameter.getParameter("name"); 
				account().put(key, value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Description");
				value = parameter.getParameter("desc"); 
				account().put(key, value);
				account().put(tellAdminsKey, "发布地点：" + value);
				key = String.join(Const.delimiter, Const.Version.V2, "Location", locationid, "Rate", openid, now);
				data().put(key, "5");
				return gson.toJson(new Result(locationid));
			} else if("clients".equals(todo)) {
				String head = String.join(Const.delimiter, Const.Version.V2, "Location", "List");
				List<Map<String, String>> list = new ArrayList<>();
				List<String> ids = new ArrayList<>();
				account().page(head, head, null, 10000, (k, id) -> {
					if(k.contains(id)) ids.add(id);
				});
				String tellAdminsKey = String.join(Const.delimiter, Const.Version.V1, "Info", "TellAdmins", openid, "" + System.currentTimeMillis(), appid);
				account().put(tellAdminsKey, "地点点赞列表");
				for(String id: ids) {
					Map<String,String> e = new HashMap<String, String>();
					String free = account().get(String.join(Const.delimiter, Const.Version.V2, "Location", id, "Free"));
					if(!"false".equals(free)) {
						free = "true";
					}
					e.put("locationid", id);
					e.put("order", "true");
					e.put("free", free);
					e.put("iconPath", account().get(String.join(Const.delimiter, Const.Version.V2, "Location", id, "Image")));
					e.put("name", account().get(String.join(Const.delimiter, Const.Version.V2, "Location", id, "Name")));
					e.put("location", account().get(String.join(Const.delimiter, Const.Version.V2, "Location", id, "Description")));
					e.put("latitude", account().get(String.join(Const.delimiter, Const.Version.V2, "Location", id, "Latitude")));
					e.put("longitude", account().get(String.join(Const.delimiter, Const.Version.V2, "Location", id, "Longitude")));
					
					String key = String.join(Const.delimiter, Const.Version.V2, "Location", id, "Rate");
					AtomicInteger count = new AtomicInteger(0);
					AtomicInteger sum = new AtomicInteger(0);
					data().page(key, key, null, 999, (k,v)->{
						count.incrementAndGet();
						try {
							float value = Float.parseFloat(v);
							if(value > 5) value = 5;
							if(value < 0) value = 1;
							value = value*100;
							sum.addAndGet((int)value);
						} catch(Exception x) {
							sum.addAndGet(100);
						}
					});
					float rate = sum.floatValue() / 100;
					float average = rate / count.get();
					float percent = average * 20;
					e.put("percent", ""+(int)percent);
					e.put("star", ""+count.get());
					e.put("rate", ""+average);
					list.add(e);
				}
				return gson.toJson(new Result(list));
			}
		} else if("joinnearby".equals(action)) {
			String todo = parameter.getParameter("todo");
			String appid = parameter.getParameter("appid");
			String code = parameter.getParameter("code");
			String openid = parameter.getParameter("openid");
			logger.info("code: " + code + ",openid: " + openid);
			if("amivip".equals(todo)) {
				if(checkVIP(appid, code, openid)) {
					return gson.toJson(new Result(new String[] {"VIP", appid, openid}));
				} else {
					return gson.toJson(new Result(500, "未加入VIP"));
				}
			} 
		}
		return gson.toJson(new Result(400, "unknown action"));
	}
	public boolean isNull(String value) {
		return value == null || "null".equals(value) || "None".equals(value);
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
		String join = account().get(String.join(Const.delimiter, Const.Version.V2, "Join", "Game", "Like", openid));
		logger.info("should I join " + openid + " = " + join);
		return join == null || openid.equals(join) || System.currentTimeMillis()  - Long.valueOf(join) > TimeUnit.MINUTES.toMillis(5);
	}
	
	public String fake(String openid) {
		int len = openid.length();
		return "未关注" + openid.substring(len - 4, len);
	}
	
	public KeyValue user(String openid) {
		String json = account().get(String.join(Const.delimiter, Const.Version.V1, openid, "User"));
		if(openid == null || json == null) {
			return new KeyValue(fake(openid), "https://suppresswarnings.com/suppresswarnings.jpg");
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

	public void activate() {
		logger.info("LikeService activate");
		handler = new LikeHandlerImpl(this);
		drawHandler = new DrawHandlerImpl(this);
		counters = new ConcurrentHashMap<>();
		
		service.scheduleWithFixedDelay(()->{
			try {
				logger.info("start to execute git pull");
				gitpull();
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
		int cnt = count.incrementAndGet();
		String countProjectLikeKey = String.join(Const.delimiter, Const.Version.V2, "Project", "LikeCount", project);
		data().put(countProjectLikeKey, "" + cnt);
		return cnt;
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
