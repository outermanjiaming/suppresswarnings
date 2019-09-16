package com.suppresswarnings.corpus.service;

import com.google.gson.Gson;
import com.suppresswarnings.osgi.network.http.Parameter;

@FunctionalInterface
public interface RequestHandler {
	Gson gson = new Gson();
	RequestHandler simple = (param, service, args) ->{
		service.publish("corpus/request/simple", gson.toJson(param));
		return "success";
	};
	String handler(Parameter parameter, CorpusService service, String ... args);
}
