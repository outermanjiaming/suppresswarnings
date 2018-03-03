package com.suppresswarnings.osgi.alone;

import java.io.Serializable;

public class BlockData implements Serializable {
	/**
	 * for test PersistUtil
	 */
	private static final long serialVersionUID = -2132039562309520360L;
	String key;
	String value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "BlockData [" + key + "=" + value + "]";
	}
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis() + 7000000);
	}
}
