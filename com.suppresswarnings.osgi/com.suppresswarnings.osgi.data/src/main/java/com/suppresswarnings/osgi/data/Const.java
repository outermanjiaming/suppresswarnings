package com.suppresswarnings.osgi.data;

import java.util.concurrent.TimeUnit;

public interface Const {
	String delimiter = ".";
	String data = "Data";
	/**
	 * collection or question or classify or reply or produce or unknown
	 * @author lijiaming
	 *
	 */
	public interface TextDataType {
		String collection = "collection";
		String question   = "question";
		String classify   = "classify";
		String reply      = "reply";
		String produce    = "produce";
		String unknown    = "unknown";
	}
	
	public interface InteractionTTL {
		long userReply = TimeUnit.MINUTES.toMillis(20);
		long setThePaper = TimeUnit.MINUTES.toMillis(120);
	}
	
	public interface CounterName {
		String data = "Data";
		String type = "Type";
	}
}
