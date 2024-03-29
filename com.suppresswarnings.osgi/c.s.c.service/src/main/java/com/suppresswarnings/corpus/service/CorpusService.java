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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.ACrypt;
import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.Format;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.corpus.common.SendMail;
import com.suppresswarnings.corpus.common.TTL;
import com.suppresswarnings.corpus.common.Type;
import com.suppresswarnings.corpus.service.aiiot.AIIoT;
import com.suppresswarnings.corpus.service.aiiot.Things;
import com.suppresswarnings.corpus.service.backup.Server;
import com.suppresswarnings.corpus.service.daigou.DaigouHandler;
import com.suppresswarnings.corpus.service.game.Guard;
import com.suppresswarnings.corpus.service.handlers.DaigouHandlerFactory;
import com.suppresswarnings.corpus.service.handlers.ManagementHandlerFactory;
import com.suppresswarnings.corpus.service.handlers.MiniprogramHandlerFactory;
import com.suppresswarnings.corpus.service.handlers.NotifyHandlerFactory;
import com.suppresswarnings.corpus.service.handlers.PingHandlerFactory;
import com.suppresswarnings.corpus.service.handlers.QRCodeHandlerFactory;
import com.suppresswarnings.corpus.service.handlers.ShortUrlHandlerFactory;
import com.suppresswarnings.corpus.service.http.CallableDownload;
import com.suppresswarnings.corpus.service.http.CallableGet;
import com.suppresswarnings.corpus.service.http.CallablePost;
import com.suppresswarnings.corpus.service.http.HttpClientHolder;
import com.suppresswarnings.corpus.service.sdk.MiniPayConfigImpl;
import com.suppresswarnings.corpus.service.sdk.WXPay;
import com.suppresswarnings.corpus.service.sdk.WXPayConfig;
import com.suppresswarnings.corpus.service.sdk.WXPayConfigImpl;
import com.suppresswarnings.corpus.service.sdk.WXPayConstants.SignType;
import com.suppresswarnings.corpus.service.sdk.WXPayUtil;
import com.suppresswarnings.corpus.service.work.Counter;
import com.suppresswarnings.corpus.service.work.Quiz;
import com.suppresswarnings.corpus.service.work.WorkHandler;
import com.suppresswarnings.corpus.service.wx.ATuser;
import com.suppresswarnings.corpus.service.wx.AccessToken;
import com.suppresswarnings.corpus.service.wx.JsAccessToken;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXnews;
import com.suppresswarnings.corpus.service.wx.WXresult;
import com.suppresswarnings.corpus.service.wx.WXuser;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class CorpusService implements HTTPService, CommandProvider {
	public static final String SUCCESS = "success";
	public static final String SENDOK = "{\"errcode\":0,\"errmsg\":\"ok\"}";
	public static final String STUPID = "oDqlM1TyKpSulfMC2OsZPwhi-9Wk";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	
	class Expire {
		long last;
		long expire;

		public Expire(long wait) {
			this.last = System.currentTimeMillis();
			this.expire = last + wait;
		}
		
		public boolean expired() {
			return System.currentTimeMillis() > expire;
		}
		
		public void reset() {
			this.last = System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return "Expire [" + last + " -> " + (expire - last) + "]";
		}
	}
	
	
	class CashoutRunnable implements Runnable {
		String approver;
		String openid;
		int cent;
		long time;
		long runTime;
		String result;
		
		public String approverName() {
			WXuser a = getWXuserByOpenId(approver);
			if(a == null || a.getSubscribe() == 0) {
				return "已取消：" + approver;
			}
			String name = getRealValue(approver);
			if(name != null) {
				return name;
			}
			return a.getNickname();
		}
		
		public String userName() {
			WXuser a = getWXuserByOpenId(openid);
			if(a == null || a.getSubscribe() == 0) {
				return "已取消：" + openid;
			}
			String name = getRealValue(openid);
			if(name != null) {
				return name;
			}
			return a.getNickname();
		}
		
		public String getApprover() {
			return approver;
		}

		public void setApprover(String approver) {
			this.approver = approver;
		}

		public String getOpenid() {
			return openid;
		}

		public void setOpenid(String openid) {
			this.openid = openid;
		}

		public int getCent() {
			return cent;
		}

		public void setCent(int cent) {
			this.cent = cent;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public long getRunTime() {
			return runTime;
		}

		public void setRunTime(long runTime) {
			this.runTime = runTime;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}
		
		CashoutRunnable(String approver, String openid, int cent) {
			this.approver = approver;
			this.openid = openid;
			this.cent = cent;
			this.time = System.currentTimeMillis();
		}
		
		@Override
		public String toString() {
			return "CashoutRunnable [approver=" + getApprover() + ", openid=" + getOpenid() + ", cent=" + cent + ", time=" + time
					+ ", runTime=" + runTime + ", result=" + result + "]";
		}

		@Override
		public void run() {
			this.runTime = System.currentTimeMillis();
			logger.info("[CashoutRunnable] approved cashout start " + runTime);
			String ret = reward(approverName() + "批准" + userName() + "提现" + cent + "分，时间" + new Date(), openid, cent);
			this.result = ret;
			logger.info("[CashoutRunnable] approved cashout result " + this);
			save();
		}
		
		public void save() {
			account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Approve", "Cashout", approver, openid, ""+time), gson.toJson(this));
			logger.info("[CashoutRunnable] approved cashout saved");
		}
	}
	
	public Format format = new Format(Const.WXmsg.msgFormat);
	Map<String, TTL> secondlife = new ConcurrentHashMap<String, TTL>();
	LinkedBlockingQueue<TTL> ttl = new LinkedBlockingQueue<TTL>(100000);
	public Map<String, Provider<?>> providers = new HashMap<>();
	public Map<String, ContextFactory<CorpusService>> factories = new HashMap<>();
	public Map<String, Context<?>> contexts = new ConcurrentHashMap<String, Context<?>>();
	public Map<String, WXuser> users = new ConcurrentHashMap<String, WXuser>();
	public Map<String, Expire> caches = new ConcurrentHashMap<String, Expire>();
	public ExecutorService threadpool = new ThreadPoolExecutor(2, 10, 7200L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1000));
	public ThreadPoolExecutor atUserPool = new ThreadPoolExecutor(2, 10, 7200L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100000), new ThreadFactory() {
		int index = 0;
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "@User-" + index++);
		}
	}, new RejectedExecutionHandler(){

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			if(r instanceof ATuser) {
				ATuser user = (ATuser) r;
				user.save();
			}
		}
	});
	public ScheduledExecutorService schedule = Executors.newScheduledThreadPool(10, new ThreadFactory() {
		int index = 0;
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "schedule-" + index++);
		}
	});
	public Gson gson = new Gson();
	public LevelDB account, data, token;
	Server backup;
	public AIIoT aiiot;
	public Guard guard;
	public DaigouHandler daigouHandler;
	public WorkHandler workHandler;
	ScheduledExecutorService scheduler;
	public HashSet<String> uniqueMsgid = new HashSet<>();
	public AtomicBoolean ready = new AtomicBoolean(false);
	
	public static final String[] formats = {
			"001.Collect.Corpus.Quiz.{QuizId}.Answer.{AnswerOpenid}.{AnswerTime}.{_}.Reply.{ReplyOpenid}.{ReplyTime}.{_}", 
			"001.Collect.Corpus.Quiz.{QuizId}.Answer.{AnswerOpenid}.{AnswerTime}.{_}.Similar.{SimilarOpenid}.{SimilarTime}.{_}"
	};
	public static final Format quizAnswerReplyOrSimilar = new Format(formats);
	public static final long STOP_THE_WORLD = 2000000000000l;
	public Map<String, Counter> counters = new ConcurrentHashMap<>();
	public Map<String, AtomicInteger> atomicIds = new ConcurrentHashMap<>();
	public Map<String, AtomicInteger> incrementers = new ConcurrentHashMap<>();
	public Map<String, AtomicBoolean> atomicSwitches = new ConcurrentHashMap<>();
	public Map<String, String> questionToAid = new ConcurrentHashMap<>();
	public Map<String, HashSet<String>> aidToAnswers = new ConcurrentHashMap<>();
	public Map<String, HashSet<String>> aidToSimilars = new ConcurrentHashMap<>();
	public Map<String, String> aidToCommand = new ConcurrentHashMap<>();
	//
	public AtomicInteger bear = new AtomicInteger(2);
	public AtomicInteger corpusCount = new AtomicInteger(0);
	public List<Quiz> assimilatedQuiz = Collections.synchronizedList(new ArrayList<>());
	public Map<String, KeyValue> notifyAdmins = new ConcurrentHashMap<>();
	List<Map<String, Object>> list = new ArrayList<>();
	Map<String, Runnable> approvedRunnable = new ConcurrentHashMap<>();
	Map<String, KeyValue> tobeApproved = new ConcurrentHashMap<>();
	
	//mqtt
	String clientId;
    MqttClient mqttClient;
    
	public Set<String> todoSet() {
		return tobeApproved.keySet();
	}
	public KeyValue approve(String openid) {
		return tobeApproved.remove(openid);
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
		Provider<?> provider = providers.get(key);
		if(provider == null) {
			logger.error("[corpus] get null from providers by key: " + key);
			return new DefaultLevelDB(key);
		}
		LevelDB instance = (LevelDB) provider.instance();
		return instance;
	}
	//TODO bugfix:synchronized
	public synchronized AtomicBoolean switches(String key) {
		AtomicBoolean switches = atomicSwitches.get(key);
		if(switches == null) {
			switches = new AtomicBoolean(false);
			atomicSwitches.put(key, switches);
		}
		return switches;
	}
	
	public void publish(String topic, String payload) {
		String md5sum = ACrypt.md5(payload + payload);
		String check = topic + "/" + md5sum;
		MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        try {
			mqttClient.publish(check, message);
		} catch (Exception e) {
			logger.error("fail to publish message to mqtt", e);
		}
	}
	
	public void atUser(String openid, String message) {
		logger.info("atUser " + openid + ": " + message);
		ATuser user = new ATuser(this, openid, message, System.currentTimeMillis());
		atUserPool.execute(user);
	}
	
	public String youGotMe(String openId, String quiz, String quizId) {
		this.workHandler.clockOut(openId);
		return this.workHandler.newJob(quiz, quizId, openId);
	}
	
	public void tellAdmins(String openid, String msg) {
		String tellAdminsKey = String.join(Const.delimiter, Const.Version.V1, "Info", "TellAdmins", openid, "" + System.currentTimeMillis(), uniqueKey("TellAdmins"));
		account().put(tellAdminsKey, msg);
	}
	
	public void tellBossNow(String openid, String msg) {
		String boss = account().get(String.join(Const.delimiter, Const.Version.V1, openid, "Boss"));
		if(boss == null || "None".equals(boss)) {
			logger.error("boss is null " + openid);
		} else {
			decrement(boss);
			atUser(boss, msg);
		}
	}
	public void approvedRunnable(String approver, String openid, int cent) {
		approvedRunnable.put(openid, new CashoutRunnable(approver, openid, cent));
		logger.warn(approver + "审批" +openid+ "提现" + cent + "分");
	}
	public void rejectRunnable(String approver, String openid, int cent) {
		approvedRunnable.remove(openid);
		atUser(openid, "提现申请被拒绝了，金额：" + cent + "分");
	}
//	public void connectChat(String wxid, String openid, String ask) {
//		java.util.Set<String> set = users.keySet();
//		WXuser myself = getWXuserByOpenId(openid);
//		logger.info("[coonect chat] users set: " + set);
//		for(String userid : set) {
//			if(openid.equals(userid)) {
//				logger.info("[connect chat] myself");
//				continue;
//			}
//			String ret = sendTxtTo("connect chat", "["+myself.getNickname()+"]" + ask, userid);
//			WXuser user = users.remove(userid);
//			logger.info("[coonect chat] after remove, users set: " + users.keySet());
//			if(SENDOK.equals(ret)) {
//				ChatContext chatContext = new ChatContext(wxid, userid, openid, this);
//				contextx(userid, chatContext, TimeUnit.MINUTES.toMillis(3));
//				logger.info("[connect chat] sent msg to user, ready to connect chat: " + user);
//				break;
//			} else {
//				logger.info("[connect chat] fail to connect user: " + user);
//			}
//		}
//	}
	
	public void forgetIt(String openid) {
		logger.info("[corpus] forgetIt: " + openid);
		this.workHandler.forgetIt(openid);
	}
	public boolean iWantJob(String openid, Type type) {
		logger.info("[corpus] iWantJob: " + openid + ", type: " + type.name());
		return this.workHandler.clockIn(openid, type);
	}
	
	public boolean offWork(String openid) {
		logger.info("[corpus] offWork: " + openid);
		return this.workHandler.clockOut(openid);
	}
	public String remoteCall(String openid, String code, String cmd, String input) {
		Things thing = aiiot.things.get(code);
		StringBuffer ret = new StringBuffer();
		logger.info("[corpus] remoteCall "+ thing);
		if(thing == null) {
			logger.info("[corpus] remoteCall but thing is null for code: " + code);
		} else if(thing.isClosed()) {
			logger.info("[corpus] thing is closed: " + thing.toString());
		} else {
			logger.info("[corpus] remoteCall("+ openid +", "+thing+", "+cmd + ";" + input+")");
			String result = thing.execute(cmd, input);
			if(result != null) ret.append(result);
		}
		return ret.toString();
	}
	public String aiiot(String wxid, String openid, String code, String input, String origin, Context<CorpusService> context) {
		if(code.contains(";")) {
			StringBuffer sb = new StringBuffer();
			String[] codes = code.split(";");
			StringBuffer now = new StringBuffer();
			for(String thing : codes) {
				String ret = aiiot.remoteCall(wxid, openid, thing, input, origin, context);
				if(ret == null) {
					logger.error("[corpus aiiot] return null: "+ thing);
				} else {
					now.append(thing).append(";");
					sb.append(thing).append("=").append(ret).append(";");
				}
			}
			if(now.length() > 1) now.deleteCharAt(now.length() - 1);
			String keyCMD = String.join(Const.delimiter, Const.Version.V1, openid, "AIIoT", input);
			context.content().account().put(keyCMD, now.toString());
			return sb.toString();
		} else {
			return aiiot.remoteCall(wxid, openid, code, input, origin, context);
		}
	}
	public String toJson(Object obj) {
		return gson.toJson(obj);
	}
	public void log(String clazz, String info) {
		logger.info("[" + clazz + "] " + info);
	}
	
	public void clearUniqueMsgid() {
		if(uniqueMsgid.size() > 1000) {
			uniqueMsgid.clear();
		}
	}
	
	public boolean sameMsgid(String msgid) {
		if(uniqueMsgid.contains(msgid)) {
			clearUniqueMsgid();
			return true;
		}
		clearUniqueMsgid();
		uniqueMsgid.add(msgid);
		return false;
	}
	
	public Guard guard() {
		return guard;
	}
	public void activate() {
		CorpusService that = this;
		logger.info("[corpus] activate");
		System.out.println("服务正在启动");
		//mqtt
		try {
			clientId = SuppressWarnings.class.getSimpleName();
			MemoryPersistence persistence = new MemoryPersistence();
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(clientId);
			connOpts.setPassword(clientId.toCharArray());
			mqttClient = new MqttClient("tcp://suppresswarnings.com:1883", clientId, persistence);
			mqttClient.connect(connOpts);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		scheduler = Executors.newScheduledThreadPool(10, new ThreadFactory() {
			int index = 0;
			@Override
			public Thread newThread(Runnable r) {
				index ++;
				return new Thread(r, "scheduler-" + index);
			}
		});
		
		if(backup != null) backup.close();
		if(aiiot != null) aiiot.close();
		if(workHandler != null) workHandler.close();
//		if(guard == null) guard = new Guard(that);
//		//1
//		scheduler.scheduleWithFixedDelay(guard, 3, 10, TimeUnit.SECONDS);
		scheduler.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					logger.info("[corpus] it will execute after 1s");
					TimeUnit.SECONDS.sleep(1);
					logger.info("[corpus] start to execute");
					backup = new Server();
					backup.working();
					logger.info("[corpus] backup working");
				} catch (Exception e) {
					logger.error("[corpus] fail to delay execute", e);
				}
			}
		});
		//2
		scheduler.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					logger.info("[corpus] it will execute after 1s");
					TimeUnit.SECONDS.sleep(1);
					logger.info("[corpus] start to execute");
					aiiot = new AIIoT(that);
					aiiot.working();
					logger.info("[corpus] aiiot working");
				} catch (Exception e) {
					logger.error("[corpus] fail to delay execute", e);
				}
			}
		});
		//3
		scheduler.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					logger.info("[corpus] it will execute after 2s");
					Thread.sleep(2000);
					logger.info("[corpus] start to execute");
					
					workHandler = new WorkHandler(that, Const.WXmsg.openid);
					workHandler.working();
					logger.info("[corpus] workHandler working");
				} catch (Exception e) {
					logger.error("[corpus] fail to delay execute", e);
				}
			}
		});
		//4
		scheduler.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					logger.info("[corpus] it will execute after 2s");
					Thread.sleep(2000);
					logger.info("[corpus] start to execute");
					daigouHandler = new DaigouHandler(that);
					ready.set(true);
				} catch (Exception e) {
					logger.error("[corpus] fail to delay execute", e);
				}
			}
		});
		//5
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				workHandler.report();
				//TODO send email
				long now = System.currentTimeMillis();
				int currentTTL = ttl.size();
				logger.info("[corpus run] start clean TTL("+currentTTL+")");
				clearUniqueMsgid();
				ttl.removeIf(out -> {
					if(out.ttl() < now) {
						if(out.marked()) {
							logger.info("[corpus run] remove key: " + out.key());
							secondlife.remove(out.key());
							Context<?> context = contexts.remove(out.key());
							context.exit();
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
		}, TimeUnit.SECONDS.toMillis(6), TimeUnit.MINUTES.toMillis(5), TimeUnit.MILLISECONDS);
		logger.info("[corpus] TTL scheduler starts in 6s");
		//NOTE access token
		//6
		scheduler.scheduleAtFixedRate(new Runnable() {
			int times = 0;
			long last = System.currentTimeMillis();
			String key = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", "973rozg");
			String expireKey = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Expire", "973rozg");
			String jskey = String.join(Const.delimiter, Const.Version.V1, "JsAccessToken", "Token", "973rozg");
			String jsexpireKey = String.join(Const.delimiter, Const.Version.V1, "JsAccessToken", "Expire", "973rozg");
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
					AccessToken at = gson.fromJson(json, AccessToken.class);
					String access = at.getAccess_token();
					long expireAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(7200);
					int result = token().put(key, access);
					token().put(expireKey, "" + expireAt);
					logger.info("[access token] refresh " + result + ", expires at " + at.getExpires_in());
					
					CallableGet jsget = new CallableGet("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi", access);
					String jsjson = jsget.call();
					@SuppressWarnings("unchecked")
					Map<String, Object> map = gson.fromJson(jsjson, Map.class);
					
					String ticket = (String) map.get("ticket");
					token().put(jskey, ticket);
					token().put(jsexpireKey, "" + expireAt);
					logger.info("[js access token] refresh " + jsjson);
					accessTokens();
					hitokoto();
				} catch (Exception e) {
					logger.error("[access token] Exception when refresh", e);
				}
			}
		}, TimeUnit.SECONDS.toMillis(6), TimeUnit.SECONDS.toMillis(7200), TimeUnit.MILLISECONDS);
		logger.info("[corpus] refresh access token scheduler starts in 6s");
		//NOTE notice report
		//7
		scheduler.scheduleAtFixedRate(new Runnable() {
			int times = 0;
			@Override
			public void run() {
				times ++;
				Calendar c = Calendar.getInstance();
				int hour = c.get(Calendar.HOUR_OF_DAY);
				if(hour < 7) {
					logger.info("[corpus] it's too early, no need to send report");
					return;
				}
				
				String admins = account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Admins"));
				if(admins == null) {
					logger.error("[corpus] scheduler: the admins not set");
					return;
				}
				AtomicInteger cnt = new AtomicInteger(0);
				incrementers.forEach((k, a) -> {
					logger.info("[VIP] " + k + " invited " + a.get());
					cnt.getAndAdd(a.get());
					account().put(k, "" + a.get());
				});
				
				updateWXuser();
				String[] admin = admins.split(",");
				StringBuffer info = new StringBuffer();
				info.append("仅通知管理员\n周期汇报：第").append(times).append("次").append("\n");
				info.append("VIP邀请：" + cnt.get() + "次").append("\n");
				info.append("用户统计：" + users.size()).append("\n");
				info.append("当前对话: " + contexts.size()).append("\n");
				info.append("组件统计：" + providers.size())
					.append('/').append(factories.size())
					.append('/').append(aiiot.things.size())
					.append("\n");
				info.append("问题统计: " + questionToAid.size())
					.append("/").append(aidToAnswers.size())
					.append("/").append(corpusCount.get()).append("\n");
				info.append("待审批：").append(tobeApproved.size()).append(",已执行：").append(atUserPool.getCompletedTaskCount()).append("\n");
//				info.append("后台工作：\n" + workHandler.report());
				
				String retrieveKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Retrieve", "TellAdmins", "Last");
				String lastRetrieved = account().get(retrieveKey);
				String tellAdminsKey = String.join(Const.delimiter, Const.Version.V1, "Info", "TellAdmins");
				String start = null;
				AtomicReference<String> last = new AtomicReference<String>();
				if(isNull(lastRetrieved)) {
					start = tellAdminsKey;
				} else {
					start = lastRetrieved;
				}
				last.set(start);
				List<String> tellAdmins = new ArrayList<String>();
				account().page(tellAdminsKey, start, null, 50, (k, v) ->{
					last.set(k);
					tellAdmins.add(v);
				});
				account().put(retrieveKey, last.get());
				for(String tell: tellAdmins) {
					uniqueKey(tell);
				}
				info.append("访问计数：");
				atomicIds.forEach((k, v) ->{
					info.append("\n" + k + "\t" + v.get());
				});
				
				for(String one : admin) {
					atUser(one, info.toString());
				}
				Set<String> openids = approvedRunnable.keySet();
				openids.forEach(action -> {
					Runnable runnable = approvedRunnable.remove(action);
					threadpool.execute(runnable);
				});
			}
		}, TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(120), TimeUnit.MILLISECONDS);
		logger.info("[corpus] scheduler admins info starts in 5 minutes");
	}

	public void deactivate() {
		logger.info("[corpus] deactivate.");
		contexts.forEach((openid, ctx) -> {
			sendTxtTo("@User", "我闭关修炼10分钟，先告辞了！", openid);
		}) ;
		
		
		if(backup != null) {
			backup.close();
		}
		backup = null;
		if(aiiot != null) {
			aiiot.close();
		}
		aiiot = null;
		if(scheduler != null) {
			scheduler.shutdownNow();
		}
		if(workHandler != null){
			workHandler.close();
		}
		scheduler = null;
		secondlife.clear();
		ttl.clear();
		providers.clear();
		factories.clear();
		contexts.clear();
		counters.clear();
		questionToAid.clear();
		aidToAnswers.clear();
		aidToSimilars.clear();
		assimilatedQuiz.clear();
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
		buffer.append("\t findn - findn <startkey> <what> <limit> - find them and delete some key-values by what value.\n");
		buffer.append("\t action - action <method name> - invoke method\n");
		buffer.append("\t img_sec_check - img_sec_check <key> - do img_sec_check\n");
		buffer.append("\t access - access <appid> - get access token and keep in token()\n");
		
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
	
	public void _access(CommandInterpreter ci) {
		logger.info("[_access] " + leveldb);
		accessTokens();
		ci.println("[_access] finish");
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
			leveldb.put(String.join(Const.delimiter, Const.Version.V1, "Corpus", "Delete", ""+System.currentTimeMillis(), "oDqlM1TyKpSulfMC2OsZPwhi-9Wk", "Quizid", k), v);
			leveldb.del(k);
			int index = i.incrementAndGet();
			ci.println("[_deleten] " + index + ". remove key:" + k + " = " + v);
		});
	}
	
	public void _img_sec_check(CommandInterpreter ci) {
		logger.info("[img_sec_check] " + leveldb);
		if(leveldb == null) return;
		String key = ci.nextArgument();
		String todo = leveldb.get(key);
		ci.println("[img_sec_check] key: " + key + " todo: " + todo);
		LevelDB db = leveldb;
		if(key.endsWith("Account")) {
			db = account();
		} else if(key.endsWith("Data")) {
			db = data();
		}
		String value = db.get(todo);
		if(isNull(value)) {
			ci.println("[img_sec_check] value: null");
			leveldb.put(String.join(Const.delimiter, Const.Version.V1, "NULL", "imgSecCheck"), key);
		} else {
			ci.println("[img_sec_check] value: " + value);
			boolean ret = imgSecCheck(value);
			leveldb.put(String.join(Const.delimiter, Const.Version.V1, "DONE", "imgSecCheck"), key);
			ci.println("[img_sec_check] alert: " + ret);
			if(!ret) {
				db.put(todo, "alert.png#" + value);
				logger.info("[img_sec_check] update image to alert.png: " + todo);
			}
		}
	}
	
	public void _findn(CommandInterpreter ci) {
		logger.info("[_findn] " + leveldb);
		ci.println("[_findn] startKey what n");
		if(leveldb == null) return;
		String start = ci.nextArgument();
		String what  = ci.nextArgument();
		String limit = ci.nextArgument();
		int n = Integer.valueOf(limit);
		AtomicInteger index = new AtomicInteger(0);
		AtomicInteger count = new AtomicInteger(0);
		leveldb.page(start, start, null, n, (k,v)->{
			if(v.contains(what)) {
				ci.println("[_findn] found key." +count.get()+ " = " + k);
				ci.println("[_findn] found val." +count.get()+ " = " + v);
				count.incrementAndGet();
			}
		});
		ci.println("[_findn] find " + index.get() + " / " + count.get() + " from " + start + " for " + what);
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
	public void accessTokens() {
		String head = String.join(Const.delimiter, Const.Version.V1, "Info", "Appid");
		account().page(head, head, null, 1000, (k,appid) ->{
			String secKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Secret", appid);
			String secret = account().get(secKey);
			logger.info("[accessTokens] " + appid);
			String key = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", appid);
			String expireKey = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Expire", appid);
			try {
				CallableGet get = new CallableGet("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appid, secret);
				String json = get.call();
				AccessToken at = gson.fromJson(json, AccessToken.class);
				String access = at.getAccess_token();
				long expireAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(at.getExpires_in());
				int result = token().put(key, access);
				token().put(expireKey, "" + expireAt);
				logger.info("[accessTokens] refresh " + result + ", expires at " + at.getExpires_in());
			} catch(Exception e) {
				logger.info("[accessTokens] Exception: " + e.getMessage());
			}
		});
	}
	public String hitokoto() {
		try {
			CallableGet hitokotoget = new CallableGet("https://v1.hitokoto.cn", "");
			String hitokotojson = hitokotoget.call();
			@SuppressWarnings("unchecked")
			Map<String, Object> hitokotomap = gson.fromJson(hitokotojson, Map.class);
			String hitokoto = (String) hitokotomap.get("hitokoto");
			account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Hitokoto"), hitokoto);
			data().put(String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Hitokoto", ""+hitokotomap.get("id")), hitokoto);
			return hitokoto;
		} catch (Exception e) {
			logger.error("hitokoto", e);
			return "";
		}
	}
	
	private static final String AUTO_COIN_CMD = "素朴网联back素朴网联home素朴网联open,com.suppresswarnings.android/com.suppresswarnings.android.MainActivity素朴网联sleep素朴网联left素朴网联right素朴网联swipe10素朴网联swipe1素朴网联scroll素朴网联scoll素朴网联click";
	public String getAutocoinCommand(String openid) {
		logger.info("getAutocoinCommand for: " + openid);
		StringBuffer sb = new StringBuffer();
		String myCmdKey = String.join(Const.delimiter, Const.Version.V1, openid, "Autocoin", "CMD");
		account().page(myCmdKey, myCmdKey, null, 30, (k,v) ->{
			if(!isNull(v)) {
				sb.append(v);
			}
		});
		if(sb.length() < 1) {
			String defaultCmd = account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Autocoin", "DefaultCMD"));
			if(isNull(defaultCmd)) {
				defaultCmd = AUTO_COIN_CMD;
				account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Autocoin", "DefaultCMD"), AUTO_COIN_CMD);
			}
			
			sb.append(defaultCmd);
		}
		return sb.toString();
	}
	public boolean isNull(String value) {
		return value == null || "null".equals(value) || "None".equals(value);
	}
	
	public boolean imgSecCheck(String upload) {
		boolean result = true;
		logger.info("[img_sec_check] " + upload);
		
        try {
        	CloseableHttpClient httpclient = HttpClientHolder.getInstance().newHttpClient();
    		HttpPost request = new HttpPost("https://api.weixin.qq.com/wxa/img_sec_check?access_token=" + accessToken("img_sec_check"));
            request.addHeader("Content-Type", "application/octet-stream");
            String path = System.getProperty("path.html") + upload;
            logger.info("[img_sec_check] check path: " + path);
	        InputStream inputStream = new FileInputStream(new File(path));
	        byte[] byt = new byte[inputStream.available()];
	        inputStream.read(byt);
	        request.setEntity(new ByteArrayEntity(byt, ContentType.create("image/jpg")));
	        HttpResponse response = httpclient.execute(request);
	        inputStream.close();
	        HttpEntity httpEntity = response.getEntity();
	        String json = EntityUtils.toString(httpEntity, "UTF-8");
	        logger.info("[img_sec_check] check result: " + json);
	        WXresult jso = gson.fromJson(json, WXresult.class);
	        if (jso.getErrcode() == 0) {
	        	result = true;
	        } else if (jso.getErrcode() == 87014) {
	        	result = false;
	        	tellAdmins(STUPID, "图片审核违法：" + json);
	        } else {
	        	tellAdmins(STUPID, "图片审核失败：" + json);
	        }
	    } catch (Exception e) {
	        logger.error("[img_sec_check] fail to imgSecCheck", e);
	    }
		return result;
	}
	
	public String updateCoin(String openid, int coin) {
		logger.info("update coins: " + openid + ":" + coin);
		AtomicBoolean lock = this.switches("mycoin" + openid);
		if(lock.get() && coin < 0) {
			logger.info("mycoin " + openid + " is locked");
			return "0";
		}
		synchronized(lock) {
			//TODO bugly after crash, mem is gone
			String time = "" + System.currentTimeMillis();
			String rand = "" + random.nextInt(99999);
			String myCoinKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "MyCoin", "Coin");
			String myCoin = this.account().get(myCoinKey);
			if(this.isNull(myCoin)) {
				//default 8 coins
				myCoin = "8";
				logger.info("set default coin:" + myCoin);
				tellAdmins(openid, "default coin:" + myCoin);
			}
			if(coin == 0) {
				return myCoin;
			}
			
			int coins = Integer.parseInt(myCoin);
			int sum = coins + coin;
			if(sum <= 0) {
				lock.compareAndSet(false, true);
				logger.error("user coins locked: " + openid);
			}
			this.account().put(myCoinKey, "" + sum);
			this.account().put(String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "MyCoin", "Stream", time, rand), "" + coin);
			return "" + sum;
		}
	}
	
	public String getOpenidByIdentity(String identity) {
		String code = account().get(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Code", identity));
		if(isNull(code)) return null;
		String openid = account().get(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Openid", code));
		if(isNull(openid)) return null;
		return openid;
	}
	public int fillWork() {
		AtomicInteger integer = new AtomicInteger(0);
		assimilatedQuiz.forEach(quiz ->{
			if(quiz.getReply().size() < bear.get()) {
				integer.incrementAndGet();
				workHandler.batchJob(quiz.getQuiz().value(), quiz.getQuiz().key(), Type.Reply);
			}
			if(quiz.getSimilar().size() < bear.get() * 5) {
				integer.incrementAndGet();
				workHandler.batchJob(quiz.getQuiz().value(), quiz.getQuiz().key(), Type.Similar);
			}
		});
		return integer.get();
	}
	
	public String myCounter(String openid) {
		Counter counter = counters.get(openid);
		if(counter == null) return "暂未统计";
		return counter.report();
	}
	
	public void collectV2(String value) {
		String key = String.join(Const.delimiter, Const.Version.V2, "Corpus", "Collect", value);
		data().put(key, value);
	}
	
	public void fillCounter(Quiz quiz){
		List<KeyValue> kvs = quizAnswerReplyOrSimilar.matches(quiz.getQuiz().key());
		String openid = kvs.get(1).value();
		
		Counter counter = counters.get(openid);
		if(counter == null) {
			counter = new Counter(openid);
			counters.put(openid, counter);
		}
		String time = kvs.get(2).value();
		counter.quiz(Long.parseLong(time), quiz.getQuiz().value());
		List<KeyValue> replys = quiz.getReply();
		for(KeyValue reply : replys) {
			List<KeyValue> replyKV = quizAnswerReplyOrSimilar.matches(reply.key());
			String openidReply = replyKV.get(4).value();
			Counter counterReply = counters.get(openidReply);
			if(counterReply == null) {
				counterReply = new Counter(openidReply);
				counters.put(openidReply, counterReply);
			}
			String timeReply = replyKV.get(5).value();
			counterReply.reply(Long.parseLong(timeReply), reply.value());
		}
		
		List<KeyValue> similars = quiz.getSimilar();
		for(KeyValue similar : similars) {
			List<KeyValue> similarKV = quizAnswerReplyOrSimilar.matches(similar.key());
			String openidSimilar = similarKV.get(4).value();
			Counter counterSimilar = counters.get(openidSimilar);
			if(counterSimilar == null) {
				counterSimilar = new Counter(openidSimilar);
				counters.put(openidSimilar, counterSimilar);
			}
			String timeSimilar = similarKV.get(5).value();
			counterSimilar.similar(Long.parseLong(timeSimilar), similar.value());
		}
	}
	
	
	public synchronized void fillQuestionsAndAnswers(String quizId){
		String start = String.join(Const.delimiter, Const.Version.V1, "Collect", "Corpus", "Quiz", quizId, "Answer");
		counters.clear();
		corpusCount.set(0);
		assimilatedQuiz.clear();
		workHandler.close();
		workHandler.working();
		logger.info("[fillQuestionsAndAnswers] start: " + start);
		List<Quiz> allQuiz = new ArrayList<>();
		data().page(start, start, null, Integer.MAX_VALUE, (t, u) -> {
			String left = t.substring(start.length());
			if(!left.contains("Similar") && !left.contains("Reply")) {
				corpusCount.incrementAndGet();
				Quiz quiz = new Quiz(t, u);
				allQuiz.add(quiz);
			}
		});
		//fill the reply and similar into each quiz
		allQuiz.forEach(quiz -> {
			String quizKey = quiz.getQuiz().key();
			String replyKey = String.join(Const.delimiter, quizKey, "Reply");
			data().page(replyKey, replyKey, null, Integer.MAX_VALUE, (t, u) -> {
				String left = t.substring(replyKey.length());
				if(!left.contains("Similar") && !left.contains("Reply")) {
					corpusCount.incrementAndGet();
					quiz.reply(t, u);
				}
			});
			
			String similarKey = String.join(Const.delimiter, quizKey, "Similar");
			data().page(similarKey, similarKey, null, Integer.MAX_VALUE, (t, u) -> {
				String left = t.substring(similarKey.length());
				if(!left.contains("Similar") && !left.contains("Reply")) {
					corpusCount.incrementAndGet();
					quiz.similar(t, u);
				}
			});
		});
		
		//assimilate quiz
		allQuiz.forEach(quiz -> {
			
			//TODO each quiz, count them
			fillCounter(quiz);
			boolean assimilated = false;
			for(int i=0;i<assimilatedQuiz.size();i++) {
				Quiz host = assimilatedQuiz.get(i);
				if(host == null) continue;
				if(host.assimilate(quiz)) {
					assimilated = true;
					break;
				}
			}
			if(!assimilated) {
				assimilatedQuiz.add(quiz);
			}
		});
		logger.info("[fillQuestionsAndAnswers] done assimilate: " + assimilatedQuiz.size());
		Collections.shuffle(assimilatedQuiz);
		logger.info("[fillQuestionsAndAnswers] shuffle assimilate");
		assimilatedQuiz.forEach(quiz -> {
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
	
	public void _action(CommandInterpreter ci) {
		try {
			String arg = ci.nextArgument();
			Class<?> clazz = this.getClass();
			Method method = clazz.getDeclaredMethod(arg);
			method.invoke(this);
			ci.println("method invoke done: " + arg);
		} catch (Exception e) {
			ci.println("fail to action");
		}
		ci.println("action execute done");
	}
	
	public void _interception(CommandInterpreter ci) {
		String quizId = ci.nextArgument();
		ci.println("[_interception] quizId: " + quizId);
		fillQuestionsAndAnswers(quizId);
		ci.println("[_interception] done");
	}
	
	/**
	 * publish all Chinese data to mqtt
	 * @param ci
	 */
	public void _publish(CommandInterpreter ci) {
		ci.println("[publish] start");
		String start = ci.nextArgument();
		if(leveldb == null) {
			ci.println("[publish] leveldb == null");
			return;
		}
		AtomicInteger count = new AtomicInteger(0);
		leveldb.page(start, start, null, 1000000, (k,v)->{
			if(CheckUtil.hasChinese(v)) {
				publish("corpus/publish", v);
				ci.println("[_findn] found val." +count.get() + " = " + v);
				count.incrementAndGet();
			}
		});
	}
	
	public void _prepare(CommandInterpreter ci) {
		ci.println("[prepare] start to write data");
		AtomicInteger integer = new AtomicInteger(0);
		this.assimilatedQuiz.forEach(quiz ->{
			String q = quiz.getQuiz().value();
			leveldb.put("003." + q, q);
			integer.incrementAndGet();
			List<KeyValue> reply = quiz.getReply();
			List<KeyValue> similar = quiz.getSimilar();
			AtomicInteger re = new AtomicInteger(0);
			for(KeyValue kv : reply) {
				String k = kv.value();
				if(CheckUtil.hasChinese(k) && k.length() < 30) {
					leveldb.put("003." + k, k);
					integer.incrementAndGet();
					leveldb.put("003." + q + ".Reply." + re.incrementAndGet(), k);
				}
			}
			AtomicInteger si = new AtomicInteger(0);
			for(KeyValue kv : similar) {
				String k = kv.value();
				if(CheckUtil.hasChinese(k) && k.length() < 30) {
					leveldb.put("003." + k, k);
					integer.incrementAndGet();
					leveldb.put("003." + q + ".Similar." + si.incrementAndGet(), k);
				}
			}
			ci.println("[prepare] prepare: " + integer.get());
		});
		ci.println("[prepare] done");
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
		if("ping".equals(action)) {
			return PingHandlerFactory.handle(parameter, this);
		} else if("qrcode".equals(action)) {
			return QRCodeHandlerFactory.handle(parameter, this);
		} else if("shorturl".equals(action))  {
			return ShortUrlHandlerFactory.handle(parameter, this);
		} else if("miniprogram".equals(action)) {  
			return MiniprogramHandlerFactory.handle(parameter, this);
		} else if("management".equals(action)) {  
			return ManagementHandlerFactory.handle(parameter, this);
		} else if("index".equals(action)) {  
			String id = parameter.getParameter("id");
			String msg = parameter.getParameter("msg");
			String openid = parameter.getParameter("openid");
			atUser(STUPID, "#" + id + " " + msg + ";来自" + openid);
			logger.info("atUser " + STUPID + " from index");
			return SUCCESS;
		} else if("addget".equals(action)) {
			String todo = parameter.getParameter("todo");
			logger.info("addget: " + todo);
			String key = parameter.getParameter("key");
			String value = parameter.getParameter("value");
			if("add".equals(todo)) {
				token().put(key, value);
				return value;
			} else if("get".equals(todo)) {
				value = token().get(key);
				return value;
			} else {
				return SUCCESS;
			}
		} else if("recall".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				logger.info("[recall] random == null");
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				logger.info("[recall] ticket == null");
				return "fail";
			}
			String state = parameter.getParameter("state");
			if(state == null) {
				logger.info("[recall] state == null");
				return "fail";
			}
			JsAccessToken accessToken = jsAccessToken(CODE);
			if(accessToken == null) {
				logger.info("[recall access_token] accessToken == null");
				return "fail";
			}
			
			String openId = accessToken.getOpenid();
			logger.info("[recall] get openid ok: " + openId);
			if(openId == null) {
				logger.info("[recall] get openid failed");
				return "fail";
			}
			account().put(String.join(Const.delimiter, "Robot", "Recall", state, openId), openId);
			account().put(String.join(Const.delimiter, "Robot", "Recall", openId, state), state);
			data().put(String.join(Const.delimiter, "Robot", "Recall", state, openId, "" + System.currentTimeMillis()), openId);
			publish("corpus/python/recall/" + state, openId);
			return article(openId);
		} else if("vip".equals(action)) {
			logger.info("[vip] lijiaming");
			String random = parameter.getParameter("random");
			if(random == null) {
				logger.info("[vip] random == null");
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				logger.info("[vip] ticket == null");
				return "fail";
			}
			String state = parameter.getParameter("state");
			if(state == null) {
				logger.info("[vip] state == null");
				return "fail";
			}
			JsAccessToken accessToken = jsAccessToken(CODE);
			if(accessToken == null) {
				logger.info("[vip access_token] accessToken == null");
				return "fail";
			}
			
			String openId = accessToken.getOpenid();
			if(openId == null) {
				logger.info("[vip] get openid failed");
				return "fail";
			}
			
			Map<String, Object> map = new HashMap<>();

			String P_Func_Target = "P_VIP_" + state;
			String qrKey = String.join(Const.delimiter, Const.Version.V1, "QRCode", P_Func_Target);
			String exist = account().get(qrKey);
			logger.info("[vip] qrkey: " + qrKey + " == " + exist);
			QRCodeTicket qrTicket = gson.fromJson(exist, QRCodeTicket.class);
			map.put("ticket", qrTicket.getTicket());
			WXuser myself = getWXuserByOpenId(state);
			map.put("uname", myself.getNickname());
			map.put("face", myself.getHeadimgurl());
			if(state.equals(openId)) {
				//I am VIP
				logger.info("[vip] I am VIP");
				
				List<KeyValue> invited = new ArrayList<>();
				List<String> openids = new ArrayList<>();
				String start = String.join(Const.delimiter, Const.Version.V1, openId, "Crew");
				account().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
					String userid = k.substring(start.length() + Const.delimiter.length());
					openids.add(userid);
				});
				openids.forEach(o ->{
					WXuser u = getWXuserByOpenId(o);
					KeyValue kv = new KeyValue(u.getHeadimgurl(), u.getNickname());
					invited.add(kv);
				});
				map.put("count", "" + openids.size());
				map.put("entries", invited);
				return gson.toJson(map);
			} else {
				//I am invited
				logger.info("[vip] I am invited");
				String start = String.join(Const.delimiter, Const.Version.V1, state, "Crew");
				AtomicInteger val = new AtomicInteger(1);
				account().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
					val.incrementAndGet();
				});
				account().put(String.join(Const.delimiter, Const.Version.V1, state, "Visitor"), openId);
				map.put("count", "" + val.get());
				
				return gson.toJson(map);
			}
		} else if("managereports".equals(action)){
			logger.info("[managereports] lijiaming");
			String random = parameter.getParameter("random");
			if(random == null) {
				logger.info("[managereports] random == null");
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				logger.info("[managereports] ticket == null");
				return "fail";
			}
			String state = parameter.getParameter("state");
			if(state == null) {
				logger.info("[managereports] state == null");
				return "fail";
			}
			JsAccessToken accessToken = jsAccessToken(CODE);
			if(accessToken == null) {
				logger.info("[managereport access_token] accessToken == null");
				return "fail";
			}
			
			String openId = accessToken.getOpenid();
			if(openId == null) {
				logger.info("[managereports] get openid failed");
				return "fail";
			}
			logger.info("[managereports] orders");
			if(!authrized(openId, "ManageReports")) {
				logger.info("[managereports] check auth failed");
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			long expire = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
			token().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Admin", "Token", CODE), "" + expire);
			token().put(code2OpenIdKey, openId);
			String start = String.join(Const.delimiter, Const.Version.V1, "Info", "Update", "Head");
			list.clear();
			logger.info("[managereports] list.clear()");
			account().page(start, start, null, 100, (x,head) ->{
				logger.info("[managereports] head = " + head);
				Map<String, Object> e = new HashMap<String, Object>();
				List<KeyValue> li = new ArrayList<KeyValue>();
				account().page(head, head, null, 999999, (k,v)-> {
					KeyValue kv = new KeyValue(k,v);
					li.add(kv);
				});
				e.put("list", li);
				list.add(e);
			});
			return gson.toJson(list);
		} else if("WX".equals(action)) {
			logger.info("[WX] request: " + parameter.toString());
			String msgSignature = parameter.getParameter("signature");
			String timestamp = parameter.getParameter("timestamp");
			String nonce = parameter.getParameter("nonce");
			String sha1 = getSHA1(Const.WXmsg.secret[0], timestamp, nonce, "", "");
			String openid =  parameter.getParameter("openid");
			String echoStr = parameter.getParameter("echostr");
			String token = parameter.getParameter("token");
			
			if(msgSignature == null || !msgSignature.equals(sha1)) {
				logger.error("[WX] wrong signature");
				return SUCCESS;
			}
			if(echoStr != null) {
				return echoStr;
			}
			if(openid != null) {
				String sms = parameter.getParameter(Parameter.POST_BODY);
				if(sms == null) {
					logger.error("[WX] no post body");
					return SUCCESS;
				}
				Map<String, String> wxmsg = WXPayUtil.xmlToMap(sms);
				String fromOpenId = wxmsg.get("ToUserName");
				if(token == null) {
					logger.error("token is null, config wrong");
					if(fromOpenId != null) {
						return xml(openid, "这个公众号还没有联系「素朴网联」管理员配置WXID，现在不能提供智能回复服务。", fromOpenId);
					}
					return SUCCESS;
				}
				String msgType = wxmsg.get("MsgType");
				logger.info("[WX] check: " + msgType);
				if(msgType == null) {
					SendMail cn = new SendMail();
					cn.title("notes [WX] msg structure not match", wxmsg.toString());
					return xml(openid, Const.WXmsg.reply[1], fromOpenId);
				}
				String input = "";
				if("event".equals(msgType)) {
					String where = "素朴网联";
					String event = wxmsg.get("Event");
					String eventKey = wxmsg.get("EventKey");
//					String ticket = wxmsg.get("Ticket");
					if(eventKey != null) {
						if(eventKey.startsWith("qrscene_")){
							where = eventKey.substring("qrscene_".length());
						} else {
							where = eventKey;
						}
					}
					
					logger.info("[lijiaming] where: " + where);
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
						
						String article = article(openid);
						WXnews news = new WXnews();
						news.setTitle("请阅读文章了解「素朴网联」全方位介绍");
						news.setDescription("请关注「素朴网联」阅读文章，全方位了解！");
						news.setUrl(article);
						news.setPicUrl("https://suppresswarnings.com/like.png");
						String json = "news://" + gson.toJson(news);
						String hitokoto = account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Hitokoto"));
						if(subscribe == null) {
							account().put(subscribeKey, time);
							atUser(openid, hitokoto);
							return xml(openid, json, fromOpenId);
						} else {
							String subscribeHistoryKey = String.join(Const.delimiter, Const.Version.V1, openid, "Subscribe", subscribe);
							account().put(subscribeHistoryKey, time);
							account().put(subscribeKey, time);
							if(subscribe.contains("unsubscribe")) {
								atUser(openid, hitokoto);
								return xml(openid, json, fromOpenId);
							} else {
								atUser(openid, hitokoto);
								return xml(openid, json, fromOpenId);
							}
						}
					} else if("unsubscribe".equals(event)) {
						String json = account().get(String.join(Const.delimiter, Const.Version.V1, openid, "User"));
						account().put(String.join(Const.delimiter, Const.Version.V1, openid, "User", "" + System.currentTimeMillis()), json);
						
						String subscribeKey = String.join(Const.delimiter, Const.Version.V1, "Subscribe", openid);
						String subscribe = account().get(subscribeKey);
						String time = "" + System.currentTimeMillis();
						String subscribeHistoryKey = String.join(Const.delimiter, Const.Version.V1, openid, "Subscribe", subscribe);
						account().put(subscribeHistoryKey, time);
						account().put(subscribeKey, time + Const.delimiter + "unsubscribe");
						unSubscribe(openid, subscribe);
						return SUCCESS;
					} else if("SCAN".equals(event)) {
						//TODO different scene
						String exchange = globalCommand(where);
						logger.info("SCAN: " + exchange + " == " + where);
						if(exchange != null) {
							//lijiaming leave from worker user
							forgetIt(openid);
							ContextFactory<CorpusService> cf = factories.get(exchange);
							if(cf == null) {
								cf = factories.get(exchange.toLowerCase());
							}
							Context<CorpusService> contxt = cf.getInstance(fromOpenId, openid, this);
							contxt.test("SCAN_" + where);
							contextx(openid, contxt, cf.ttl());
							return xml(openid, contxt.output(), fromOpenId);
						}
						
						return xml(openid, where, fromOpenId);
					} else if("LOCATION".equals(event)) {
						logger.info("[corpus] location: " + wxmsg.get("FromUserName") + " = (" + wxmsg.get("Latitude") + ", " + wxmsg.get("Longitude") + ") * " + wxmsg.get("Precision"));
						String mediaKey = String.join(Const.delimiter, Const.Version.V1, "Keep", "Location", ""+System.currentTimeMillis(), openid);
						String position = "LOC_" +wxmsg.get("Latitude") + ";" + wxmsg.get("Longitude") + ";" + wxmsg.get("Label");
						account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Send", "Location"), position);
						data().put(mediaKey, sms);
						return SUCCESS;
					} else if("user_view_card".equals(event)) {
						account().put(String.join(Const.delimiter, Const.Version.V1, "Keep", "ViewCard", openid, ""+System.currentTimeMillis()), wxmsg.get("UserCardCode"));
						return SUCCESS;
					} else if("user_enter_session_from_card".equals(event)) {
						account().put(String.join(Const.delimiter, Const.Version.V1, "Keep", "FromCard", openid, ""+System.currentTimeMillis()), wxmsg.get("UserCardCode"));
						return SUCCESS;
					} else if("user_del_card".equals(event)) {
						account().put(String.join(Const.delimiter, Const.Version.V1, "Keep", "DeleteCard", openid, ""+System.currentTimeMillis()), wxmsg.get("UserCardCode"));
						return SUCCESS;
					} else if("ShakearoundUserShake".equals(event)) {
						/**
						 * ChosenBeacon:Major = 10197
						 * ChosenBeacon:Minor = 49838
						 * CreateTime = 1562864790
						 * ChosenBeacon:Uuid = FDA50693-A4E2-4FB1-AFCF-C6EB07647825
						 * ChosenBeacon:Distance = 1.5933141040212173
						 * ToUserName = gh_b3ac48e83f7d
						 * MsgType = event
						 * ChosenBeacon = FDA50693-A4E2-4FB1-AFCF-C6EB0764782510197498381.5933141040212173-67
						 * ChosenBeacon:Rssi = -67
						 * AroundBeacons = 
						 * ChosenPageId = 6750529
						 * Event = ShakearoundUserShake
						 * FromUserName = oDqlM1T1RcLQuv48d0iVr0r1VpLQ
						 */
						String major = wxmsg.get("ChosenBeacon:Major");
						String minor = wxmsg.get("ChosenBeacon:Minor");
						String uuid = wxmsg.get("ChosenBeacon:Uuid");
						String pageid = wxmsg.get("ChosenPageId");
						String time = "" + System.currentTimeMillis();
						if(debug()) {
							atUser(STUPID, String.format("摇一摇：%s%s,pageid:%s,uuid:%s\nopenid:%s", major, minor, pageid, uuid, openid));
						}
						String ibeacon = major + minor;
						String key = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Bind", ibeacon);
						String bind = account().get(key);
						account().put(String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Shake", ibeacon, pageid, uuid, openid, time), openid);
						account().put(String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Shake", ibeacon, pageid, uuid, time), ibeacon);
						WXuser user = getWXuserByOpenId(openid);
						String name = "匿名";
						if(user != null) {
							name = user.getNickname();
						}
						if(isNull(bind)) {
							atUser(STUPID, name+"发现了（未绑定）的摇钱树:" + ibeacon);
						} else {
							account().put(String.join(Const.delimiter, Const.Version.V1, bind, "iBeacon", "Customer", ibeacon, pageid, uuid, openid), openid);
							data().put(String.join(Const.delimiter, Const.Version.V1, bind, "iBeacon", "Customer", ibeacon, pageid, uuid, openid, time), openid);
							atUser(bind, name+"发现了你的摇钱树");
						}
					} else if("user_get_card".equals(event)) {
						String cardid = wxmsg.get("CardId");
						String give   = wxmsg.get("IsGiveByFriend");
						String friend = wxmsg.get("FriendUserName");
						String key = String.join(Const.delimiter, Const.Version.V1, "Info", "Card", "Bind", cardid);
						String bind = account().get(key);
						String time = "" + System.currentTimeMillis();
						account().put(String.join(Const.delimiter, Const.Version.V1, "Card", "Get", cardid, openid, time), openid);
						account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Card", "Get", cardid, time), cardid);
						if(debug()) {
							atUser(STUPID, String.format("卡券:%s,赠送:%s,来自:%s,绑定:%s\nopenid:%s", cardid, give, friend, bind, openid));
						}
						String from = "";
						if("1".equals(give)) {
							WXuser given = getWXuserByOpenId(friend);
							if(given != null) {
								from = "("+given.getNickname() + "赠送的)";
							}
						}
						WXuser user = getWXuserByOpenId(openid);
						String name = "匿名";
						if(user != null) {
							name = user.getNickname();
						}
						if(isNull(bind)) {
							atUser(STUPID, name + "领取了(未绑定)的卡券" + from + cardid);
						} else {
							account().put(String.join(Const.delimiter, Const.Version.V1, bind, "Card", "Customer", cardid, openid), openid);
							data().put(String.join(Const.delimiter, Const.Version.V1, bind, "Card", "Customer", cardid, openid, time), openid);
							atUser(bind, name + "领取了你的卡券" + from);
						}
					} else {
						atUser(STUPID, "未处理的事件：" + wxmsg);
						logger.info("unhandled event: " + event + ", " + eventKey);
						return SUCCESS;
					}
				} else if("location".equals(msgType)) {
					String position = "LOC_" + wxmsg.get("Location_X") +";"+ wxmsg.get("Location_Y") + ";" + wxmsg.get("Label");
					Context<?> context = context(openid);
					logger.info("[corpus] LOCATION context: " + context + ", openid: " + openid);
					account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Send", "Location"), position);
					if(context != null) {
						context.test(position);
						logger.info("[corpus] LOCATION return: " + context + ", openid: " + openid);
						return xml(openid, context.output(), fromOpenId);
					} else {
						return SUCCESS;
					}
				} 
				
				String msgid = wxmsg.get("MsgId");
				if(sameMsgid(msgid)) {
					logger.warn("duplicated msgid: " + msgid + ", openid: " + openid);
					return SUCCESS;
				}
				if("text".equals(msgType)) {
					input = wxmsg.get("Content");
					logger.info("[corpus] text: " + input);
				} else if("voice".equals(msgType)) {
					input = wxmsg.get("Recognition");
					if(input == null) {
						input = "听不清说了啥？";
					}
				} else if("image".equals(msgType)) {
					String mediaKey = String.join(Const.delimiter, Const.Version.V1, "Keep", "Media", ""+System.currentTimeMillis(), openid);
					data().put(mediaKey, sms);
					String url = wxmsg.get("PicUrl");
					//TODO lijiaming save image to server
					String downloadFolder = "download/";
					String saveTo = System.getProperty("path.html") + downloadFolder;
					CallableDownload download = new CallableDownload(url, CallableDownload.MB10, saveTo, ".jpg", true, TimeUnit.HOURS.toMillis(48));
					scheduler.submit(download);
					String futureUrl = "https://suppresswarnings.com/" + downloadFolder + download.getFileName();
					input = "IMAGE_" + futureUrl;
				} else {
					String mediaKey = String.join(Const.delimiter, Const.Version.V1, "Keep", "Other", ""+System.currentTimeMillis(), openid);
					data().put(mediaKey, sms);
					return SUCCESS;
				}

				Context<?> context = context(openid);
				logger.info("[corpus] context: " + context + ", openid: " + openid);
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
				String out = context.output();
				
				return xml(openid, out, fromOpenId);
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
			String openid = parameter.getParameter("openid");
			if(openid != null) {
				logger.info("report to openid: " + openid);
				WXuser user = this.getWXuserByOpenId(openid);
				if(user!= null) {
					String tokenKey  = String.join(Const.delimiter, Const.Version.V2, openid, "Token");
					String token = this.account().get(tokenKey);
					logger.info("check token: " + token);
					if(!this.isNull(token) && reportToken.equals(token)) {
						tellAdmins(openid, reportMsg);
					}
				}
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
			//lijiaming: it should be serious about openid
			//TODO 
			return DaigouHandlerFactory.handle(parameter, this);
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
				return exist;
			}
			//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
			JsAccessToken accessToken = jsAccessToken(CODE);
			if(accessToken == null) {
				logger.info("[daigou] accessToken == null");
				return "fail";
			}
			
			if(accessToken.getOpenid() == null) {
				logger.info("[daigou] accessToken.getOpenid == null");
				return "fail";
			}
			
			token().put(code2OpenIdKey, accessToken.getOpenid());
			return accessToken.getOpenid();
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
			JsAccessToken accessToken = jsAccessToken(CODE);
			if(accessToken == null) {
				logger.info("[corpus collect access_token] accessToken == null");
				return "fail";
			}
			String openId = accessToken.getOpenid();
			if(openId == null) {
				logger.info("[accessToken.getOpenid] is null");
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
					Map<String, Object> e = new HashMap<>();
					e.put("replyid", t);
					e.put("reply", u);
					replyinfo.add(e);
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
		} else if("similarreplies".equals(action)) {
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
			data().page(similarKey, similarKey, null, Integer.MAX_VALUE, (t, u) -> {
				String z = t.substring(similarKey.length());
				if(!z.contains("Reply")) {
					result.add(u);
				}
			});
			logger.info("[corpus reply similar] " + result.size() + " openid:" + exist);
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
			JsAccessToken accessToken = jsAccessToken(CODE);
			if(accessToken == null) {
				logger.info("[corpus user access_token] accessToken == null");
				return "fail";
			}
			
			if(accessToken.getOpenid() == null) {
				logger.info("[corpus user access_token] " + accessToken.toString());
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
		} else if("validate".equals(action)) {
			String identity = parameter.getParameter("identity");
			String code = parameter.getParameter("token");
			String expireKey = String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Expire", identity);
			
			if(identity == null||"".equals(identity)) {
				return "fail";
			} else {
				String existKey = String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Identity", identity);
				String exist = account().get(existKey);
				
				if("Paid".equals(exist)) {
					String openid = getOpenidByIdentity(identity);
					String expireAt = account().get(expireKey);
					if(expireAt == null || "None".equals(expireAt)) {
						long expire = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24);
						account().put(expireKey, "" + expire);
						return "Paid~" + getAutocoinCommand(openid);
					} else {
						try {
							long expire = Long.parseLong(expireAt);
							if(expire > System.currentTimeMillis()) {
								return "Paid~" + getAutocoinCommand(openid);
							} else {
								if(code == null || "".equals(code)) {
									logger.info("[validate] 验证:" + code + " expireAt: " + expireAt + " now: " + System.currentTimeMillis());
									return "expired~激活码过期了，请重新获取(" + expireAt + ")";
								} else {
									logger.info("[validate] 首次激活:" + code);
								}
							}
						} catch (Exception e) {
							logger.error("激活码过期了", e);
						}
					}
				}
			}
			
			if(code == null || "".equals(code)) {
				return "fail~激活码不能为空";
			} else {
				String key = String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Software", code);
				String exist = account().get(key);
				if("Used".equals(exist) || "".equals(exist) || null == exist) {
					return "fail~激活码不存在或已被使用";
				} else {
					long now = System.currentTimeMillis();
					long expire = now + TimeUnit.HOURS.toMillis(24);
					account().put(key, "Used");
					//TODO identity -> code -> openid
					account().put(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Bind", code), identity);
					account().put(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Code", identity), code);
					account().put(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Identity", identity), "Paid");
					account().put(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Software", code, ""+ now), exist);
					account().put(expireKey, "" + expire);
					logger.info("[validate] 激活:" + code + " expireAt: " + expire + " now: " + System.currentTimeMillis());
					String openid = getOpenidByIdentity(identity);
					tellAdmins(openid, "用户激活:" + openid + " " + identity + " " + code);
					return "Paid~" + getAutocoinCommand(openid);
				}
			}
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
		} else if("minipay".equals(action)){
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String openid = parameter.getParameter("openid");
			String groupid = parameter.getParameter("groupid");
			String goodsid = parameter.getParameter("goods");
			String userid = parameter.getParameter("userid");
			String notice = parameter.getParameter("notice");
			String goodsPriceKey = String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Price");
			String amount = account().get(goodsPriceKey);
			if(isNull(amount)) {
				tellAdmins(openid, "商品未上架：" + goodsid);
				return "fail";
			}
			String name = parameter.getParameter("name");
			String typeKey = String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Type");
			String type = account().get(typeKey);
			if(isNull(type)) {
				type = "mini";
			}
			
			String openIdEnd = openid.substring(openid.length() - 7);
			String randEnd = random.substring(random.length() - 4);
			long current = System.currentTimeMillis(); 
			String orderid = type + current + openIdEnd + randEnd;
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Userid"), isNull(userid) ? "null" : userid);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Notice"), isNull(notice) ? "null" : notice);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Groupid"), isNull(groupid) ? "null" : groupid);
			try {
				String appid = System.getProperty("mp.appid");
				String argsAppid = parameter.getParameter("appid");
				WXPayConfig config = new MiniPayConfigImpl(isNull(argsAppid)?appid:argsAppid);
				return minipay(config, orderid, "素朴网联摇钱树支付", name, goodsid, "1", amount, ip, openid, current, type);
			} catch (Exception e) {
				return "fail";
			}
		} else if("prepay".equals(action)){
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String ticket = parameter.getParameter("ticket");
			if(ticket == null) {
				logger.error("[prepay] ticket null");
				return "fail";
			}
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", ticket);
			String openid = token().get(code2OpenIdKey);
			if(openid == null) {
				logger.error("[prepay] openid null");
				return "fail";
			}
			String state = parameter.getParameter("goodsid");
			String amount  = parameter.getParameter("amount");
			
			String detail = "[" + state + ", " + amount + "]";
			String goodsid = state;
			if(state.contains("_Template_")) {
				goodsid = state.split("_Template_")[0];
			}
			String reason =  account().get(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Reason"));
			String what =  account().get(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "What"));
			String body = "素朴网联-" + reason + "-" +amount + "-" + what;
			String goodsPriceKey = String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Price");
			String totalcent = account().get(goodsPriceKey);
			if(totalcent == null) {
				logger.error("[prepay] totalcent null");
				return "fail";
			}
			
			String goodsTypeKey = String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Type");
			String type = account().get(goodsTypeKey);
			if(type == null) {
				logger.error("[prepay] type null");
				return "fail";
			}
			if("Data".equals(type)) {
				double price = Double.parseDouble(amount) * Integer.parseInt(totalcent);
				totalcent = "" + (long) price;
			}
			
			String openIdEnd = openid.substring(openid.length() - 7);
			String randEnd = random.substring(random.length() - 4);
			long current = System.currentTimeMillis(); 
			String orderid = type + current + openIdEnd + randEnd;
			//TODO 临时加的赞助金额的goodid特殊表示
			if(state.contains("_Template_")) {
				goodsid = state.split("_Template_")[0];
				orderid = goodsid + current + openIdEnd + randEnd;
			}
			
			String projectid = parameter.getParameter("projectid");
			if(projectid != null && projectid.length() > 5) {
				account().put(String.join(Const.delimiter, Const.Version.V1, "Orderid", "Projectid", orderid), projectid);
			}
			
			String clientip = ip.split(",")[0];
			logger.info("[corpus prepay] openid:" + openid + ", goodsid:" + state + ", amount:" + amount);
			try {
				return prepay(orderid, body, detail, state, amount, totalcent, clientip, openid, current, type);
			} catch (Exception e) {
				return "fail";
			}
		}  else if("payment".equals(action)) {
			String random = parameter.getParameter("random");
			if(random == null) {
				return "fail";
			}
			String CODE = parameter.getParameter("ticket");
			if(CODE == null) {
				return "fail";
			}
			
			String state = parameter.getParameter("state");
			if(state == null) {
				return "fail";
			}
			
			String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
			String exist = token().get(code2OpenIdKey);
			if(exist != null) {
				return "fail";
			}
			JsAccessToken accessToken = jsAccessToken(CODE);
			if(accessToken == null) {
				logger.info("[payment] accessToken == null");
				return "fail";
			}
			
			
			if(accessToken.getOpenid() == null) {
				logger.info("[corpus access_token] " + accessToken.toString());
				return "fail";
			}
			
			token().put(code2OpenIdKey, accessToken.getOpenid());
			
			String goodsid = state;
			if(state.contains("_Template_")) {
				goodsid = state.split("_Template_")[0];
			}
			String reason =  account().get(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Reason"));
			String what =  account().get(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "What"));
			String goodsTypeKey = String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Type");
			String type = account().get(goodsTypeKey);
			String pricecent =  account().get(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", goodsid, "Price"));
			WXuser user = getWXuserByOpenId(accessToken.getOpenid());
			Map<String, String> goods = new HashMap<>();
			goods.put("reason", reason);
			goods.put("what", what);
			goods.put("type", type);
			goods.put("pricecent", pricecent);
			goods.put("userimg", user.getHeadimgurl());
			goods.put("username", user.getNickname());
			return gson.toJson(goods);
		} else if("notify".equals(action)) {
			NotifyHandlerFactory.handle(parameter, this);
		} else if("jsapi_ticket".equals(action)) {
			long current = System.currentTimeMillis()/1000;
			int random = new Random().nextInt(100000);
			String noncestr = current + "" +  random;
			String jsapiTicket = jsAccessToken();
			String timestamp = "" + current;
			String url = parameter.getParameter("url");
			Base64.Decoder decoder = Base64.getDecoder();
			String de = new String(decoder.decode(url.getBytes("UTF-8")), "UTF-8");
			String sha1 = getSHA1("jsapi_ticket="+jsapiTicket, "noncestr=" + noncestr, "timestamp="+timestamp, "url="+de, "&");
			Map<String, Object> map = new HashMap<>();
			map.put("nonceStr", noncestr);
			map.put("timestamp", current);
			map.put("signature", sha1);
			return gson.toJson(map);
		}
		logger.info(parameter.toString());
		logger.info("[Corpus] return success for any unknown action " + action + " from " + ip);
		return SUCCESS;
	}
	
	public String generateRandomToken(String openid, long expireAt, String reason) {
		String key = "";
		int x = new Random().nextInt(999999999);
		AtomicInteger integer = new AtomicInteger(1);
		String exist = "";
		String code = "";
		while(exist != null) {
			x = x + integer.getAndIncrement();
			code = Integer.toHexString(x).toUpperCase();
			key = String.join(Const.delimiter, Const.Version.V1, "Token", "For", reason, code);
			exist = token().get(key);
		}
		token().put(key, openid);
		expireAt(token(), expireAt, key);
		if(integer.get() > 100) {
			logger.warn("[corpus] lijiaming: too many tries, delete some expired keys");
			List<KeyValue> toDelete = expiredList(token());
			integer.set(0);
			toDelete.forEach(kv->{
				if(LevelDB.OK == token().del(kv.value())) {
					integer.getAndIncrement();
					token().del(kv.key());
				}
			});
			logger.warn("[corpus] lijiaming: need to be deleted = " + toDelete.size() + ", actually deleted = " + integer.get());
		}
		account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Token", "For", reason), code);
		token().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Token", "For", reason, openid), code);
		return code;
	}
	
	public void expireAt(LevelDB levedb, long expireAt, String key) {
		long index = STOP_THE_WORLD - expireAt;
		levedb.put(String.join(Const.delimiter, Const.Version.V1, "ExpireAt", "" + index), key);
	}
	
	public List<KeyValue> expiredList(LevelDB levedb) {
		String head = String.join(Const.delimiter, Const.Version.V1, "ExpireAt");
		long index = STOP_THE_WORLD - System.currentTimeMillis();
		String start = String.join(Const.delimiter, Const.Version.V1, "ExpireAt", "" + index);
		List<KeyValue> expiredList = new ArrayList<>();
		levedb.page(head, start, null, 10000, (k,v) ->{
			expiredList.add(new KeyValue(k, v));
		});
		return expiredList;
	}

	public String generateActivateCode(String openid) {
		String key = "";
		int x = new Random().nextInt(99999999);
		AtomicInteger integer = new AtomicInteger(1);
		String exist = "";
		String code = "";
		while(exist != null) {
			x = x + integer.getAndIncrement();
			code = Integer.toHexString(x).toUpperCase();
			key = String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Software", code);
			exist = account().get(key);
		}
		account().put(String.join(Const.delimiter, Const.Version.V1, "Code", "Activate", "Openid", code), openid);
		account().put(key, openid);
		String mykey = String.join(Const.delimiter, Const.Version.V1, openid, "Code", "Activate", "Software");
		account().put(mykey, code);
		account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Activate", "Software", openid), code);
		return code;
	}
	public String jsAccessToken() {
		String key = String.join(Const.delimiter, Const.Version.V1, "JsAccessToken", "Token", "973rozg");
		String ticket = token().get(key);
		return ticket;
	}
	public JsAccessToken jsAccessToken(String code) {
		String APPID = System.getProperty("wx.appid");
		String SECRET = System.getProperty("wx.secret");
		
		if(APPID == null || SECRET == null) {
			logger.error("[daigou] wrong request with null parameters: appid=" + APPID + ", code=" + code);
			return null;
		}

		CallableGet get = new CallableGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", APPID, SECRET, code);
		String json;
		try {
			json = get.call();
			JsAccessToken accessToken = gson.fromJson(json, JsAccessToken.class);
			return accessToken;
		} catch (Exception e) {
			logger.error("[jsAccessToken] fail to call get ", e);
		}
		
		return null;
	}
	public String end(String origin, int length) {
		if(origin.length() < length + 4) return origin;
		int size = origin.length();
		return origin.substring(size - length, size);
	}
	public String minipay(WXPayConfig config, String orderid, String body, String detail, String goodsid, String amount, String totalcent, String clientip, String openid, long current, String type) {
		long timeStamp = current / 1000;
		try {
			WXPay wxPay = new WXPay(config);
			logger.info("[corpus minipay] MiniPay ready");
			
			Map<String, String> reqData = new HashMap<>();
			reqData.put("timeStamp", ""+timeStamp);
			reqData.put("device_info", "WEB");
			reqData.put("body", body);
			reqData.put("detail", detail);
			reqData.put("attach", goodsid);
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
			logger.info("[corpus minipay] reqData: " + reqData.toString());
			logger.info("[corpus minipay] unifiedOrder result ready");
			Map<String, String> resultData = wxPay.unifiedOrder(reqData);
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
			logger.info("[corpus minipay] paySign ready");
			
			String unifiedOrder = gson.toJson(result);
			logger.info("[corpus minipay] unifiedOrder OK: " + unifiedOrder);
			String reqjson = gson.toJson(reqData);
			String resultjson = gson.toJson(resultData);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", "Orderid", orderid), orderid);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "UnifiedOrder"), unifiedOrder);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Reqjson"), reqjson);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Resultjson"), resultjson);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "State"), "Wait");
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Openid"), openid);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Goodsid"), goodsid);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Amount"), amount);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Pricecent"), totalcent);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Type"), type);
			
			account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Orderid", orderid), orderid);
			account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Orderid", orderid, "Body"), body);			
			return unifiedOrder;
		} catch (Exception e) {
			logger.error("获取minipay接口参数失败", e);
			return null;
		}
		
	}
	
	public String prepay(String orderid, String body, String detail, String goodsid, String amount, String totalcent, String clientip, String openid, long current, String type) {
		long timeStamp = current / 1000;
		try {
			WXPayConfig config = new WXPayConfigImpl();
			WXPay wxPay = new WXPay(config);
			logger.info("[corpus prepay] WXPay ready");
			
			Map<String, String> reqData = new HashMap<>();
			reqData.put("timeStamp", ""+timeStamp);
			reqData.put("device_info", "WEB");
			reqData.put("body", body);
			reqData.put("detail", detail);
			reqData.put("attach", goodsid);
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
			logger.info("[corpus prepay] unifiedOrder result ready");
			Map<String, String> resultData = wxPay.unifiedOrder(reqData);
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
			
			String unifiedOrder = gson.toJson(result);
			logger.info("[corpus prepay] unifiedOrder OK: " + unifiedOrder);
			String reqjson = gson.toJson(reqData);
			String resultjson = gson.toJson(resultData);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", "Orderid", orderid), orderid);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "UnifiedOrder"), unifiedOrder);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Reqjson"), reqjson);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Resultjson"), resultjson);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "State"), "Wait");
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Openid"), openid);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Goodsid"), goodsid);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Amount"), amount);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Pricecent"), totalcent);
			account().put(String.join(Const.delimiter, Const.Version.V1, "Order", orderid, "Type"), type);
			
			account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Orderid", orderid), orderid);
			account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Orderid", orderid, "Body"), body);
			
			if(orderid.startsWith("Auth")) {
				String paidKey = String.join(Const.delimiter, Const.Version.V1, "Paid", goodsid, openid);
				String paidState = account().get(paidKey);
				if(paidState == null) {
					account().put(paidKey, orderid);
					logger.info("[prepay] note orderid for auth pay: " + goodsid + ", " + openid + ", orderid" + orderid);
				}
			}
			
			return unifiedOrder;
		} catch (Exception e) {
			logger.error("获取预支付接口参数失败", e);
			return null;
		}
		
	}
	
	public String xml(String openid, String msg, String fromOpenId) {
		if(msg == null || msg.length() < 1) {
			logger.error("[Corpus] empty response");
			return SUCCESS;
		}
		long time = System.currentTimeMillis()/1000;
		if(msg.startsWith("news://")) {
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
		WXuser user = syncWXuser(openId);
		users.put(openId, user);
		user = getWXuserByOpenId(openId);
		logger.info("[user online] new online: " + user.toString());
		tellAdmins(openId, user.getNickname() + " 新关注");
	}
	
	public void unSubscribe(String openid, String subscribe) {
		forgetIt(openid);
		WXuser user = getWXuserByOpenId(openid);
		tellAdmins(openid, user.getNickname() + " 取消关注");
		tellBossNow(openid, "你邀请的朋友" + user.getNickname() + "现在取消关注了，快去挽回一下朋友吧");
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
	
	public void updateWXuser() {
		if(new Random().nextDouble() < 0.3) {
			logger.info("no need to update WXuser");
			return;
		}
		String head = String.join(Const.delimiter, Const.Version.V1, "User");
		List<String> openids = new ArrayList<>();
		account().page(head, head, null, 100000, (k, t) ->{
			String openid = k.substring(head.length() + Const.delimiter.length());
			openids.add(openid);
		});
		
		openids.forEach(openid -> {
			logger.info("check user " + openid);
			WXuser user = syncWXuser(openid);
			if(user != null) {
				users.put(openid, user);
			}
		});
		
		logger.info("update user info: " + users.size());
	}
	
	public WXuser syncWXuser(String openId) {
		logger.info("[corpus] get WXuser info: " + openId);
		String userKey = String.join(Const.delimiter, Const.Version.V1, openId, "User");
		String accessToken = accessToken("Sync User Info");
		CallableGet get = new CallableGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN", accessToken, openId);
		try {
			String json = get.call();
			WXuser user = gson.fromJson(json, WXuser.class);
			account().put(userKey, json);
			return user;
		} catch (Exception e) {
			logger.error("[corpus] fail to get user info: " + openId, e);
			return null;
		}
	}
	/**
	 * 001.openidvalue.User = {user info json}
	 * @param openId
	 * @return
	 */
	public WXuser getWXuserByOpenId(String openId) {
		if(openId == null) return null;
		WXuser user = users.get(openId);
		if(user != null) {
			logger.info("[corpus] getWXuserByOpenId: " + user.getNickname());
			return user;
		}
		
		String userKey = String.join(Const.delimiter, Const.Version.V1, openId, "User");
		String json = account().get(userKey);
		if(debug() || json == null || "None".equals(json)) {
			user = syncWXuser(openId);
		} else {
			logger.info("[corpus] getWXuserByOpenId using exist json: " + json);
			try {
				user = gson.fromJson(json, WXuser.class);
				account().put(userKey, json);
			} catch (Exception e) {
				account().put(userKey, "None");
				logger.error("[corpus] fail to parse user info: " + openId, e);
			}
			
		}
		
		if(user == null) {
			logger.info("[corpus] fail to get user info: use default");
			user = new WXuser();
			user.setSubscribe(0);
			user.setOpenid(openId);
			user.setNickname("未关注");
		}
		users.put(openId, user);
		return user;
	}
	
	public boolean debug(){
		return "on".equals(data().get(String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus", "ON")));
	}
	public String sendNewsTo(String business, String newsJson, String openid) {
		if(newsJson == null || openid == null || openid.length() < 2 || newsJson.length() < 2 ) {
			return null;
		}
		String json = "{\"touser\":\""+openid+"\",\"msgtype\":\"news\",\"news\":{\"articles\": ["+newsJson+"]}}";
		String accessToken = accessToken(business);
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + accessToken;
		CallablePost post = new CallablePost(url, json);
		try {
			String result = post.call();
			logger.info("[corpus] send news to: " + openid + ", newsJson: " + newsJson + ", result: " + result);
			return result;
		} catch (Exception e) {
			logger.error("[corpus] fail to send news to user: " + openid, e);
			return null;
		}
    }

	public String sendTxtTo(String business, String message, String openid) {
		if(message == null || openid == null || openid.length() < 2 || message.length() < 2 ) {
			return null;
		}
		String accessToken = accessToken(business);
		String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + accessToken;
		String json = "{\"touser\":\"" + openid + "\",\"msgtype\":\"text\",\"text\":{\"content\":\"" + message.replace("\\", "\\\\") + "\"}}";
		CallablePost post = new CallablePost(url, json);
		try {
			String result = post.call();
			logger.info("[corpus] send text to: " + openid + ", msg: " + message + ", result: " + result);
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
	public String accessToken(String business) {
		logger.info("[corpus] get access token for " + business);
		if("img_sec_check".equals(business)) {
			return token().get(String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", "wxc2e12f2151179727"));
		} else if(business.startsWith("wx")) {
			return token().get(String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", business));
		}
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
	
	String article = "https://w.url.cn/s/AbDIQuB";
	List<String> articles = new ArrayList<>();
	Random random = new Random();
	public void updateArticle() {
		String head = String.join(Const.delimiter, Const.Version.V2, "Article", "List", "Link");
		logger.info("0/1 articles = " + articles);
		articles.clear();
		account().page(head, head, null, 10000, (k, link) ->{
			articles.add(link);
		});
		logger.info("1/1 articles = " + articles);
	}
	public String article(String openid) {
		try {
			String read = account().get(String.join(Const.delimiter, Const.Version.V2, "Article", "Read", openid));
			String chose = null;
			if(isNull(read)) {
				String set = account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Setting", "Article"));
				if(!isNull(set)) {
					article = set;
				}
			}

			if(articles.size() < 2) {
				chose = article;
			} else {
				chose = articles.get(random.nextInt(articles.size()));
			}
			logger.info("[article] " + openid + " chose " + chose);
			account().put(String.join(Const.delimiter, Const.Version.V2, "Article", "Read", openid), chose);
			data().put(String.join(Const.delimiter, Const.Version.V2, "Article", "Read", openid, "" + System.currentTimeMillis()), chose);
			return chose;
		} catch (Exception e) {
			logger.error("fail to get article", e);
			return article;
		}
	}
	public String getSHA1(String token, String timestamp, String nonce, String encrypt, String join) {
		if(CheckUtil.anyNull(token, timestamp, nonce, encrypt)) {
			return null;
		}
		try {
			String[] array = new String[] { token, timestamp, nonce, encrypt };
			Arrays.sort(array);
			String str = String.join(join, array);
			logger.info("sha1 to be encrypt: " + str);
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
		TTL e = expire(openid, timeToLiveMillis);
		contexts.put(openid, context);
		context.setTTL(e);
	}
	public TTL expire(String name, long timeToLiveMillis) {
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
		return e;
	}
	
	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		System.out.println(c.get(Calendar.HOUR_OF_DAY));
		
		String string = "23Similar456Reply67890";
		System.out.println(string.split("Similar|Reply")[0]);
	}
	
	public String uniqueKey(String string) {
		synchronized (atomicIds) {
			String key = String.join(Const.delimiter, Const.Version.V1, "Unique", "Key", string);
			AtomicInteger id = atomicIds.get(string);
			if(id == null) {
				String value = account().get(key);
				if(value == null) {
					value = "0";
					account().put(key, value);
				}
				int v = Integer.valueOf(value);
				id = new AtomicInteger(v);
				atomicIds.put(string, id);
			}
			account().put(key, "" + id.incrementAndGet());
			return string + Const.delimiter + id.get();
		}
	}
	
	public String increment(String key, String start) {
		synchronized (incrementers) {
			AtomicInteger idx = incrementers.get(key);
			if(idx == null) {
				AtomicInteger val = new AtomicInteger(0);
				account().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
					val.incrementAndGet();
				});
				idx = val;
				incrementers.put(key, idx);
			}
			
			return ""+idx.incrementAndGet();
		}
	}
	
	public void decrement(String openid) {
		synchronized (incrementers) {
			AtomicInteger idx = incrementers.get(openid);
			if(idx == null) {
				logger.error("[decrement] counter not found: " + openid);
			} else {
				idx.decrementAndGet();
			}
			
		}
	}
	
	public List<Quiz> getQuiz(int n) {
		List<Quiz> all = new ArrayList<>();
		all.addAll(assimilatedQuiz);
		Collections.shuffle(all);
		if(all.size() <= n) return all;
		else {
			List<Quiz> little = new ArrayList<>();
			for(int i=0;i<n;i++) {
				little.add(all.get(i));
			}
			return little;
		}
	}
	
	public String getRealValue(String openid) {
		return account().get(String.join(Const.delimiter, Const.Version.V2, openid, "RealValue"));
	}

	public String requestApprove(String openid, int cent) {
		if(cent < 30 || cent > 10000) {
			return "提现金额不合法，未能提交";
		}
		if(tobeApproved.containsKey(openid)) {
			return "请耐心等待，勿重复提交";
		}
		tobeApproved.put(openid, new KeyValue(openid, "" + cent));
		logger.info("[requestApprove] 用户发起了提现申请(" + cent+"分)");
		return "提现申请成功，等待审核";
	}
	
	public String reward(String reason, String openid, int cent) {
		if(cent < 30 || cent > 10000) {
			return "提现金额不合法，未能提交";
		}
		String time = ""+ System.currentTimeMillis();
		try {
			CallableGet get = new CallableGet("http://localhost:8998/pay?input=%s&sign=%s&time=%s&cent=%s&what=%s", openid, ACrypt.md5(System.getProperty("wx.key") + openid + time + cent + System.getProperty("wx.key")), time, cent, reason);
			String result = get.call();
			logger.info("[reward] openid: " + openid + ", result: " + result);
			atUser(STUPID, new Date().toString() + "提现：" + cent + ",理由：" + reason + "，用户: " + openid + ", 结果: " + result);
			return result;
		} catch (Exception e) {
			logger.error("fail to reward", e);
			return "Fail";
		}
	}
}
