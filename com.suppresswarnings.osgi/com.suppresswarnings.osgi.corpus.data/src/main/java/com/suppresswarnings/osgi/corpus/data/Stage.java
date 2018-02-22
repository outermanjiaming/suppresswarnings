package com.suppresswarnings.osgi.corpus.data;

public class Stage {
	String key;
	String title;
	String value;
	RequireChain require;
	public Stage(){}
	public Stage(String key, String title) {
		this.key = key;
		this.title = title;
	}
	public boolean agree(){
		if(value == null) {
			return false;
		}
		try {
			RequireChain node = require;
			while(node != null) {
				if(!node.agree(value)) {
					return false;
				}
				node = node.next;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

	public void addRequire(RequireChain require) {
		if(this.require == null) this.require = require;
		else this.require.next = require;
	}

	@Override
	public String toString() {
		return "Stage [key=" + key + ", title=" + title + ", value=" + value + ", require=" + require + "]";
	}
	
}
