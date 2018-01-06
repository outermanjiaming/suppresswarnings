package com.suppresswarnings.osgi.common.user;

public interface AccountService {

	public User login(Login args);
	public void lastLogin(final User user);
	public User register(Register args);
	
}
