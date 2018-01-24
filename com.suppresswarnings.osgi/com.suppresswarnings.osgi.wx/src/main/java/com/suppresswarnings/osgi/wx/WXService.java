package com.suppresswarnings.osgi.wx;

import java.security.MessageDigest;
import java.util.Arrays;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;
import com.suppresswarnings.osgi.user.SendMail;

public class WXService implements HTTPService {
	public static final String name = "wx.http";
	public static final String xml  = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[gh_a1fe05b98706]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[%s]]></Content></xml>";
	public static final String from = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[%s]]></Content><MsgId>%s</MsgId></xml>";
	private static final String[] keys = {"lijiaming2018123", "2a6mVPNhf1iNxJMCXoZUomUrS323MVzsSHkpAn4ZwWp", "wx1f95008283948d0b"};
	private org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String start(Parameter parameter) throws Exception {
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		logger.info("msg from wx: " + parameter.toString());
		String action = parameter.getParameter("action");
		if(("WX").equals(action)){
			logger.info("[wx] IP: "+ip);
		}
		String msgSignature = parameter.getParameter("signature");
		String timestamp = parameter.getParameter("timestamp");
		String nonce = parameter.getParameter("nonce");
		String sha1 = getSHA1(keys[0], timestamp, nonce, "");
		String openid =  parameter.getParameter("openid");
		String echoStr = parameter.getParameter("echostr");
		if(msgSignature == null || !msgSignature.equals(sha1)) {
			logger.error("[wx] wrong signature");
			if(openid != null) return reply(openid, "(fail) I'm glad you're interested in us");
		}
		if(echoStr != null) return echoStr;
		if(openid != null) return reply(openid, "(success) I'm glad you're interested in us");
		return "success";
	}
	public static String reply(String to, String msg) {
		long time = System.currentTimeMillis()/1000;
		return String.format(xml, to, ""+time, msg);
	}
	public static void main(String[] args) {
		SendMail cn = new SendMail();
		cn.title("msg from wx: ", "test");
	}
	
	public String getSHA1(String token, String timestamp, String nonce, String encrypt) {
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
}