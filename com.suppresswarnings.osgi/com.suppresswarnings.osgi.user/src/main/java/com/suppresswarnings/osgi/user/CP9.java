package com.suppresswarnings.osgi.user;

public class CP9 implements ThirdParty {
	String action;
	public CP9(){}
	public CP9(String action){
		this.action = action;
	}

	@Override
	public String token() {
		return "lijiaming";
	}
	
	@Override
	public String call() {
		System.out.println("call: " + action);
		return token() + " called " + action;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CP9 [action=");
		builder.append(action);
		builder.append("]");
		return builder.toString();
	}
}
