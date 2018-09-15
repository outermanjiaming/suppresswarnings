/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service;

import java.util.List;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.Format;
import com.suppresswarnings.corpus.service.backup.Server;
import com.suppresswarnings.corpus.service.daigou.Cart;
import com.suppresswarnings.corpus.service.daigou.DaigouHandler;
import com.suppresswarnings.corpus.service.daigou.Goods;
import com.suppresswarnings.corpus.service.daigou.Order;
import com.suppresswarnings.corpus.service.exam.ExamHandler;
import com.suppresswarnings.corpus.service.http.CallableGet;
import com.suppresswarnings.corpus.service.http.CallablePost;
import com.suppresswarnings.corpus.service.sdk.WXPay;
import com.suppresswarnings.corpus.service.sdk.WXPayConfig;
import com.suppresswarnings.corpus.service.sdk.WXPayConfigImpl;
import com.suppresswarnings.corpus.service.sdk.WXPayUtil;
import com.suppresswarnings.corpus.service.work.Quiz;
import com.suppresswarnings.corpus.service.work.TodoTask;
import com.suppresswarnings.corpus.service.work.WorkHandler;
import com.suppresswarnings.corpus.service.work.WorkerUser;
import com.suppresswarnings.corpus.service.sdk.WXPayConstants.SignType;
import com.suppresswarnings.corpus.service.wx.AccessToken;
import com.suppresswarnings.corpus.service.wx.JsAccessToken;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXuser;
import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.corpus.common.SendMail;
import com.suppresswarnings.corpus.common.TTL;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class CorpusService implements HTTPService, CommandProvider {
	public static final String SUCCESS = "success";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	public Format format = new Format(Const.WXmsg.msgFormat);
	Map<String, TTL> secondlife = new ConcurrentHashMap<String, TTL>();
	LinkedBlockingQueue<TTL> ttl = new LinkedBlockingQueue<TTL>(100000);
	public Map<String, Provider<?>> providers = new HashMap<>();
	public Map<String, ContextFactory<CorpusService>> factories = new HashMap<>();
	public Map<String, Context<?>> contexts = new ConcurrentHashMap<String, Context<?>>();
	public LevelDB account, data, token;
	Server backup;
	public DaigouHandler daigouHandler;
	public WorkHandler workHandler;
	
	public ExamHandler examHandler;
	ScheduledExecutorService scheduler;
	public Map<String, String> questionToAid = new ConcurrentHashMap<>();
	public Map<String, HashSet<String>> aidToAnswers = new ConcurrentHashMap<>();
	public Map<String, HashSet<String>> aidToSimilars = new ConcurrentHashMap<>();
	//
	public static final int bear = 5;
	public List<Quiz> assimilatedQuiz = new ArrayList<>();
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
		Provider<?> provider = providers.get(key);
		if(provider == null) {
			logger.error("[corpus] get null from providers by key: " + key);
			return new DefaultLevelDB(key);
		}
		LevelDB instance = (LevelDB) provider.instance();
		return instance;
	}
	
	public String youGotMe(String openId, String quiz, String quizId) {
		this.workHandler.clockOut(openId);
		return this.workHandler.newJob(quiz, quizId, openId);
	}
	public void forgetIt(String openId) {
		this.workHandler.forgetIt(openId);
	}
	public boolean iWantJob(String openid, Type type) {
		return this.workHandler.clockIn(openid, type);
	}
	public void activate() {
		logger.info("[corpus] activate.");
		backup = new Server();
		try {
			backup.working();
		} catch (Exception e) {
			logger.error("[corpus] server backup fail to start working", e);
		}
		//daigou
		daigouHandler = new DaigouHandler(this);
		workHandler = new WorkHandler(this, Const.WXmsg.openid);
		examHandler = new ExamHandler(this, "ClassExam");
		try {
			workHandler.working();
		} catch (Exception e) {
			logger.error("[corpus] workHandler fail to start working", e);
		}
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				workHandler.report();
				//TODO send email
				long now = System.currentTimeMillis();
				int currentTTL = ttl.size();
				logger.info("[corpus run] start clean TTL("+currentTTL+")");
				ttl.removeIf(out -> {
					if(out.ttl() < now) {
						if(out.marked()) {
							logger.info("[corpus run] remove key: " + out.key());
							secondlife.remove(out.key());
							contexts.remove(out.key());
							return true;
						} else {
							out.mark();
							secondlife.put(out.key(), out);
						}
					}
					return false;
				});
				int change = currentTTL - ttl.size();
				logger.info(change > 0 ? "[corpus run] removed " + change + " TTLs" : "[corpus run] TTL not changed");
			}
		}, TimeUnit.MINUTES.toMillis(3), TimeUnit.MINUTES.toMillis(2), TimeUnit.MILLISECONDS);
		logger.info("[corpus] TTL scheduler starts in 3 minutes");
		//NOTE access token
		scheduler.scheduleAtFixedRate(new Runnable() {
			int times = 0;
			long last = System.currentTimeMillis();
			String key = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", "973rozg");
			String expireKey = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Expire", "973rozg");
			
			@Override
			public void run() {
				try {
					times ++;
					CallableGet get = new CallableGet("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", System.getProperty("wx.appid"), System.getProperty("wx.secret"));
					long now = System.currentTimeMillis();
					long period = now - last;
					logger.info("[access token] start " + times + ", last: " + last + ", now: " + now + ", period: " + period);
					last = now;
					String json = get.call();
					Gson gson = new Gson();
					AccessToken at = gson.fromJson(json, AccessToken.class);
					long expireAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(7200);
					int result = token().put(key, at.getAccess_token());
					token().put(expireKey, "" + expireAt);
					logger.info("[access token] refresh " + result + ", expires at " + at.getExpires_in());
				} catch (Exception e) {
					logger.error("[access token] Exception when refresh", e);
				}
			}
		}, TimeUnit.MINUTES.toMillis(3), TimeUnit.SECONDS.toMillis(7200), TimeUnit.MILLISECONDS);
		logger.info("[corpus] refresh access token scheduler starts in 3 minutes");
		//NOTE notice report
		scheduler.scheduleAtFixedRate(new Runnable() {
			int times = 0;
			@Override
			public void run() {
				times ++;
				String admins = account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Admins"));
				if(admins == null) {
					logger.error("[corpus] scheduler: the admins not set");
					return;
				}
				
				String[] admin = admins.split(",");
				StringBuffer info = new StringBuffer();
				info.append("周期汇报：第").append(times).append("次").append("\n");
				info.append("contexts: " + contexts.size()).append("\n");
				info.append("providers: " + providers.size()).append("\n");
				info.append("factories: " + factories.size()).append("\n");
				info.append("questionToAid: " + questionToAid.size()).append("\n");
				info.append("aidToAnswers: " + aidToAnswers.size()).append("\n");
				info.append("ttl: " + ttl.size()).append("\n");
				info.append("backup：" + backup.toString()).append("\n");
				info.append("workHandler：" + workHandler.report());
				for(String one : admin) {
					sendTxtTo("schedule report", info.toString(), one);
				}
			}
		}, TimeUnit.MINUTES.toMillis(3), TimeUnit.MINUTES.toMillis(90), TimeUnit.MILLISECONDS);
		logger.info("[corpus] scheduler admins info starts in 3 minutes");
		
		String quizId = getTodoQuizid();
		if(quizId != null) {
			fillQuestionsAndAnswers(quizId);
		}
	}

	public void deactivate() {
		logger.info("[corpus] deactivate.");
		if(backup != null) {
			backup.close();
		}
		backup = null;
		if(scheduler != null) {
			scheduler.shutdownNow();
		}
		scheduler = null;
		secondlife.clear();
		ttl.clear();
		providers.clear();
		factories.clear();
		contexts.clear();
	}

	public void modified() {
		logger.info("[corpus] modified.");
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
	public void factory(ContextFactory<CorpusService> factory) {
		logger.info("factory: " + factory.description());
		if(factories.containsKey(factory.command())) {
			logger.warn("factory exist, replace: " + factory.command());
		} else {
			logger.info("new factory register: " + factory.command());
		}
		factories.put(factory.command(), factory);
	}
	public void clearFactory(ContextFactory<CorpusService> factory) {
		boolean removed = factories.remove(factory.command(), factory);
		logger.info("remove the factory: " + factory.command() + "(" + factory + ") = " + removed);
	}
	
	@Override
	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("---SuppressWarnings CommandProvider---\n");
		buffer.append("\t which - which <which> - change to use different LevelDB.\n");
		buffer.append("\t putkv - putkv <key> <value> - put value to that key.\n");
		buffer.append("\t getkv - getkv <key> - get value by that key.\n");
		buffer.append("\t listn - listn <startkey> <limit> - list some values by start limit.\n");
		buffer.append("\t deleten - deleten <startkey> <limit> - delete some values by start limit.\n");
		return buffer.toString();
	}
	LevelDB leveldb;
	public void _which(CommandInterpreter ci) {
		logger.info("[_which] " + leveldb);
		String arg = ci.nextArgument();
		ci.println(providers.keySet() + ": "+ arg);
		leveldb = (LevelDB) providers.get(arg).instance();
		ci.println("[_which] " + leveldb);
	}
	
	public void _putkv(CommandInterpreter ci) {
		logger.info("[putkv] " + leveldb);
		if(leveldb == null) return;
		String key = ci.nextArgument();
		String value = ci.nextArgument();
		int result = leveldb.put(key, value);
		ci.println("[putkv] " + result + " put: "+ key + " = " + value);
	}
	
	public void _getkv(CommandInterpreter ci) {
		logger.info("[getkv] " + leveldb);
		if(leveldb == null) return;
		String key = ci.nextArgument();
		String value = leveldb.get(key);
		ci.println("get: "+ key + " = " + value);
	}
	public void _deleten(CommandInterpreter ci) {
		logger.info("[getkv] " + leveldb);
		if(leveldb == null) return;
		String start = ci.nextArgument();
		String limit = ci.nextArgument();
		int n = Integer.valueOf(limit);
		ci.println("[_deleten] head: " + start + ", count: " + n);
		AtomicInteger i = new AtomicInteger(0);
		leveldb.page(start, start, null, n, (k, v) -> {
			leveldb.del(k);
			int index = i.incrementAndGet();
			ci.println("[_deleten] " + index + ". remove key:" + k + " = " + v);
		});
	}
	public void _listn(CommandInterpreter ci) {
		logger.info("[_listn] " + leveldb);
		if(leveldb == null) return;
		String start = ci.nextArgument();
		String limit = ci.nextArgument();
		int n = Integer.valueOf(limit);
		leveldb.list(start, n, new BiConsumer<String, String>() {
			int index = 0;
			@Override
			public void accept(String t, String u) {
				index ++;
				ci.println("[_listn] " + index + ". " + t + " = " + u);
			}
		});
	}
	
	public String getTodoQuizid() {
		String taskKey = String.join(Const.delimiter, Const.Version.V1, "Task", "Quiz", "Reply");
		String quizId = data().get(taskKey);
		return quizId;
	}
	
	public int fillExam() {
		AtomicInteger integer = new AtomicInteger(0);
		assimilatedQuiz.forEach(quiz ->{
			if(quiz.getReply().size() < bear) {
				integer.incrementAndGet();
				examHandler.batchJob(quiz.getQuiz().value(), quiz.getQuiz().key(), Type.Reply);
			}
			if(quiz.getSimilar().size() < bear) {
				integer.incrementAndGet();
				examHandler.batchJob(quiz.getQuiz().value(), quiz.getQuiz().key(), Type.Similar);
			}
		});
		return integer.get();
	}
	
	public int fillWork() {
		AtomicInteger integer = new AtomicInteger(0);
		assimilatedQuiz.forEach(quiz ->{
			if(quiz.getReply().size() < bear) {
				integer.incrementAndGet();
				workHandler.batchJob(quiz.getQuiz().value(), quiz.getQuiz().key(), Type.Reply);
			}
			if(quiz.getSimilar().size() < bear) {
				integer.incrementAndGet();
				workHandler.batchJob(quiz.getQuiz().value(), quiz.getQuiz().key(), Type.Similar);
			}
		});
		return integer.get();
	}
	
	
	
	public void fillQuestionsAndAnswers(String quizId){
		String start = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus","Quiz", quizId, "Answer");
		assimilatedQuiz.clear();
		workHandler.close();
		workHandler.working();
		List<Quiz> allQuiz = new ArrayList<>();
		data().page(start, start, null, Integer.MAX_VALUE, (t, u) -> {
			String left = t.substring(start.length());
			if(!left.contains("Similar") && !left.contains("Reply")) {
				Quiz quiz = new Quiz(t, u);
				allQuiz.add(quiz);
			}
		});
		//fill the reply and similar into each quiz
		allQuiz.forEach(quiz -> {
			
			String replyKey = String.join(Const.delimiter, quiz.getQuiz().key(), "Reply");
			data().page(replyKey, replyKey, null, Integer.MAX_VALUE, (t, u) -> {
				String left = t.substring(replyKey.length());
				if(!left.contains("Similar") && !left.contains("Reply")) {
					quiz.reply(t, u);
				}
			});
			
			String similarKey = String.join(Const.delimiter, quiz.getQuiz().key(), "Similar");
			data().page(similarKey, similarKey, null, Integer.MAX_VALUE, (t, u) -> {
				String left = t.substring(similarKey.length());
				if(!left.contains("Similar") && !left.contains("Reply")) {
					quiz.similar(t, u);
				}
			});
		});
		
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
		
		fillWork();
	}
	public void _interception(CommandInterpreter ci) {
		String quizId = ci.nextArgument();
		ci.println("[_interception] quizId: " + quizId);
		fillQuestionsAndAnswers(quizId);
		AtomicInteger integer = new AtomicInteger(0);
		this.questionToAid.forEach((quiz, aid) -> {
			ci.println(integer.incrementAndGet() + ".   " + quiz + "\n\t->A. " + this.aidToAnswers.get(aid) + "\n\t->S. " + this.aidToSimilars.get(aid));
		});
	}
	
	@Override
	public String getName() {
		return "wx.http";
	}
	@Override
	public String start(Parameter parameter) throws Exception {
		// TODO get action to do things
		String action = parameter.getParameter("action");
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		if("class_exam".equals(action)){
			String onKey = String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus", "ON");
			String on = data().get(onKey);
			if("off".equals(on)) {
				return "fail";
			}
			examHandler.report();
			String time = "" +System.currentTimeMillis();
			String openid = parameter.getParameter("ticket");
			String type = parameter.getParameter("type");
			String random = parameter.getParameter("random");
			String quizid = parameter.getParameter("quizid");
			if(quizid == null || "".equals(quizid.trim())) {
				logger.info("[class_exam] null quizid: " + quizid);
				return "fail";
			}
			String text =  parameter.getParameter("text");
			if(text == null || "".equals(text.trim())) {
				logger.info("[class_exam] null text: " + text);
				return "fail";
			}
			Gson gson = new Gson();
			try {
				if("index".equals(type)){
					List<Quiz> quizs = examHandler.allQuiz();
					return gson.toJson(quizs);
				} else if("quiz".equals(type)) {
					String quizKey = String.join(Const.delimiter, Const.Version.V2, "Collect","Corpus","Quiz", quizid, "Answer", openid, time);
					data().put(quizKey, text);
					examHandler.newJob(text, quizKey, openid);
					return quizKey;
				} else {
					if("reply".equals(type)) {
						logger.info("[class_exam] reply");
						TodoTask task = new TodoTask();
						task.setOpenId(openid);
						task.setQuizId(quizid);
						task.setQuiz(text);
						task.setResult(text);
						
						String answer = text;
						String answerKey = String.join(Const.delimiter, task.getQuizId(), Type.Reply.name(), openid, time, random);
						data().put(answerKey, answer);
						task.finish(answer);
						logger.info("[class_exam] reply： " + task.toString());
						examHandler.done(task, Type.Reply);
						
						
						WorkerUser worker = new WorkerUser();
						worker.setOpenId(openid);
						worker.setType(Type.Reply);
						worker.setFree();
						TodoTask todo = examHandler.want(worker);
						if(todo == null) return "fail";
						Map<String, String> map = new HashMap<>();
						map.put("quiz", todo.getQuiz());
						map.put("quizid", todo.getQuizId());
						map.put("deleteone", answerKey);
						return gson.toJson(map);
					} else if("similar".equals(type)) {
						logger.info("[class_exam] similar");
						TodoTask task = new TodoTask();
						task.setOpenId(openid);
						task.setQuizId(quizid);
						task.setResult(text);
						task.setQuiz(text);
						
						String answer = text;
						String answerKey = String.join(Const.delimiter, task.getQuizId(), Type.Similar.name(), openid, time, random);
						data().put(answerKey, answer);
						task.finish(answer);
						logger.info("[class_exam] similar： " + task.toString());
						examHandler.done(task, Type.Similar);
						examHandler.newJob(text, quizid, openid);
						
						WorkerUser worker = new WorkerUser();
						worker.setOpenId(openid);
						worker.setType(Type.Similar);
						TodoTask todo = examHandler.want(worker);
						if(todo == null) return "fail";
						Map<String, String> map = new HashMap<>();
						map.put("quiz", todo.getQuiz());
						map.put("quizid", todo.getQuizId());
						map.put("deleteone", answerKey);
						return gson.toJson(map);
					} else if("delete".equals(type)) {
						String delete = parameter.getParameter("quizid");
						String value = data().get(delete);
						String deleteKey = String.join(Const.delimiter, Const.Version.V2, "Corpus", "Delete", time, openid);
						data().put(deleteKey, value);
						int ret = data().del(delete);
						value = data().get(delete);
						logger.info("[class_exam] "+ ret + "delete " + value + " == " + delete);
						return SUCCESS;
					}
					
				}
			} catch (Exception e) {
				logger.error("[class_exam] Exception", e);
			}
			return "fail";
		} else if("WX".equals(action)) {
			logger.info("[WX] request: " + parameter.toString());
			String msgSignature = parameter.getParameter("signature");
			String timestamp = parameter.getParameter("timestamp");
			String nonce = parameter.getParameter("nonce");
			String sha1 = getSHA1(Const.WXmsg.secret[0], timestamp, nonce, "");
			String openid =  parameter.getParameter("openid");
			String echoStr = parameter.getParameter("echostr");
			String token = parameter.getParameter("token");
			String fromOpenId = Const.WXmsg.openid;
			if(token != null) {
				String fromOpenIdKey = String.join(Const.delimiter, Const.Version.V1, "WXID", "Token", token);
				fromOpenId = account().get(fromOpenIdKey);
			}
			if(msgSignature == null || !msgSignature.equals(sha1)) {
				logger.error("[WX] wrong signature");
				if(openid != null) {
					return xml(openid, Const.WXmsg.reply[0], fromOpenId);
				}
			}
			if(echoStr != null) {
				return echoStr;
			}
			if(openid != null) {
				String sms = parameter.getParameter(Parameter.POST_BODY);
				if(sms == null) {
					return xml(openid, Const.WXmsg.reply[1], fromOpenId);
				}
				Map<String, String> wxmsg = WXPayUtil.xmlToMap(sms);
				String msgType = wxmsg.get("MsgType");
				logger.info("[WX] check: " + msgType);
				if(msgType == null) {
					SendMail cn = new SendMail();
					cn.title("notes [WX] msg structure not match", wxmsg.toString());
					return xml(openid, Const.WXmsg.reply[1], fromOpenId);
				}
				String input = "";
				if("text".equals(msgType)) {
					input = wxmsg.get("Content");
				} else if("voice".equals(msgType)) {
					input = wxmsg.get("Recognition");
					if(input == null) {
						input = "听不清说了啥？";
					}
				} else if("event".equals(msgType)) {
					//lijiaming leave from worker user
					forgetIt(openid);
					
					String where = "素朴网联";
					String event = wxmsg.get("Event");
					String eventKey = wxmsg.get("EventKey");
					String ticket = wxmsg.get("Ticket");
					if(eventKey != null) {
						if(eventKey.startsWith("qrscene_")){
							where = eventKey.substring("qrscene_".length());
						} else {
							where = eventKey;
						}
					}
					logger.info("[lijiaming] event: " + where);
					if("subscribe".equals(event)) {
						String subscribeKey = String.join(Const.delimiter, Const.Version.V1, "Subscribe", openid);
						String subscribe = account().get(subscribeKey);
						
						
						String time = "" + System.currentTimeMillis();
						subscribe(openid, time);
						
						if(eventKey != null) {
							String subscribeEventKey = String.join(Const.delimiter, Const.Version.V1, "Subscribe", "Event", openid, time);
							account().put(subscribeEventKey, eventKey);
							String exchange = globalCommand(where);
							logger.info("SCAN: " + exchange + " == " + where);
							//NOTE a qrcode with exchange commands
							if(exchange != null) {
								ContextFactory<CorpusService> cf = factories.get(exchange);
								Context<CorpusService> contxt = cf.getInstance(fromOpenId, openid, this);
								contxt.test("SCAN_" + where);
								contextx(openid, contxt, cf.ttl());
								return xml(openid, contxt.output(), fromOpenId);
							}
						}
						
						if(ticket != null) {
							dealWithTicket(ticket, openid);
						}
						
						if(subscribe == null) {
							account().put(subscribeKey, time);
							return xml(openid, "hi，如果你愿意，我会陪你聊天一直到永远，你首先想到说什么吗？", fromOpenId);
						} else {
							String subscribeHistoryKey = String.join(Const.delimiter, Const.Version.V1, openid, "Subscribe", subscribe);
							account().put(subscribeHistoryKey, time);
							account().put(subscribeKey, time);
							if(subscribe.contains("unsubscribe")) {
								return xml(openid, "hi，如果你依然愿意，我会陪你聊天一直到永远，你首先想到说什么吗？", fromOpenId);
							} else {
								return xml(openid, "hi，如果你还愿意的话，我会陪你聊天一直到永远，你首先想到说什么吗？", fromOpenId);
							}
						}
					} else if("unsubscribe".equals(event)) {
						String subscribeKey = String.join(Const.delimiter, Const.Version.V1, "Subscribe", openid);
						String subscribe = account().get(subscribeKey);
						String time = "" + System.currentTimeMillis();
						String subscribeHistoryKey = String.join(Const.delimiter, Const.Version.V1, openid, "Subscribe", subscribe);
						account().put(subscribeHistoryKey, time);
						account().put(subscribeKey, time + Const.delimiter + "unsubscribe");
						return SUCCESS;
					} else if("SCAN".equals(event)) {
						dealWithTicket(ticket, openid);
						//TODO different scene
						String exchange = globalCommand(where);
						logger.info("SCAN: " + exchange + " == " + where);
						if(exchange != null) {
							ContextFactory<CorpusService> cf = factories.get(exchange);
							Context<CorpusService> contxt = cf.getInstance(fromOpenId, openid, this);
							contxt.test("SCAN_" + where);
							contextx(openid, contxt, cf.ttl());
							return xml(openid, contxt.output(), fromOpenId);
						}
						
						return xml(openid, "欢迎来到【" + where + "】", fromOpenId);
					} else if("LOCATION".equals(event)) {
						logger.info("[corpus] location: " + wxmsg.get("FromUserName") + " = (" + wxmsg.get("Latitude") + ", " + wxmsg.get("Longitude") + ") * " + wxmsg.get("Precision"));
						return SUCCESS;
					}
				} else if("image".equals(msgType)) {
					String url = wxmsg.get("PicUrl");
					input = "IMAGE_" + url;
				} else {
					return SUCCESS;
				}

				Context<?> context = context(openid);
				if(context == null) {
					//lijiaming leave from worker user
					forgetIt(openid);
					
					String command = CheckUtil.cleanStr(input);
					ContextFactory<CorpusService> cf = factories.get(command);
					if(cf == null) {
						String exchange = globalCommand(command);
						if(exchange != null) {
							cf = factories.get(exchange);
						}
					}
					
					if(cf != null) {
						Context<CorpusService> ctx = cf.getInstance(fromOpenId, openid, this);
						context = ctx;
						if(cf.ttl() != ContextFactory.forever) {
							contextx(openid, context, cf.ttl());
						} else {
							context(openid, context);
						}
						context.test(input);
						return xml(openid, context.output(), fromOpenId);
					}
					
					WXContext ctx = new WXContext(fromOpenId, openid, this);
					logger.info("[WX] init context: " + ctx + " for openid: " + openid);
					context = ctx;
					contextx(openid, context, Const.InteractionTTL.userReply);
				}
				logger.info("[WX] use context: " + context + " for openid: " + openid);
				boolean finish = context.test(input);
				if(finish) {
					logger.info("[WX] this stage finished: " + context.state());
				}
				
				return xml(openid, context.output(), fromOpenId);
			}
		} else if("backup".equals(action)) {
			String from = parameter.getParameter("from");
			String capacity = parameter.getParameter("capacity");
			boolean result = backup.newAgent(from, Long.valueOf(capacity));
			return Boolean.toString(result);
		} else if("report".equals(action)) {
			String reportMsg = parameter.getParameter("msg");
			if(reportMsg == null || reportMsg.length() > 240) {
				return "";
			}
			String reportToken = parameter.getParameter("token");
			if(reportToken == null || reportToken.length() > 240) {
				return "";
			}
			String lastKey = String.join(Const.delimiter, Const.Version.V1, "Report", "Token", reportToken);
			String last = token().get(lastKey);
			if(last == null) {
				logger.error("[pi] token not accepted: " + reportToken);
				return "";
			}
			String time = "" + System.currentTimeMillis();
			String entry = String.join(Const.delimiter, Const.Version.V1, "Report", "Msg", reportToken, time);
			token().put(lastKey, time);
			token().put(entry, reportMsg + "\n数据来源：" + ip);
			return SUCCESS;
		} else if("login".equals(action)) {
			//TODO BUG ticket should expire
			String ticket = parameter.getParameter("ticket");
			if(ticket != null) {
				String openid = token().get(String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", ticket));
				if(openid != null) {
					return SUCCESS;
				}
				String expires = token().get(String.join(Const.delimiter, Const.Version.V1, "Temp", "QRCode", "Login", ticket));
				if(expires != null) {
					long expire = Long.parseLong(expires);
					if(expire < System.currentTimeMillis()) {
						return "Change";
					} else {
						return "Later";
					}
				}
			} else {
				return "";
			}
			
		} else if("qrcode".equals(action)){
			String random = parameter.getParameter("random");
			if(random == null) {
				return "Fail";
			}
			Gson gson = new Gson();
			String accessToken = accessToken("login");
			String result = qrCode(accessToken, 180, "QR_STR_SCENE", "‘素朴网联’官网");
			logger.info("[corpus qrcode] " + result);
			QRCodeTicket qrCodeTicket = gson.fromJson(result, QRCodeTicket.class);
			long expire = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(qrCodeTicket.getExpire_seconds());
			token().put(String.join(Const.delimiter, Const.Version.V1, "Temp", "QRCode", "Login", qrCodeTicket.getTicket()), "" + expire);
			return result;
		} else if("daigou".equals(action)){
			String random = parameter.getParameter("random");
			if(random == null) {
				logger.info("[daigou] random == null");
				return "fail";
			}
			String todo = parameter.getParameter("todo");
			if(todo == null) {
				logger.info("[daigou] todo == null");
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				logger.info("[daigou] ticket == null");
				return "fail";
			}
			
			//lijiaming: it show be serious about openid
			
			if("manageorders".equals(todo)){
				String state = parameter.getParameter("state");
				if(state == null) {
					logger.info("[daigou] state == null");
					return "fail";
				}
				//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
				String APPID = System.getProperty("wx.appid");
				String SECRET = System.getProperty("wx.secret");
				
				if(APPID == null || SECRET == null) {
					logger.error("[daigou] wrong request with null parameters: appid=" + APPID + ", code=" + CODE);
					return "fail";
				}
				Gson gson = new Gson();
				CallableGet get = new CallableGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", APPID, SECRET, CODE);
				String json = get.call();
				JsAccessToken accessToken = gson.fromJson(json, JsAccessToken.class);
				logger.info("[daigou access_token] " + accessToken.toString());
				String openId = accessToken.getOpenid();
				if(openId == null) {
					logger.info("[daigou] get openid failed");
					return "fail";
				}
				logger.info("[daigou] orders");
				if(!authrized(openId, "ManageOrders")) {
					logger.info("[daigou] check auth failed");
					return "fail";
				}
				return gson.toJson(daigouHandler.listOrders(openId));
			}
			
			if("changeorderstate".equals(todo)) {
				String userid = parameter.getParameter("userid");
				String orderid = parameter.getParameter("orderid");
				String newstate = parameter.getParameter("newstate");
				daigouHandler.updateOrderState(orderid, userid, newstate);
				return newstate;
			}
			
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			String openid = token().get(code2OpenIdKey);
			if(openid == null) {
				logger.info("[daigou] openid == null");
				return "fail";
			}
			String agentid = parameter.getParameter("state");
			if(agentid == null) {
				logger.info("[daigou] state == null");
				return "fail";
			}
			/**
			 * TODO: Daigou
			 */
			Gson gson = new Gson();
			if("index".equals(todo)) {
				logger.info("[daigou] index");
				List<Goods> list  = daigouHandler.listGoods();
				if(daigouHandler.replacePricecentVIPPrice(list, openid)) {
					logger.info("[daigou] use vip price");
				} else if(daigouHandler.replacePricecentAgentPrice(list, agentid)){
					logger.info("[daigou] use agent price");
				}
				return gson.toJson(list);
			}

			if("addgoodstocart".equals(todo)) {
				logger.info("[daigou] addgoodstocart");
				String goodsid = parameter.getParameter("goodsid");
				if(goodsid == null) {
					logger.info("[daigou] goodsid == null");
					return "fail";
				}
				Cart cart = daigouHandler.addGoodsToCart(agentid, openid, goodsid);
				if(cart == null) {
					logger.info("[daigou] cart == null");
					return "fail";
				}
				return SUCCESS;
			}
			if("mycarts".equals(todo)) {
				logger.info("[daigou] mycarts");
				List<Cart> carts = daigouHandler.myCarts(openid);
				daigouHandler.fillGoodsToCart(carts);
				return gson.toJson(carts);
			}
			if("makeanorder".equals(todo)) {
				logger.info("[daigou] makeanorder");
				String username = parameter.getParameter("username");
				if(username == null || username.length() < 1) return "fail";
				String idcard = parameter.getParameter("idcard");
				if(idcard == null || idcard.length() < 15) return "fail";
				String mobile = parameter.getParameter("mobile");
				if(mobile == null || mobile.length() < 10) return "fail";
				String address = parameter.getParameter("address");
				if(address == null || address.length() < 3) return "fail";
				String comment = parameter.getParameter("comment");
				
				List<Cart> carts = daigouHandler.myCarts(openid);
				if(carts.isEmpty()) {
					logger.info("[lijiaming] 订单为空");
					return "fail";
				}
				daigouHandler.fillGoodsToCart(carts);
				StringBuffer sb = new StringBuffer();
				long totalcent = 0;
				int totalcount = 0;
				int totaltype = 0;
				for(Cart cart : carts) {
					int count = Integer.parseInt(cart.getCount());
					int price = Integer.parseInt(cart.getActualpricecent());
					long pricecent = price * count;
					totaltype = totaltype + 1;
					totalcent = totalcent + pricecent;
					totalcount = totalcount + count;
					sb.append("[").append(cart.getGoodsid()).append("*").append(cart.getCount()).append("] ");
				}
				double totalprice = totalcent * 0.01d;
				
				String openIdEnd = openid.substring(openid.length() - 7);
				String randEnd = random.substring(random.length() - 4);
				long current = System.currentTimeMillis();
				String orderid = "DG"+ current + openIdEnd + randEnd;
				sb.append(openid);
				String detail = sb.toString();
				String clientip = ip.split(",")[0];
				String body = orderid +" 共¥" + totalprice;
				String attach = openid;
				String prepay = prepay(orderid, body, detail, attach, ""+totalcent, clientip, openid, current);
				if(prepay != null) {
					Order order = new Order();
					order.setAddress(address);
					order.setCarts(carts);
					order.setIdcard(idcard);
					order.setComment(comment);
					order.setGoodscount(""+totalcount);
					order.setGoodstypes(""+totaltype);
					order.setMobile(mobile);
					order.setOpenid(openid);
					order.setOrderid(orderid);
					order.setState(Order.State.Create);
					order.setTime(""+current);
					order.setTotalcent(""+totalcent);
					order.setUsername(username);
					order.setDetail(detail);
					daigouHandler.saveOrder(order);
					return prepay;
				} else {
					return "fail";
				}
			}
			if("goodsdetail".equals(todo)) {
				String goodsid = parameter.getParameter("goodsid");
				if(goodsid == null) {
					logger.info("[daigou] goodsid == null");
					return "fail";
				}
				Goods goods = daigouHandler.getByGoodsid(goodsid);
				if(daigouHandler.replacePricecentVIPPrice(goods, openid)) {
					logger.info("[daigou] use vip price");
				} else if(daigouHandler.replacePricecentAgentPrice(goods, agentid)){
					logger.info("[daigou] use agent price");
				}
				return gson.toJson(goods);
			}
			
			if("myorders".equals(todo)) {
				//TODO check openid
				logger.info("[daigou] myorders");
				List<Order> myOrders = daigouHandler.myOrders(openid);
				//TODO clean input for xss
				return gson.toJson(myOrders);
			}
			if("removecart".equals(todo)) {
				logger.info("[daigou] remove cart");
				String cartid = parameter.getParameter("cartid");
				if(cartid == null) {
					logger.info("[daigou] cartid == null");
					return "fail";
				}
				String deleted = daigouHandler.deleteCart(openid, cartid);
				if(!Cart.State.Delete.equals(deleted)) {
					return "fail";
				}
				return SUCCESS;
			}
			if("updatecartnum".equals(todo)) {
				logger.info("update cart num");
				String cartid = parameter.getParameter("cartid");
				if(cartid == null) {
					logger.info("cartid == null");
					return "fail";
				}
				String newnum = parameter.getParameter("newnum");
				if(newnum == null) {
					logger.info("newnum == null");
					return "fail";
				}
				try {
					Integer num = Integer.parseInt(newnum);
					if(num <= 0 || num > 10000) {
						return "fail";
					}
				} catch (Exception e) {
					logger.info("wrong number for cart: " + newnum + ", openid: " + openid + ", cartid: " + cartid);
					return "fail";
				}
				String result = daigouHandler.updateCartCount(openid, cartid, newnum);
				return result;
			}
			return SUCCESS;
		} else if("access_token".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			String exist = token().get(code2OpenIdKey);
			if(exist != null) {
				return SUCCESS;
			}
			//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
			String APPID = System.getProperty("wx.appid");
			String SECRET = System.getProperty("wx.secret");
			
			if(APPID == null || SECRET == null) {
				logger.error("[corpus access_token] wrong request with null parameters: appid=" + APPID + ", code=" + CODE);
				return "fail";
			}
			Gson gson = new Gson();
			CallableGet get = new CallableGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", APPID, SECRET, CODE);
			String json = get.call();
			JsAccessToken accessToken = gson.fromJson(json, JsAccessToken.class);
			logger.info("[corpus access_token] " + accessToken.toString());
			if(accessToken.getOpenid() == null) {
				return "fail";
			}
			
			token().put(code2OpenIdKey, accessToken.getOpenid());
			return SUCCESS;
		} else if("collect".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String quizId = parameter.getParameter("state");
			if(quizId == null) {
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				return "fail";
			}
			
			//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
			String APPID = System.getProperty("wx.appid");
			String SECRET = System.getProperty("wx.secret");
			
			if(APPID == null || SECRET == null) {
				logger.error("[corpus collect access_token] wrong request with null parameters: appid=" + APPID + ", code=" + CODE);
				return "fail";
			}
			Gson gson = new Gson();
			CallableGet get = new CallableGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", APPID, SECRET, CODE);
			String json = get.call();
			JsAccessToken accessToken = gson.fromJson(json, JsAccessToken.class);
			logger.info("[corpus collect access_token] " + accessToken.toString());
			String openId = accessToken.getOpenid();
			if(openId == null) {
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			token().put(code2OpenIdKey, openId);
			WXuser user = getWXuserByOpenId(openId);
			Map<String, Object> map = new HashMap<>();
			String quizKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus","Quiz", quizId);
			String quiz = data().get(quizKey);
			map.put("quiz", quiz);
			List<String> array = collectCrewImageByQuizId(quizId);
			map.put("array", array);
			map.put("username", user.getNickname());
			map.put("userimg", user.getHeadimgurl());
			List<Map<String, Object>> replyinfo = new ArrayList<>();
			String start = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus","Quiz", quizId, "Answer");
			data().page(start, start, null, Integer.MAX_VALUE, (t, u) -> {
				String z = t.substring(start.length());
				if(!z.contains("Similar") && !z.contains("Reply")) {
					boolean need = false;
					
					String adminKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Auth", "Admin", openId);
					String admin = account().get(adminKey);
					if(admin != null && !"None".equals(admin)) {
						need = true;
					}
					String question = CheckUtil.cleanStr(u);
					String aid = questionToAid.get(question);
					
					if(aid != null && !need) {
						HashSet<String> answers = aidToAnswers.get(aid);
						if(answers == null) {
							need = true;
						} else {
							if(answers.size() < 1) {
								need = true;
							}
						}
						
						HashSet<String> similars = aidToSimilars.get(aid);
						if(similars == null) {
							need = true;
						} else {
							if(similars.size() < 1) {
								need = true;
							}
						}
					}
					if(need) {
						Map<String, Object> e = new HashMap<>();
						e.put("replyid", t);
						e.put("reply", u);
						replyinfo.add(e);
					}
				}
			});
			map.put("replyinfo", replyinfo);
			return gson.toJson(map);
		} else if("replyquiz".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				logger.error("[corpus reply quiz] no random number");
				return "fail";
			}
			String quizId = parameter.getParameter("state");
			if(quizId == null) {
				logger.error("[corpus reply quiz] no state");
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				logger.error("[corpus reply quiz] no ticket");
				return "fail";
			}
			String reply = parameter.getParameter("reply");
			if(reply == null) {
				logger.error("[corpus reply quiz] reply is null");
				return "fail";
			}
			if(reply.trim().length() < 1) {
				logger.error("[corpus reply quiz] reply too short");
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			String exist = token().get(code2OpenIdKey);
			if(exist == null) {
				return "fail";
			}
			String time = "" + System.currentTimeMillis();
			String replyKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus","Quiz", quizId, "Answer", exist, time, random);
			int ret = data().put(replyKey, reply);
			String crewKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Crew", exist);
			data().put(crewKey, time);
			logger.info("[corpus reply quiz] " + ret + " openid:" + exist);
			return replyKey;
		} else if("replysimilar".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				logger.error("[corpus reply similar] no random number");
				return "fail";
			}
			String quizId = parameter.getParameter("state");
			if(quizId == null) {
				logger.error("[corpus reply quiz] no state");
				return "fail";
			}
			String replyId = parameter.getParameter("replyid");
			if(replyId == null) {
				logger.error("[corpus reply similar] no replyId");
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				logger.error("[corpus reply similar] no ticket");
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			String exist = token().get(code2OpenIdKey);
			if(exist == null) {
				return "fail";
			}
			String similar = parameter.getParameter("similar");
			if(similar == null) {
				logger.error("[corpus reply similar] similar is null");
				return "fail";
			}
			if(similar.trim().length() < 1) {
				logger.error("[corpus reply similar] similar too short");
				return "fail";
			}
			
			String time = "" + System.currentTimeMillis();
			String similarKey = String.join(Const.delimiter, replyId, "Similar", exist, time, random);
			int ret = data().put(similarKey, similar);
			String crewKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Crew", exist);
			data().put(crewKey, time);
			logger.info("[corpus reply similar] " + ret + " openid:" + exist);
			return similarKey;
		}else if("similarreplies".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				logger.error("[corpus reply similar] no random number");
				return "fail";
			}
			String quizId = parameter.getParameter("state");
			if(quizId == null) {
				logger.error("[corpus reply quiz] no state");
				return "fail";
			}
			String replyId = parameter.getParameter("replyid");
			if(replyId == null) {
				logger.error("[corpus reply similar] no replyId");
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				logger.error("[corpus reply similar] no ticket");
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			String exist = token().get(code2OpenIdKey);
			if(exist == null) {
				return "fail";
			}
			String similarKey = String.join(Const.delimiter, replyId, "Similar");
			List<String> result = new ArrayList<>();
			data().page(similarKey, similarKey, null, 1000, (t, u) -> {
				String z = t.substring(similarKey.length());
				if(!z.contains("Reply")) {
					result.add(u);
				}
			});
			logger.info("[corpus reply similar] " + result.size() + " openid:" + exist);
			Gson gson = new Gson();
			return gson.toJson(result);
		} else if("user".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String state = parameter.getParameter("state");
			if(state == null) {
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				return "fail";
			}
			
			//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
			String APPID = System.getProperty("wx.appid");
			String SECRET = System.getProperty("wx.secret");
			
			if(APPID == null || SECRET == null) {
				logger.error("[corpus user access_token] wrong request with null parameters: appid=" + APPID + ", code=" + CODE);
				return "fail";
			}
			Gson gson = new Gson();
			CallableGet get = new CallableGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", APPID, SECRET, CODE);
			String json = get.call();
			JsAccessToken accessToken = gson.fromJson(json, JsAccessToken.class);
			logger.info("[corpus user access_token] " + accessToken.toString());
			if(accessToken.getOpenid() == null) {
				return "fail";
			}
			
			String openId = accessToken.getOpenid();
			WXuser user = getWXuserByOpenId(openId);
			Map<String, Object> map = new HashMap<>();
			map.put("ownername", user.getNickname());
			map.put("ownerimg", user.getHeadimgurl());
			List<String> userCrewImages = userCrewImageByOpenId(openId);
			map.put("array", userCrewImages);
			List<Map<String, Object>> quizs = userQuizByOpenId(openId);
			map.put("datas", quizs);
			return gson.toJson(map);
		} else if("next".equals(action)){
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String ticket = parameter.getParameter("ticket");
			if(ticket == null) {
				return "fail";
			}
			
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", ticket);
			String openId = token().get(code2OpenIdKey);
			if(openId == null) {
				return "fail";
			}
			Gson gson = new Gson();
			String next = parameter.getParameter("next");
			if(next == null || "".equals(next)) {
				String start = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus");
				List<String> quizIds = new ArrayList<>();
				account().page(start, start, null, Integer.MAX_VALUE, new BiConsumer<String, String>() {

					@Override
					public void accept(String t, String u) {
						//putkv 001.Collect.Corpus.Quiz.T_Corpus_oDqlM1fwmR6XTkKTjalDwMXsi2ME_1534649693345_106.State Hide
						String stateKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz",  u, "State");
						String state = data().get(stateKey);
						logger.info("[next] quizid: " + state + " = " + u);
						if(!"Hide".equals(state)) {
							quizIds.add(u);
						}
					}
				});
				String quizId = quizIds.remove(0);
				Map<String, Object> result = new HashMap<>();
				result.put("array", quizIds);
				Map<String, Object> quiz = new HashMap<>();
				quiz = quizByQuizId(quizId);
				String openIdKey  = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "OpenId");
				String ownerId = data().get(openIdKey);
				WXuser user = getWXuserByOpenId(ownerId);
				quiz.put("ownerimg", user.getHeadimgurl());
				quiz.put("ownername", user.getNickname());
				result.put("quiz", quiz);
				return gson.toJson(result);
			} else {
				String quizId = next;
				Map<String, Object> result = new HashMap<>();
				Map<String, Object> quiz = new HashMap<>();
				quiz = quizByQuizId(quizId);
				String openIdKey  = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "OpenId");
				String ownerId = data().get(openIdKey);
				WXuser user = getWXuserByOpenId(ownerId);
				quiz.put("ownerimg", user.getHeadimgurl());
				quiz.put("ownername", user.getNickname());
				result.put("quiz", quiz);
				return gson.toJson(result);
			}
		} else if("prepay".equals(action)){
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String ticket = parameter.getParameter("ticket");
			if(ticket == null) {
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", ticket);
			String openid = token().get(code2OpenIdKey);
			if(openid == null) {
				return "fail";
			}
			String quizId= parameter.getParameter("goodsid");
			String title  = parameter.getParameter("title");
			String body = "素朴网联-语料";
			String detail = "[" + quizId + ", 1]";
			String quizPriceKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Price");
			String totalcent = data().get(quizPriceKey);
			if(totalcent == null) {
				totalcent = "8";
			}
			String openIdEnd = openid.substring(openid.length() - 7);
			String randEnd = random.substring(random.length() - 4);
			long current = System.currentTimeMillis(); 
			String orderid = current + openIdEnd + randEnd;
			String clientip = ip.split(",")[0];
			logger.info("[corpus prepay] openid:" + openid + ", goodsid:" + quizId + ", title:" + title);
			try {
				return prepay(orderid, body, detail, openid, totalcent, clientip, openid, current);
			} catch (Exception e) {
				return "fail";
			}
		} else if("notify".equals(action)) {
			String postbody = parameter.getParameter(Parameter.POST_BODY);
			Map<String, String> map = WXPayUtil.xmlToMap(postbody);
			logger.info("notify map: " + map.toString());
			String orderid = map.get("out_trade_no");
			String openid = map.get("openid");
			daigouHandler.updateOrderState(orderid, openid, Order.State.Paid);
			long current = System.currentTimeMillis();
			int random = new Random().nextInt(100000);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Notify", ""+current, ""+random, ip), postbody);
			return SUCCESS;
		}
		logger.info(parameter.toString());
		logger.info("[Corpus] return success for any unknown action " + action + " from " + ip);
		return SUCCESS;
	}

	public String prepay(String orderid, String body, String detail, String attach, String totalcent, String clientip, String openid, long current) throws Exception {
		
		long timeStamp = current / 1000;
		WXPayConfig config = new WXPayConfigImpl();
		WXPay wxPay = new WXPay(config);
		logger.info("[corpus prepay] WXPay ready");
		Map<String, String> reqData = new HashMap<>();
		reqData.put("timeStamp", ""+timeStamp);
		reqData.put("device_info", "WEB");
		reqData.put("body", body);
		reqData.put("detail", detail);
		reqData.put("attach", attach);
		reqData.put("out_trade_no", orderid);
		reqData.put("fee_type", "CNY");
		reqData.put("total_fee", totalcent);
		reqData.put("spbill_create_ip", clientip);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		reqData.put("time_start", dateFormat.format(new Date(current)));
		reqData.put("time_expire", dateFormat.format(new Date(current + TimeUnit.HOURS.toMillis(2))));
		reqData.put("notify_url", "http://suppresswarnings.com/notify.http");
		reqData.put("trade_type", "JSAPI");
		reqData.put("product_id", orderid);
		reqData.put("openid", openid);
		logger.info("[corpus prepay] reqData: " + reqData.toString());
		Map<String, String> resultData = wxPay.unifiedOrder(reqData);
		logger.info("[corpus prepay] unifiedOrder result ready");
		
		Map<String, String> result = new HashMap<>();
    	String appid = resultData.get("appid");
    	String nonceStr = resultData.get("nonce_str");
    	String prepay_id = resultData.get("prepay_id");
    	
    	result.put("appId", appid);
    	result.put("timeStamp", ""+timeStamp);
    	result.put("nonceStr", nonceStr);
    	result.put("package", "prepay_id=" + prepay_id);
    	result.put("signType", SignType.HMACSHA256.name());
    	//sign
    	String sign = wxPay.sign(result, SignType.HMACSHA256);
		result.put("paySign", sign);
		logger.info("[corpus prepay] paySign ready");
		
		Gson gson = new Gson();
		String unifiedOrder = gson.toJson(result);
		logger.info("[corpus prepay] unifiedOrder OK: " + unifiedOrder);
		String reqjson = gson.toJson(reqData);
		String resultjson = gson.toJson(resultData);
		account().put(String.join(Const.delimiter, Const.Version.V1, "Order", "Orderid", orderid), orderid);
		account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "UnifiedOrder"), unifiedOrder);
		account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Reqjson"), reqjson);
		account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Resultjson"), resultjson);
		account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "State"), "Wait");
		return unifiedOrder;
		
	}
	public String xml(String openid, String msg, String fromOpenId) {
		if(msg == null || msg.length() < 1) {
			logger.error("[Corpus] empty response");
			return SUCCESS;
		}
		long time = System.currentTimeMillis()/1000;
		if(msg.startsWith("news://")) {
			Gson gson = new Gson();
			String json = msg.substring(7);
			WXnews news = gson.fromJson(json, WXnews.class);
			return String.format(Const.WXmsg.news, openid, fromOpenId, "" + time, news.getTitle(), news.getDescription(), news.getPicUrl(), news.getUrl());
		}
		return String.format(Const.WXmsg.xml, openid, fromOpenId, "" + time, msg);
	}
	public Map<String, Object> quizByQuizId(String quizId) {
		Map<String, Object> map = new HashMap<>();
		String quizKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId);//001.Collect.Corpus.Quiz.T_Corpus_oDqlM1TyKpSulfMC2OsZPwhi-9Wk_1534646328739_890
		String quiz = data().get(quizKey);
		String quizStateKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "State");
		String state = data().get(quizStateKey);
		String quizSampleKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Sample");
		String sample = data().get(quizSampleKey);
		if(state == null) state = "0";
		String quizQRCodeKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "QRCode");
		String qrCode = data().get(quizQRCodeKey);
		Gson gson = new Gson();
		QRCodeTicket ticket = gson.fromJson(qrCode, QRCodeTicket.class);
		String quizPriceKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Price");
		String price = data().get(quizPriceKey);
		if(price == null) {
			price = "8";
		}
		
		map.put("quiz", quiz);
		map.put("sample", sample);
		map.put("quizId", quizId);
		map.put("quizState", state);
		map.put("qrcode", "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket.getTicket());
		map.put("price", price);
		return map;
	}
	public List<Map<String, Object>> userQuizByOpenId(String openId) {
		List<Map<String, Object>> quizs = new ArrayList<>();
		String quizKey = String.join(Const.delimiter, Const.Version.V1, openId, "Collect", "Corpus");
		List<String> quizIds = new ArrayList<>();
		account().page(quizKey, quizKey, null, 100, new BiConsumer<String, String>() {

			@Override
			public void accept(String t, String u) {
				quizIds.add(u);
			}
		});
		for(String quizId : quizIds) {
			Map<String, Object> quiz = quizByQuizId(quizId);
			quizs.add(quiz);
		}
		return quizs;
	}
	
	public List<String> userCrewImageByOpenId(String openId) {
		//001.Collect.Corpus.Quiz.T_Corpus_oDqlM1TyKpSulfMC2OsZPwhi-9Wk_1534646328739_890.QRCode
		String crewKey = String.join(Const.delimiter, Const.Version.V1, openId, "Crew");
		List<String> crews = new ArrayList<>();
		account().page(crewKey, crewKey, null, 100, new BiConsumer<String, String>() {

			@Override
			public void accept(String t, String u) {
				if(t.length() > crewKey.length()) {
					String crew = t.substring(crewKey.length() + 1);
					logger.info("[Corpus user crew]: " + crew + " time: " + u);
					crews.add(crew);
				}
			}
		});
		List<String> images = new ArrayList<>();
		for(String crew : crews) {
			WXuser user = getWXuserByOpenId(crew);
			if(user.getSubscribe() == 0) continue;
			images.add(user.getHeadimgurl());
		}
		return images;
	}
	public List<String> collectCrewImageByQuizId(String quizId) {
		//001.Collect.Corpus.Quiz.T_Corpus_oDqlM1TyKpSulfMC2OsZPwhi-9Wk_1534646328739_890.QRCode
		String crewKey = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Crew");
		List<String> crews = new ArrayList<>();
		logger.info("[corpus collect crew image by quizId] " + quizId);
		data().page(crewKey, crewKey, null, 100, new BiConsumer<String, String>() {

			@Override
			public void accept(String t, String u) {
				if(t.length() > crewKey.length()) {
					String crew = t.substring(crewKey.length() + 1);
					crews.add(crew);
				}
			}
		});
		List<String> images = new ArrayList<>();
		for(String crew : crews) {
			WXuser user = getWXuserByOpenId(crew);
			if(user.getSubscribe() == 0) continue;
			images.add(user.getHeadimgurl());
		}
		return images;
	}
	/**
	 * 001.User.openidvalue = time
	 * @param openId
	 */
	public void subscribe(String openId, String time) {
		String userKey = String.join(Const.delimiter, Const.Version.V1, "User", openId);
		account().put(userKey, time);
	}
	public String globalCommand(String sceneOrCommand) {
		String nowCommandKey = String.join(Const.delimiter, "Setting", "Global", "Command", sceneOrCommand.toLowerCase());
		String exchange = account().get(nowCommandKey);
		return exchange;
	}
	public void setGlobalCommand(String newCommand, String command, String openId, String time) {
		String nowCommandKey = String.join(Const.delimiter, "Setting", "Global", "Command", newCommand.toLowerCase());
		String infoKey = String.join(Const.delimiter, "Setting", "Info", "Global", "Command", newCommand.toLowerCase());
		String info = String.join(Const.delimiter, newCommand, openId, time);
		account().put(nowCommandKey, command);
		account().put(infoKey, info);
	}
	/**
	 * 001.openidvalue.User = {user info json}
	 * @param openId
	 * @return
	 */
	public WXuser getWXuserByOpenId(String openId) {
		WXuser user = null;
		Gson gson = new Gson();
		String accessToken = accessToken("User Info");
		
		String userKey = String.join(Const.delimiter, Const.Version.V1, openId, "User");
		String json = account().get(userKey);
		if(json == null) {
			logger.info("[Corpus] get WXuser info: " + openId);
			CallableGet get = new CallableGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN", accessToken, openId);
			try {
				json = get.call();
				user = gson.fromJson(json, WXuser.class);
				account().put(userKey, json);
			} catch (Exception e) {
				logger.error("[WXContext] fail to get user info: " + openId, e);
			}
		} else {
			user = gson.fromJson(json, WXuser.class);
			if(user.getSubscribe() == 0) {
				logger.info("[Corpus] get WXuser info: " + openId);
				CallableGet get = new CallableGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN", accessToken, openId);
				try {
					json = get.call();
					user = gson.fromJson(json, WXuser.class);
				} catch (Exception e) {
					logger.error("[WXContext] fail to get user info: " + openId, e);
				}
			}
			account().put(userKey, json);
		}
		
		if(user == null) {
			logger.info("[WXContext] fail to get user info: use default");
			user = new WXuser();
			user.setSubscribe(0);
			user.setOpenid(openId);
		}
		return user;
	}
	public String sendTxtTo(String business, String message, String openid) {
		String accessToken = accessToken(business);
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + accessToken;
		String json = "{\"touser\":\"" + openid + "\",\"msgtype\":\"text\",\"text\":{\"content\":\"" + message + "\"}}";
		CallablePost post = new CallablePost(url, json);
		try {
			String result = post.call();
			logger.info("[corpus] send text to: " + openid + ", result: " + result);
			return result;
		} catch (Exception e) {
			logger.error("[corpus] fail to send text to user: " + openid, e);
			return null;
		}
	}
	
	public boolean isAdmin(String openid, String whatfor, String time) {
		String adminKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Auth", "Admin", openid);
		String admin = account().get(adminKey);
		if(admin != null && !"None".equals(admin)) {
			logger.info("[CorpusService authrized] Use Admin power, openid: " + openid + ", whatfor: " + whatfor + ", time: " + time);
			return true;
		}
		return false;
	}
	
	public boolean authrized(String openid, String auth) {
		String time = "" + System.currentTimeMillis();
		if(isAdmin(openid, "Check:" + auth, time)) {
			return true;
		}
		String authKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Auth", auth, openid);
		String authrized = account().get(authKey);
		if(authrized == null || "None".equals(authrized)) {
			return false;
		}
		logger.info("[CorpusService authrized] Use auth power, openid: " + openid + ", auth: " + auth + ", time: " + time);
		return true;
	}
	public void dealWithTicket(String ticket, String openid) {
		if(ticket != null) {
			String expires = token().get(String.join(Const.delimiter, Const.Version.V1, "Temp", "QRCode", "Login", ticket));
			if(expires != null) {
				if("1".equals(expires)) {
					
				}
				long expire = Long.parseLong(expires);
				if(expire > System.currentTimeMillis()) {
					token().put(String.join(Const.delimiter, Const.Version.V1, "Temp", "QRCode", "Login", ticket), "1");
					token().put(String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", ticket), openid);
				}
			}
		}
	}
	public String accessToken(String business) {
		logger.info("[corpus] get access token for " + business);
		return token().get(String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", "973rozg"));
	}
	public String qrCode(String accessToken, int expire_seconds, String actionName, String scene_str) {
		String json = "{\"expire_seconds\": " + expire_seconds + ", \"action_name\": \""+actionName+"\", \"action_info\": {\"scene\": {\"scene_str\": \""+scene_str+"\"}}}";
		String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + accessToken;
		CallablePost post = new CallablePost(url, json);
		try {
			return post.call();
		} catch (Exception e) {
			return null;
		}
	}
	public String getSHA1(String token, String timestamp, String nonce, String encrypt) {
		if(CheckUtil.anyNull(token, timestamp, nonce, encrypt)) {
			return null;
		}
		try {
			String[] array = new String[] { token, timestamp, nonce, encrypt };
			StringBuffer sb = new StringBuffer();
			Arrays.sort(array);
			for (int i = 0; i < 4; i++) {
				sb.append(array[i]);
			}
			String str = sb.toString();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();
			StringBuffer hexstr = new StringBuffer();
			String shaHex = "";
			for (int i = 0; i < digest.length; i++) {
				shaHex = Integer.toHexString(digest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexstr.append(0);
				}
				hexstr.append(shaHex);
			}
			return hexstr.toString();
		} catch (Exception e) {
			logger.error("sha-1 error", e);
			return null;
		}
	}
	

	public Context<?> context(String openid) {
		return contexts.get(openid);
	}
	public void context(String openid, Context<?> context) {
		contexts.put(openid, context);
	}
	public void contextx(String openid, Context<?> context, long timeToLiveMillis) {
		expire(openid, timeToLiveMillis);
		contexts.put(openid, context);
	}
	public void expire(String name, long timeToLiveMillis) {
		long now = System.currentTimeMillis();
		TTL e = new TTL(now + timeToLiveMillis, name);
		TTL old = secondlife.remove(name);
		logger.info("[corpus] expire exist for " + name + " = " + old);
		if(old != null) {
			if(old.marked()) {
				ttl.remove(old);
				ttl.offer(e);
			} else {
				if(old.ttl() < e.ttl()) {
					ttl.remove(old);
					ttl.offer(e);
				}
				//don't offer this TTL since it is still short than old one
			}
		} else {
			ttl.removeIf(out -> {
				if(out.equals(e) && out.ttl() < e.ttl()) {
					return true;
				}
				return false;
			});
			ttl.offer(e);
		}
	}
	
	public void clear(){
		long now = System.currentTimeMillis();
		logger.info("[corpus] start clean TTL("+ttl.size()+")");
		ttl.removeIf(out -> {
			if(out.ttl() < now) {
				if(out.marked()) {
					logger.info("[corpus] remove key: " + out.key());
					secondlife.remove(out.key());
					contexts.remove(out.key());
					return true;
				} else {
					out.mark();
					secondlife.put(out.key(), out);
				}
			}
			return false;
		});
		logger.info("[corpus] after clean TTL("+ttl.size()+")");
	}
	
	public static void main(String[] args) {}
}
