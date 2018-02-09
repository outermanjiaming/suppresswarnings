package com.suppresswarnings.osgi.user;

import com.suppresswarnings.osgi.leveldb.LevelDB;

/**
 * key principle:
 * attr: a KEY
 * getwhat: a KEY
 * what: a value by the key
 * value: an argument
 * 0.version:attr:getwhat:args
 * 		e.g. V1:Invite:A03ed4F
 * 1.version:owner:attr
 * 		e.g. V1:lijiaming:Token
 * 2.version:by[attr:...]:getwhat:[args:...], 
 * 		e.g. V1:Account:Passwd:UID:lijiaming:ifsef84ru28394r@fas#as&
 * @author lijiaming
 *
 */
public interface AccountService {

	public boolean exist(String username);
	public String login(String username, String passcode);
	public boolean register(String username, String passcode, String openid);
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
	public LevelDB leveldb();
}
