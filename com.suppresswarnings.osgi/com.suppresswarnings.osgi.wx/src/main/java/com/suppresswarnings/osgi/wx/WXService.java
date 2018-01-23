package com.suppresswarnings.osgi.wx;

import org.slf4j.LoggerFactory;

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
		SendMail cn = new SendMail();
		cn.title("msg from wx: " + ip, parameter.toString());
		return "success";
	}
	
	public static void main(String[] args) {
		SendMail cn = new SendMail();
		cn.title("msg from wx: ", "test");
	}
	
}
