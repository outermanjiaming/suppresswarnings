package com.suppresswarnings.osgi.notify;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class NotifyService implements HTTPService {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	
	@Override
	public String getName() {
		return "notify.http";
	}

	@Override
	public String start(Parameter parameter) throws Exception {
		String postbody = parameter.getParameter(Parameter.POST_BODY);
		logger.info("postbody = " + postbody);
		return "success";
	}

}
