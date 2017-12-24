package org.com.suppresswarnings.osgi.ner;

public class Item {

	int id;
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
	public String key(){return key;}
	public String value(){return value;}
	@Override
	public String toString() {
		return key+":"+value;
	}
}
