package com.suppresswarnings.corpus.service;

import com.suppresswarnings.osgi.network.http.Parameter;

@FunctionalInterface
public interface RequestHandler {
	String handler(Parameter parameter, CorpusService service);
}
