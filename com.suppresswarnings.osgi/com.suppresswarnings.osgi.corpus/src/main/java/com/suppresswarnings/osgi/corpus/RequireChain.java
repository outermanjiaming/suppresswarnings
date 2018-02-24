package com.suppresswarnings.osgi.corpus;

public abstract class RequireChain {
	public abstract String desc();
	public abstract boolean agree(String value);
	public RequireChain and(RequireChain next) {
		this.next = next;
		return next;
	}
	RequireChain next;
	@Override
	public String toString() {
		return "RequireChain [" + desc() + " -> " + next + "]";
	}
	
}
