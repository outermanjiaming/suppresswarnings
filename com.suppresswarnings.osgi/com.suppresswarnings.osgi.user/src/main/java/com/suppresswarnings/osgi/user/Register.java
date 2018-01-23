package com.suppresswarnings.osgi.user;

public interface Register {

	public String username();
	public String passcode();
	public String confirm();
	/**
	 * for token safely return
	 * @return masked token
	 */
	public String randomask();
}
