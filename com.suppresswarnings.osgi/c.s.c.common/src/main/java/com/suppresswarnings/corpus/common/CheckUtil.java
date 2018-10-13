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

import java.lang.reflect.Field;
import java.util.regex.Matcher;
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
		String trim = str.trim();
		String clean = symbolPattern.matcher(trim).replaceAll("");
		if(clean.length() < 1) {
			clean = trim;
		}
		return clean;
	}

	public static boolean hasChinese(String str) {
		String regEx = "[\u4e00-\u9fa5]";
		Pattern pat = Pattern.compile(regEx);
		Matcher matcher = pat.matcher(str);
		boolean flg = false;
		if (matcher.find())
			flg = true;

		return flg;
	}
	
	public static String check(Object obj) {
		if (obj == null) {
			return "null error";
		} else {
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Check checker = field.getAnnotation(Check.class);
				if(checker == null) {
					continue;
				} else if (ValidationType.INTEGER.equals(checker.type())) {
					try{
						Integer val = (Integer) field.get(obj);
						if (checker.nonNull()) {
							if (val == null) {
								return checker.name() + " 参数值不能为null";
							}
						} else {
							if (val == null) {
								continue;
							}
						}
						if (val > checker.max()) {
							return checker.name() + " 值不能大于" + checker.max();
						}
						if (val < checker.min()) {
							return checker.name() + " 值不能小于" + checker.min();
						}
					} catch (Exception e) {
						return "获取对象属性值异常" + e.getMessage();
					}
				} else if (ValidationType.STRING.equals(checker.type())) {
					try{
						String val = (String) field.get(obj);
						if (checker.nonNull()) {
							if (val == null) {
								return checker.name() + " 参数值不能为null";
							}
						} else {
							if (val == null) {
								continue;
							}
						}
						if (val.length() > checker.maxLength()) {
							return checker.name() + " 长度不能大于"+ checker.maxLength();
						}
						if (val.length() < checker.minLength()) {
							return checker.name() + " 长度不能小于"+ checker.minLength();
						}
						if( checker.xss() ) {
						    if(val.contains("<") || val.contains(">")) {
	                            return checker.name() + " 包含非法字符:< >";
	                        }
						}
					} catch (Exception e) {
						return "获取对象属性值异常" + e.getMessage();
					}
				} else {
					System.out.println("unrecognized type : " + checker.type().name());
				}
			}
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println(hasChinese("!@_-我。。%"));
	}
}
