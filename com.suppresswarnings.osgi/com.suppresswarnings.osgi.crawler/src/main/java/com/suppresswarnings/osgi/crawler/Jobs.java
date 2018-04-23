/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.crawler;

public class Jobs {
	String name;
	String seed;
	String regex;
	String select;
	public Jobs(String name, String seed, String regex, String select) {
		this.name = name;
		this.seed = seed;
		this.regex = regex;
		this.select = select;
	}
	
	public String getName() {
		return name;
	}
	public String getSeed() {
		return seed;
	}
	public String getRegex() {
		return regex;
	}
	public String getSelect() {
		return select;
	}
	
	@Override
	public String toString() {
		return "Jobs("+name+") [seed=" + seed + ", regex=" + regex + ", select=" + select + "]";
	}
	
}
