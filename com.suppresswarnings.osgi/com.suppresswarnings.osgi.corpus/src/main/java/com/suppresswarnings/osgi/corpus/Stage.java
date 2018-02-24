package com.suppresswarnings.osgi.corpus;

public class Stage {
	String key;
	String title;
	String value;
	StringBuffer error = new StringBuffer();
	RequireChain require;
	public Stage(){}
	public Stage(String key, String title) {
		this.key = key;
		this.title = title;
	}
	public boolean agree(){
		error.setLength(0);
		if(value == null) {
			error.append("不能为null");
			return false;
		}
		try {
			RequireChain node = require;
			while(node != null) {
				if(!node.agree(value)) {
					error.append("要求：" + node.desc());
					return false;
				}
				node = node.next;
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.append("检查时异常");
			return false;
		}
		return true;
	}
	public String error(){
		return error.toString();
	}
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public RequireChain getRequire() {
		return require;
	}

	public RequireChain andRequire(RequireChain require) {
		if(this.require == null) {
			this.require = require;
		} else {
			RequireChain node = this.require;
			while(node.next != null) {
				node = node.next;
			}
			node.next = require;
		}
		return require;
	}

	@Override
	public String toString() {
		return "Stage [key=" + key + ", title=" + title + ", value=" + value + ", require=" + require + "]";
	}
	
}
