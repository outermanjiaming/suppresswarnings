package com.suppresswarnings.osgi.alone;

import java.util.regex.Pattern;

public class CheckUtil {
	Pattern mailRegex = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
	
	public static boolean anyNull(String...others) {
		if(others == null) return true;
		for(String s : others) if(s == null) return true;
		return false;
	}
}
