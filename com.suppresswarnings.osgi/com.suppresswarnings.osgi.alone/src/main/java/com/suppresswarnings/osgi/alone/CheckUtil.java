package com.suppresswarnings.osgi.alone;

import java.util.regex.Pattern;

public class CheckUtil {
	public static final String symbolRegex = "[`~!@#$%^&*()+=|{}':;'\",\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
	public static final Pattern symbolPattern = Pattern.compile(symbolRegex);
	public static boolean anyNull(String...others) {
		if(others == null) return true;
		for(String s : others) if(s == null) return true;
		return false;
	}
	
	public static String cleanStr(String str) {
		if(str == null) return null;
		return symbolPattern.matcher(str.trim()).replaceAll("");
	}
	
	public static void main(String[] args) {
		String clean = cleanStr("!@我要答题。。%");
		System.out.println(clean);
	}
}
