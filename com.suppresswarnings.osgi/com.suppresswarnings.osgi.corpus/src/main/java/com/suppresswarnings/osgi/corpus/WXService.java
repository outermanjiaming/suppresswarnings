package com.suppresswarnings.osgi.corpus;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

import com.suppresswarnings.osgi.alone.CheckUtil;
import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.Format;
import com.suppresswarnings.osgi.alone.Format.KeyValue;
import com.suppresswarnings.osgi.alone.SendMail;
import com.suppresswarnings.osgi.alone.TTL;
import com.suppresswarnings.osgi.alone.Version;
import com.suppresswarnings.osgi.data.Const;
import com.suppresswarnings.osgi.data.DataService;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;
import com.suppresswarnings.osgi.user.AccountService;
import com.suppresswarnings.osgi.user.KEY;
import com.suppresswarnings.osgi.user.TokenService;
import com.suppresswarnings.osgi.user.User;

public class WXService implements HTTPService, Runnable, CommandProvider {
	public static final String SUCCESS = "success";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	long startup = System.currentTimeMillis();
	long initdone = 0;
	Format format = new Format(Const.WXmsg.msgFormat);
	/**
	 * for each openid, we keep a context for a period time to save memory
	 */
	Map<String, Context<?>> contexts = new ConcurrentHashMap<String, Context<?>>();
	/**
	 * in some case, we maybe keep some information for later use, e.g. question(or qid) 
	 */
	Map<String, String> cacheString = new ConcurrentHashMap<String, String>();
	/**
	 * we maybe keep some class bytes here, for instance, State
	 */
	Map<String, byte[]> cacheBytes = new ConcurrentHashMap<String, byte[]>();
	/**
	 * 
	 */
	Map<String, TTL> secondlife = new ConcurrentHashMap<String, TTL>();
	/**
	 * for those Map above, we clear some of the keys according to this ttl list
	 */
	LinkedBlockingQueue<TTL> ttl = new LinkedBlockingQueue<TTL>(100000);
	/**
	 * 1.counter of openid(if not exist, add one and save it)
	 * 2.counter of data{a.counter for all data, b.counter for kind of data, c.counter for question data}
	 * NOTE: user counter do not cache in memory, we'd better not use counter for user, we use timestamp instead
	 */
	Map<String, AtomicInteger> counter = new ConcurrentHashMap<String, AtomicInteger>();
	String datacounter = String.join(Const.delimiter, Version.V1, Const.count, Const.data);
	String usercounter = String.join(Const.delimiter, Version.V1, Const.count, KEY.Account.name());
	String questioncounter = String.join(Const.delimiter, Version.V1, Const.count, Const.data, Const.TextDataType.question);
	String classifycounter = String.join(Const.delimiter, Version.V1, Const.count, Const.data, Const.TextDataType.classify);
	String unknowncounter = String.join(Const.delimiter, Version.V1, Const.count, Const.data, Const.TextDataType.unknown);
	String[] counterKeys = {datacounter,usercounter,questioncounter,classifycounter,unknowncounter};
	/**
	 * 1.persist AtomicInteger Map
	 * 2.run ttl clear
	 * 3.report the status periodic(map size, Min-Max)
	 */
	ScheduledExecutorService schedule = Executors.newScheduledThreadPool(3);
	/**
	 * 1.create account
	 * 2.add information
	 * 3.check uid(no need for WX)
	 * 
	 */
	AccountService accountService;
	TokenService tokenService;
	DataService dataService;
	LevelDB leveldb;
	/**
	 * 0..n
	 */
	Map<String, ContextFactory> factories = new ConcurrentHashMap<String, ContextFactory>();
	
	@Override
	public String getName() {
		return "wx.http";
	}

	public void activate() {
		logger.info("[WX] activate.");
	}

	public void deactivate() {
		logger.info("[WX] deactivate.");
	}

	public void modified() {
		logger.info("[WX] modified.");
	}
	
	public void data(DataService service) {
		logger.info("[WX] init dataService: " + service);
		this.dataService = service;
		this.leveldb = dataService.leveldb();
		initdone |= 1;
		init();
	}
	public void clearData(DataService service) {
		logger.info("[WX] release dataService: msg:" + leveldb + " here:" + this.dataService);
		this.dataService = null;
	}
	public void account(AccountService service) {
		logger.info("[WX] init accountService: " + service);
		this.accountService = service;
		initdone |= 2;
		init();
	}
	public void clearAccount(AccountService service){
		logger.info("[WX] release accountService: msg:" + leveldb + " here:" + this.accountService);
		this.accountService = null;
	}
	public void token(TokenService service) {
		logger.info("[WX] init tokenService: " + service);
		this.tokenService = service;
		initdone |= 4;
		init();
	}
	public void clearToken(TokenService service){
		logger.info("[WX] release tokenService: msg:" + leveldb + " here:" + this.tokenService);
		this.tokenService = null;
	}
	
	public void factory(ContextFactory factory) {
		if(factories.containsKey(factory.command())) {
			logger.warn("[WX] factory exist, replace: " + factory.command());
		} else {
			logger.info("[WX] new factory register: " + factory.command());
		}
		factories.put(factory.command(), factory);
	}
	
	public void clearFactory(ContextFactory factory) {
		boolean removed = factories.remove(factory.command(), factory);
		logger.info("[WX] remove the factory: " + factory.command() + "(" + factory + ") = " + removed);
	}
	
	public ContextFactory command(String command) {
		ContextFactory found =  factories.get(command);
		logger.info("[WX] get ContextFactory by command: " + command + ", " + found);
		return found;
	}
	
	public String register(String openid, String invite) {
		LevelDB db = accountService.leveldb();
		String exist = String.join(Const.delimiter, Version.V1, KEY.Account.name(), openid);
		String uid = db.get(exist);
		if(uid != null) {
			return "用户已经存在了";
		}
		String header = String.join(Const.delimiter, Version.V1, KEY.Account.name());
		AtomicInteger ucounter = counter(db, usercounter, header);
		String key =  header + Const.delimiter + ucounter.getAndIncrement();
		db.put(key, openid);
		db.put(exist, "" + System.currentTimeMillis());
		User user = new User(openid);
		String result = accountService.invited(invite, user);
		if(result == null) {
			return "邀请码无法使用";
		}
		String limitByUid = String.join(Const.delimiter, Version.V1, openid, KEY.Invite.name(), KEY.Limit.name());
		db.put(limitByUid, "3");
		logger.info("[WX] save openid: " + key + " = " + openid);
		return SUCCESS;
	}
	
	public String invite(String openid) {
		User user = new User(openid);
		String invite = accountService.invite(user);
		SendMail mail = new SendMail();
		mail.title("[SuppressWarnings] Invite Code", invite);
		return invite;
	}
	
	public AtomicInteger counter(LevelDB db, String name, String header) {
		AtomicInteger pointer = counter.get(name);
		if(pointer == null) {
			String counterInDB = db.get(name);
			if(counterInDB == null) {
				pointer = new AtomicInteger(1);
			} else {
				pointer = new AtomicInteger(Integer.parseInt(counterInDB));
			}
			
			do{
				int current = pointer.getAndIncrement();
				String check = header + Const.delimiter + current;
				String value = db.get(check);
				logger.info("[WX] check: " + check + " = " + value);
				if(value == null) {
					db.put(name, ""+current);
					break;
				}
			} while(true);
			
			counter.put(name, pointer);
		}
		return pointer;
	}
	@Override
	public String start(Parameter parameter) {
		String action = parameter.getParameter("action");
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		if(!"WX".equals(action)){
			logger.info("[WX] this request is unusual, IP: "+ip);
		}
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
			if(!"MsgType".equals(kv.key())) {
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
			} else {
				return xml(openid, Const.WXmsg.reply[2] + Const.WXmsg.types.get(kv.value()));
			}
			
			Context<?> context = context(openid);
			if(context == null) {
				WXContext ctx = new WXContext(openid, this);
				ctx.init(ctx.init);
				logger.info("[WX] init context: " + ctx + " for openid: " + openid);
				context = ctx;
				contextx(openid, context, Const.InteractionTTL.userReply);
			}
			logger.info("[WX] context: " + context);
			boolean finish = context.test(input);
			if(finish) {
				logger.info("[WX] this stage finished: " + context.state());
			}
				
			return xml(openid, context.output());
		}
		
		
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
	public void bytes(String name, byte[] bytes) {
		cacheBytes.put(name, bytes);
	}
	public void value(String name, String value) {
		cacheString.put(name, value);
	}
	public void bytesx(String name, byte[] bytes, long timeToLiveMillis) {
		expire(name, timeToLiveMillis);
		cacheBytes.put(name, bytes);
	}
	public void valuex(String name, String value, long timeToLiveMillis) {
		expire(name, timeToLiveMillis);
		cacheString.put(name, value);
	}
	public void expire(String name, long timeToLiveMillis) {
		long now = System.currentTimeMillis();
		TTL e = new TTL(now + timeToLiveMillis, name);
		TTL old = secondlife.remove(name);
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
			ttl.offer(e);
		}
	}
	
	public void clear(){
		long now = System.currentTimeMillis();
		logger.info("[content] clean TTL("+ttl.size()+"): " + ttl);
		ttl.removeIf(out -> {
			if(out.ttl() < now) {
				if(out.marked()) {
					logger.info("[content] remove key: " + out.key());
					cacheString.remove(out.key());
					cacheBytes.remove(out.key());
					secondlife.remove(out.key());
					return true;
				} else {
					out.mark();
					secondlife.put(out.key(), out);
				}
			}
			return false;
		});
		logger.info("[content] clean TTL("+ttl.size()+"): " + ttl);
	}
	
	public void init(){
		if(initdone == 7) {
			logger.info("[content] init start.");
			schedule.scheduleWithFixedDelay(this, Const.InteractionTTL.clearCache, Const.InteractionTTL.clearCache, TimeUnit.MILLISECONDS);
		} else {
			logger.info("[content] requirements not met");
		}
	}
	
	@Override
	public void run() {
		long start = System.currentTimeMillis();
		logger.info("[content] run clean start");
		clear();
		logger.info("[content] run clean end: " + (System.currentTimeMillis() - start));
	}

	public static void main(String[] args) {
		int status = 1;
		System.out.println(0+":"+status);
		int s1 = 1<<1;
		status |= s1;
		System.out.println(s1+":"+status);
		int s2 = 1<<2;
		status |= s2;
		System.out.println(s2+":"+status);
		int s3 = 1<<3;
		status |= s3;
		System.out.println(s3+":"+status);
		status |= s3;
		System.out.println(s3+":"+status);
		WXService service = new WXService();
		String xml = service.xml("asasasasasa", "fuckit");
		System.out.println(xml);
	}

	@Override
	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("---SuppressWarnings WXService---\n");
		buffer.append("\t which - which <which> - change to use different LevelDB.\n");
		buffer.append("\t putkv - putkv <key> <value> - put value to that key.\n");
		buffer.append("\t getkv - getkv <key> - get value by that key.\n");
		buffer.append("\t listn - listn <start> <limit> - list some values by start limit.\n");
		return buffer.toString();
	}
	
	public void _which(CommandInterpreter ci) {
		logger.info("[WX Command] which: " + leveldb);
		String arg = ci.nextArgument();
		ci.println("[data account token]: "+ arg);
		if("data".equals(arg)) {
			leveldb = dataService.leveldb();
		} else if("account".equals(arg)) {
			leveldb = accountService.leveldb();
		} else if ("token".equals(arg)) {
			leveldb = tokenService.leveldb();
		} else {
			ci.println("[WX Command] ignore: " + arg);
			if("clear".equals(arg)) {
				ci.println("[WX Command] secret command: " + arg);
				clear();
			}
		}
		ci.println("[WX Command] use " + leveldb);
	}
	
	public void _putkv(CommandInterpreter ci) {
		logger.info("[WX Command] putkv: " + leveldb);
		String key = ci.nextArgument();
		String value = ci.nextArgument();
		int result = leveldb.put(key, value);
		ci.println(result + " put: "+ key + " = " + value);
	}
	
	public void _getkv(CommandInterpreter ci) {
		logger.info("[WX Command] getkv: " + leveldb);
		String key = ci.nextArgument();
		String value = leveldb.get(key);
		ci.println("get: "+ key + " = " + value);
	}
	
	public void _listn(CommandInterpreter ci) {
		logger.info("[WX Command] listn: " + leveldb);
		String start = ci.nextArgument();
		String limit = ci.nextArgument();
		int n = Integer.valueOf(limit);
		leveldb.list(start, n, new BiConsumer<String, String>() {
			int index = 0;
			@Override
			public void accept(String t, String u) {
				index ++;
				ci.println("[WX Command] " + index + ". " + t + " => " + u);
			}
		});
	}
	
}
