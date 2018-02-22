package com.suppresswarnings.osgi.alone;

public class CheckUtil {
	public static boolean anyNull(String...others) {
		if(others == null) return true;
		for(String s : others) if(s == null) return true;
		return false;
	}
}
