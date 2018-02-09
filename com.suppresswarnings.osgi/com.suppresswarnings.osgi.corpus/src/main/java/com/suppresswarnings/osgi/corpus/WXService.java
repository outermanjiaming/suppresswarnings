package com.suppresswarnings.osgi.corpus;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;
import com.suppresswarnings.osgi.user.AccountService;
import com.suppresswarnings.osgi.user.TokenService;
import com.suppresswarnings.osgi.alone.CheckUtil;
import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.Format;
import com.suppresswarnings.osgi.alone.SendMail;
import com.suppresswarnings.osgi.alone.State;
import com.suppresswarnings.osgi.alone.Format.KeyValue;
import com.suppresswarnings.osgi.data.Const;
import com.suppresswarnings.osgi.data.DataService;
import com.suppresswarnings.osgi.data.ExampleContent;
import com.suppresswarnings.osgi.data.ExampleContext;
import com.suppresswarnings.osgi.data.ExampleState;

public class WXService implements HTTPService {
	public static final String SUCCESS = "success";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Format format = new Format(Const.WXmsg.msgFormat);
	Content ctx = new Content();
	
	AccountService accountService;
	TokenService tokenService;
	DataService dataService;
	
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
	
	public void data(DataService leveldb) {
		logger.info("[WX] init dataService: " + leveldb);
		this.dataService = leveldb;
	}
	public void clearData(DataService leveldb) {
		logger.info("[WX] release dataService: msg:" + leveldb + " here:" + this.dataService);
		this.dataService = null;
	}
	public void account(AccountService leveldb) {
		logger.info("[WX] init accountService: " + leveldb);
		this.accountService = leveldb;
	}
	public void clearAccount(AccountService leveldb){
		logger.info("[WX] release accountService: msg:" + leveldb + " here:" + this.accountService);
		this.accountService = null;
	}
	public void token(TokenService leveldb) {
		logger.info("[WX] init tokenService: " + leveldb);
		this.tokenService = leveldb;
	}
	public void clearToken(TokenService leveldb){
		logger.info("[WX] release tokenService: msg:" + leveldb + " here:" + this.tokenService);
		this.tokenService = null;
	}
	@Override
	public String start(Parameter parameter) throws Exception {
		String action = parameter.getParameter("action");
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		if(!"WX".equals(action)){
			logger.info("[WX] this request is unusual, IP: "+ip);
		}
		
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
			}
			
			Context<?> context = ctx.get(openid);
			if(context == null) {
				State<Context<Content>> state = WXState.init;
				context = new WXContext(ctx, state);
				ctx.put(openid, context);
			}
			boolean finish = context.test(input);
			if(finish) {
				System.out.println("this stage finished: " + context.state());
			} else {
				return xml(openid, context.output());
			}
			
			return xml(openid, Const.WXmsg.reply[2] + Const.WXmsg.types.get(kv.value()));
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
	
	public static void main(String[] args) {
		WXService se = new WXService();
		WXContext a = new WXContext(new Content(), WXState.init);
		ExampleContext b = new ExampleContext(new ExampleContent(), ExampleState.S0);
		
		se.ctx.put("a", a);
		se.ctx.put("b", b);
		
		Context<?> x = se.ctx.get("b");
		boolean y = x.test("final");
		System.out.println(y);
		System.out.println(x);
	}

}
