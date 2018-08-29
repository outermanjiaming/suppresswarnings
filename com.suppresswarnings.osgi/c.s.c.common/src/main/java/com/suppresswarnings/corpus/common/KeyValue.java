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
	public KeyValue key(String newKey) {
		this.key = newKey;
		return this;
	}
	public KeyValue value(String newValue) {
		this.value = newValue;
		return this;
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