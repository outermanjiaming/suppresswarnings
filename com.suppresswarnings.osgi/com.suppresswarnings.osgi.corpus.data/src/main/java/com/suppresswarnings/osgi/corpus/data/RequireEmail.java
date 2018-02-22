package com.suppresswarnings.osgi.corpus.data;

import java.util.regex.Pattern;

public class RequireEmail extends RequireChain {
	Pattern mailRegex = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
	public static final String desc = "邮件格式";
	@Override
	public String desc() {
		return desc;
	}

	@Override
	public boolean agree(String value) {
		return mailRegex.matcher(value).matches();
	}

}
