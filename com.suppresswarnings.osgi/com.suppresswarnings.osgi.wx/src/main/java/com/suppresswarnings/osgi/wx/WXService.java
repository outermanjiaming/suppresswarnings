package com.suppresswarnings.osgi.wx;

import org.apache.http.client.fluent.Request;
import org.slf4j.LoggerFactory;

import com.qq.weixin.mp.aes.WXBizMsgCrypt;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;
import com.suppresswarnings.osgi.user.SendMail;

public class WXService implements HTTPService {
	public static final String name = "wx.http";
	
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
		if("token".equals(action)) {
			String appid = parameter.getParameter("appid");
			String secret = parameter.getParameter("secret");
			String identity = System.getProperty("mail.passcode");
			String identify = parameter.getParameter("mail.passcode");
			if(identity.equals(identify)) {
				SendMail cn = new SendMail();
				String result = Request.Get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret=" + secret).execute().returnContent().toString();
				cn.title("msg from wx: " + ip, parameter.toString() + result);
				return "success";
			}
		} else {
			WXBizMsgCrypt pc = new WXBizMsgCrypt("lijiaming2018123", "2a6mVPNhf1iNxJMCXoZUomUrS323MVzsSHkpAn4ZwWp", "wx1f95008283948d0b");
			String msgSignature = parameter.getParameter("signature");
			String timestamp = parameter.getParameter("timestamp");
			String nonce = parameter.getParameter("nonce");
			String echoStr = parameter.getParameter("echostr");
			String echo = pc.verifyUrl(msgSignature, timestamp, nonce, echoStr);
			logger.info("echo: " + echo + " == " + echoStr);
			return echo;
		}
		return "true";
	}
	
	public static void main(String[] args) {
		SendMail cn = new SendMail();
		cn.title("msg from wx: ", "test");
	}
	
}
