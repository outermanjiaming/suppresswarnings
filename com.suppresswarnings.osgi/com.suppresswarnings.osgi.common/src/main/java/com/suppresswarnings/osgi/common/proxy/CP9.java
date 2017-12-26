package com.suppresswarnings.osgi.common.proxy;

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

}
