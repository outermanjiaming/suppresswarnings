package com.suppresswarnings.corpus.service;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.service.aiiot.Things;
import com.suppresswarnings.osgi.network.http.Parameter;

public class PingHandlerFactory {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static RequestHandler simple = (param, serivce) ->{
		return "success";
	};
	static RequestHandler  aiit = (param, service) ->{
		String key = param.getParameter("token");
		return service.aiiot.ping(key);
	};
	public static String handle(Parameter parameter, CorpusService service) {
		String type = parameter.getParameter("type");
		if("things".equals(type)) {
			return aiit.handler(parameter, service);
		} else {
			return simple.handler(parameter, service);
		}
	}
}
