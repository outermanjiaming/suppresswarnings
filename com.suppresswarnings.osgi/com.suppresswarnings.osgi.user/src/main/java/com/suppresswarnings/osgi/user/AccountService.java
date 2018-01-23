package com.suppresswarnings.osgi.user;

public interface AccountService {

	/**
	 * 1.check args
	 * 2.check exist
	 * 3.login by username+passcode
	 * @param args
	 * @return
	 */
	public User login(Login args);
	/**
	 * update last login and its count
	 * @param user
	 */
	public void lastLogin(final User user, String additional);
	public User register(Register args);
	/**
	 * User create an invite code by uid
	 * 1.get uid by token( before call this ), check token first
	 * 2.check invite limit by uid
	 * 3.create invite code by uid( set uid-invite, invite-uid )
	 * @param uid
	 * @return invite code
	 */
	public String invite(User user);
	/**
	 * make sure user created first, then it could be invited.
	 * 1.check invite code valid
	 * 2.create user if needed, use invite as username temporarily, it can be changed( set username-changeable )
	 * 3.set uid-invited
	 * @param invite
	 * @param user
	 * @return
	 */
	public String invited(String invite, final User user);
}
