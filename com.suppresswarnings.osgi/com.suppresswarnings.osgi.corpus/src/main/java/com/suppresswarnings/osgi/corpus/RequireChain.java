package com.suppresswarnings.osgi.corpus;

public abstract class RequireChain {
	public abstract String desc();
	public abstract boolean agree(String value);
	RequireChain next;
	@Override
	public String toString() {
		return "RequireChain [" + desc() + " -> " + next + "]";
	}
	
}
