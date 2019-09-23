package com.suppresswarnings.corpus.service.handlers;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.RequestHandler;
import com.suppresswarnings.osgi.network.http.Parameter;

public class ManagementHandlerFactory {
	static org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static RequestHandler update = (param, service, args) ->{
		String key = param.getParameter("key");
		if(key == null) {
			return "fail";
		}
		String value = param.getParameter("value");
		if(value == null) {
			return "fail";
		}
		String old = service.account().get(key);
		service.account().put(key, value);
		return "success:" + old;
	};
	public static String handle(Parameter parameter, CorpusService service) {
		String random = parameter.getParameter("random");
		if(random == null) {
			return "fail";
		}
		String CODE = parameter.getParameter("ticket");
		if(CODE == null) {
			return "fail";
		}
		String code2OpenIdKey = String.join(Const.delimiter, Const.Version.V1, "To", "OpenId", CODE);
		String openid = service.token().get(code2OpenIdKey);
		if(openid == null) {
			return "fail";
		}
		String expire = service.token().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Admin", "Token", CODE));
		long expired = Long.parseLong(expire);
		if(System.currentTimeMillis() - expired > 0) {
			logger.info(System.currentTimeMillis() + " > " + expired);
			return "fail";
		}
		String what = parameter.getParameter("what");
		if(what == null) {
			return "fail";
		}
		if("update".equals(what)) {
			return update.handler(parameter, service, openid);
		}
		
		return RequestHandler.simple.handler(parameter, service);
	}

}
