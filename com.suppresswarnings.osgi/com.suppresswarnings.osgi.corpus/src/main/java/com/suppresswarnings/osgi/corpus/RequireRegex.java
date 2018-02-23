package com.suppresswarnings.osgi.corpus;

import java.util.regex.Pattern;

public class RequireRegex extends RequireChain {
	String regex;
	Pattern pattern;
	public RequireRegex(){}
	public RequireRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
	}
	@Override
	public String desc() {
		return regex;
	}
	@Override
	public boolean agree(String value) {
		return pattern.matcher(value).matches();
	}
	
}
