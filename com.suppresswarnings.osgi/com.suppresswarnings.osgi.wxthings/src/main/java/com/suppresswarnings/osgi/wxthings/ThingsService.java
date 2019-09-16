package com.suppresswarnings.osgi.wxthings;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class ThingsService implements HTTPService {
	Logger logger = LoggerFactory.getLogger("SYSTEM");

	@Override
	public String getName() {
		return "wxthings.http";
	}

	@Override
	public String start(Parameter parameter) throws Exception {
		String signature = parameter.getParameter("signature");
		String echostr = parameter.getParameter("echostr");
		String nonce = parameter.getParameter("nonce");
		String timestamp = parameter.getParameter("timestamp");
		String ip = parameter.getParameter(Parameter.COMMON_KEY_CLIENT_IP);
		String check = getSHA1(Const.WXmsg.secret[0], timestamp, nonce, "", "");
		logger.info(ip + " > wxthings check = " + check + " == " + signature);
		if(Objects.equals(check, signature)) {
			return echostr;
		}
		return "success";
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
	
	public void activate() {
		logger.info("activate = " + this);
	}
	
	public void deactivate() {
		logger.info("deactivate = " + this);
	}
	public void modified() {
		logger.info("modified = " + this);
	}
}
