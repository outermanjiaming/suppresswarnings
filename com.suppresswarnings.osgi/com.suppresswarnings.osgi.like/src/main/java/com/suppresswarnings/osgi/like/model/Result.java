package com.suppresswarnings.osgi.like.model;

import java.util.Map;

public class Result {
	int code;
	String msg;
	Object data;
	Map<String, String> extra;
	
	public Result(Object data) {
		this.code = 0;
		this.msg = "";
		this.data = data;
	}
	
	public Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Map<String, String> getExtra() {
		return extra;
	}

	public void setExtra(Map<String, String> extra) {
		this.extra = extra;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	
}
