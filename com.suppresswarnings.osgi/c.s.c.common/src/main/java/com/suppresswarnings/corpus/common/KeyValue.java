/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

public class KeyValue {
	String key;
	String value;
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String key() {
		return key;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return key + "=" + value;
	}
}