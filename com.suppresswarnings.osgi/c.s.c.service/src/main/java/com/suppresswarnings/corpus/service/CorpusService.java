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

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.service.backup.Server;
import com.suppresswarnings.corpus.service.http.CallableGet;
import com.suppresswarnings.corpus.service.http.CallablePost;
import com.suppresswarnings.corpus.service.wx.AccessToken;
import com.suppresswarnings.corpus.service.wx.QRCodeTicket;
import com.suppresswarnings.corpus.service.wx.WXevent;
import com.suppresswarnings.corpus.service.wx.WXtext;
import com.suppresswarnings.corpus.service.wx.WXvoice;
import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.corpus.common.SendMail;
import com.suppresswarnings.corpus.common.TTL;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class CorpusService implements HTTPService, Runnable, CommandProvider {
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
	ScheduledExecutorService scheduler; 
	
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
	public void activate() {
		logger.info("[corpus] activate.");
		backup = new Server();
		try {
			backup.working();
		} catch (Exception e) {
			logger.error("[corpus] server backup fail to start working", e);
		}
		
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(this, TimeUnit.MINUTES.toMillis(3), TimeUnit.MINUTES.toMillis(2), TimeUnit.MILLISECONDS);
		logger.info("[corpus] TTL scheduler starts in 3 minutes");
		scheduler.scheduleAtFixedRate(new Runnable() {
			int times = 0;
			String key = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", "973rozg");
			String expireKey = String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Expire", "973rozg");
			
			@Override
			public void run() {
				try {
					times ++;
					CallableGet get = new CallableGet("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", "wx41b262e9b9d8885e", "e64302221a8a128fad1cbc723abc122d");
					logger.info("[access token] start " + times);
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
		scheduler.scheduleAtFixedRate(new Runnable() {
			int times = 0;
			@Override
			public void run() {
				times ++;
				String accessToken = token().get(String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", "973rozg"));
				String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + accessToken;
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
				info.append("ttl: " + ttl.size()).append("\n");
				info.append("backup：" + backup.toString());
				for(String one : admin) {
					String json = "{\"touser\":\"" + one + "\",\"msgtype\":\"text\",\"text\":{\"content\":\"" + info.toString() + "\"}}";
					CallablePost post = new CallablePost(url, json);
					try {
						String result = post.call();
						logger.info("[corpus] scheduler post result: " + result);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, TimeUnit.MINUTES.toMillis(3), TimeUnit.MINUTES.toMillis(90), TimeUnit.MILLISECONDS);
		logger.info("[corpus] scheduler admins info starts in 3 minutes");
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
		buffer.append("\t listn - listn <start> <limit> - list some values by start limit.\n");
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
	
	@Override
	public void run() {
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
	
	@Override
	public String getName() {
		return "wx.http";
	}
	@Override
	public String start(Parameter parameter) throws Exception {
		// TODO get action to do things
		String action = parameter.getParameter("action");
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		if("WX".equals(action)) {
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
				List<KeyValue> kvs = format.matches(sms);
				KeyValue kv = kvs.get(Const.WXmsg.msgTypeIndex);
				logger.info("[WX] check: " + kv.toString());
				if(!Const.WXmsg.keys[Const.WXmsg.msgTypeIndex].equals(kv.key())) {
					SendMail cn = new SendMail();
					cn.title("notes [WX] msg structure not match", kvs.toString());
					return xml(openid, Const.WXmsg.reply[1], fromOpenId);
				}
				String input = "";
				if("text".equals(kv.value())) {
					WXtext value = new WXtext();
					value.init(kvs);
					input = value.Content;
				} else if("voice".equals(kv.value())) {
					WXvoice value = new WXvoice();
					value.init(kvs);
					input = value.Recognition;
				} else if("event".equals(kv.value())) {
					WXevent event = new WXevent();
					event.init(kvs);
					if("subscribe".equals(event.event)) {
						String subscribeKey = String.join(Const.delimiter, Const.Version.V1, "Subscribe", openid);
						String subscribe = account().get(subscribeKey);
						String time = "" + System.currentTimeMillis();
						
						if(event.eventKey != null) {
							String subscribeEventKey = String.join(Const.delimiter, Const.Version.V1, "Subscribe", "Event", openid, time);
							account().put(subscribeEventKey, event.eventKey);
						}
						
						if(subscribe == null) {
							account().put(subscribeKey, time);
							return xml(openid, "欢迎你来到素朴网联。\n" + event.eventKey, fromOpenId);
						} else {
							String subscribeHistoryKey = String.join(Const.delimiter, Const.Version.V1, openid, "Subscribe", subscribe);
							account().put(subscribeHistoryKey, time);
							account().put(subscribeKey, time);
							if(subscribe.contains("unsubscribe")) {
								return xml(openid, "欢迎再次来到素朴网联。\n" + event.eventKey, fromOpenId);
							} else {
								return xml(openid, "欢迎来到素朴网联。\n" + event.eventKey, fromOpenId);
							}
						}
					} else if("unsubscribe".equals(event.event)) {
						String subscribeKey = String.join(Const.delimiter, Const.Version.V1, "Subscribe", openid);
						String subscribe = account().get(subscribeKey);
						String time = "" + System.currentTimeMillis();
						String subscribeHistoryKey = String.join(Const.delimiter, Const.Version.V1, openid, "Subscribe", subscribe);
						account().put(subscribeHistoryKey, time);
						account().put(subscribeKey, time + Const.delimiter + "unsubscribe");
						return xml(openid, "这里永远欢迎你再来。", fromOpenId);
					} else if("SCAN".equals(event.event)) {
						return xml(openid, "欢迎来到【" + event.eventKey + "】", fromOpenId);
					}
				} else {
					return xml(openid, Const.WXmsg.reply[2] + Const.WXmsg.types.get(kv.value()), fromOpenId);
				}
				
				Context<?> context = context(openid);
				if(context == null) {
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
			int sceneId = 100101;
			String random = parameter.getParameter("random");
			if(random != null) {
				sceneId = sceneId + Integer.parseInt(random);
			}
			String json = "{\"expire_seconds\": 604800, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": " + sceneId + "}}}";
			String accessToken = token().get(String.join(Const.delimiter, Const.Version.V1, "AccessToken", "Token", "973rozg"));
			String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + accessToken;
			CallablePost post = new CallablePost(url, json);
			String result = post.call();
			Gson gson = new Gson();
			QRCodeTicket qrCodeTicket = gson.fromJson(result, QRCodeTicket.class);
			return "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + qrCodeTicket.getTicket();
		}
		logger.info("[Corpus] return success for any unknown action " + action + " from " + ip);
		return SUCCESS;
	}

	public String xml(String openid, String msg, String fromOpenId) {
		long time = System.currentTimeMillis()/1000;
		return String.format(Const.WXmsg.xml, openid, fromOpenId, "" + time, msg);
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
		logger.info("[WX] expire exist for " + name + " = " + old);
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
	
}
