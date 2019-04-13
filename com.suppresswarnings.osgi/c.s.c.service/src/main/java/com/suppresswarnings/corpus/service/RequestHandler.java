package com.suppresswarnings.corpus.service;

import com.suppresswarnings.osgi.network.http.Parameter;

@FunctionalInterface
public interface RequestHandler {
	RequestHandler simple = (param, serivce) ->{
		return "success";
	};
	String handler(Parameter parameter, CorpusService service);
}
