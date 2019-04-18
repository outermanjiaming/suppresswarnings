package com.suppresswarnings.corpus.service.handlers;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.RequestHandler;
import com.suppresswarnings.osgi.network.http.Parameter;

public class PingHandlerFactory {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static RequestHandler aiit = (param, service, args) ->{
		String key = param.getParameter("token");
		return service.aiiot.ping(key);
	};
	public static String handle(Parameter parameter, CorpusService service) {
		String type = parameter.getParameter("type");
		if("things".equals(type)) {
			return aiit.handler(parameter, service);
		} else {
			return RequestHandler.simple.handler(parameter, service);
		}
	}
}
