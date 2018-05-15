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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.Format;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.service.backup.Server;
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
	Format format = new Format(Const.WXmsg.msgFormat);
	Map<String, TTL> secondlife = new ConcurrentHashMap<String, TTL>();
	LinkedBlockingQueue<TTL> ttl = new LinkedBlockingQueue<TTL>(100000);
	Map<String, Provider<?>> providers = new HashMap<>();
	Map<String, ContextFactory<CorpusService>> factories = new HashMap<>();
	Map<String, Context<?>> contexts = new ConcurrentHashMap<String, Context<?>>();
	LevelDB account, data, token;
	Server backup;
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
		if(provider == null) return new DefaultLevelDB(key);
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
	}

	public void deactivate() {
		logger.info("[corpus] deactivate.");
		if(backup != null) backup.close();
		backup = null;
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
		//TODO clean context
		//TODO send email
		logger.info("[run] unimplemented");
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
			if(msgSignature == null || !msgSignature.equals(sha1)) {
				logger.error("[WX] wrong signature");
				if(openid != null) {
					return xml(openid, Const.WXmsg.reply[0]);
				}
			}
			if(echoStr != null) {
				return echoStr;
			}
			if(openid != null) {
				String sms = parameter.getParameter(Parameter.POST_BODY);
				List<KeyValue> kvs = format.matches(sms);
				KeyValue kv = kvs.get(Const.WXmsg.msgTypeIndex);
				logger.info("[WX] check: " + kv.toString());
				if(!Const.WXmsg.keys[Const.WXmsg.msgTypeIndex].equals(kv.key())) {
					SendMail cn = new SendMail();
					cn.title("notes [WX] msg structure not match", kvs.toString());
					return xml(openid, Const.WXmsg.reply[1]);
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
					KeyValue event = kvs.get(Const.WXmsg.msgTypeIndex + 1);
					if(!"event".equals(event.key())) {
						return xml(openid, Const.WXmsg.reply[1] + Const.WXmsg.types.get(kv.value()));
					}
					if("subscribe".equals(event.value())) {
						return xml(openid, "欢迎你来到素朴网联。");
					}
					if("unsubscribe".equals(event.value())) {
						return xml(openid, "这里永远欢迎你再来。");
					}
				} else {
					return xml(openid, Const.WXmsg.reply[2] + Const.WXmsg.types.get(kv.value()));
				}
				
				Context<?> context = context(openid);
				if(context == null) {
					WXContext ctx = new WXContext(openid, this);
					logger.info("[WX] init context: " + ctx + " for openid: " + openid);
					context = ctx;
					contextx(openid, context, Const.InteractionTTL.userReply);
				}
				logger.info("[WX] use context: " + context + " for openid: " + openid);
				boolean finish = context.test(input);
				if(finish) {
					logger.info("[WX] this stage finished: " + context.state());
				}
				
				return xml(openid, context.output());
			}
		} else if("backup".equals(action)) {
			String from = parameter.getParameter("from");
			String capacity = parameter.getParameter("capacity");
			boolean result = backup.newAgent(from, Long.valueOf(capacity));
			return Boolean.toString(result);
		}
		logger.info("[Corpus] return success for any unknown action " + action + " from " + ip);
		return SUCCESS;
	}

	public String xml(String openid, String msg) {
		long time = System.currentTimeMillis()/1000;
		return String.format(Const.WXmsg.xml, openid, Const.WXmsg.openid, "" + time, msg);
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
