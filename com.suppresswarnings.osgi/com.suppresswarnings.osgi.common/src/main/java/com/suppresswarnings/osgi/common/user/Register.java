package com.suppresswarnings.osgi.common.user;

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
