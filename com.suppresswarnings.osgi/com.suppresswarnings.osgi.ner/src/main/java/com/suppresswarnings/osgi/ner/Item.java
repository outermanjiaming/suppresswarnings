package com.suppresswarnings.osgi.ner;

public class Item {

	private int id;
	String key;
	String value;
	
	public Item(){}
	public Item(int i) {
		this.id = i;
	}
	public Item key(String k) {
		this.key = k;
		return this;
	}
	
	public Item value(String v) {
		this.value = v;
		return this;
	}
	public int id(){return id;}
	public String key(){return key;}
	public String value(){return value;}
	@Override
	public String toString() {
		return key+":"+value;
	}
}
