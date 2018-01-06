package com.suppresswarnings.osgi.common.user;

public interface Login {

	public String username();
	public String passcode();
	/**
	 * for token safely return
	 * @return masked token
	 */
	public String randomask();
}
