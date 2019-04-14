package com.suppresswarnings.osgi.like.model;

import java.util.List;

public class Page<T> {
	List<T> entries;
	String start;
	String next;
	int size;
	
	public List<T> getEntries() {
		return entries;
	}
	public void setEntries(List<T> entries) {
		this.entries = entries;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getNext() {
		return next;
	}
	public void setNext(String next) {
		this.next = next;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
}
