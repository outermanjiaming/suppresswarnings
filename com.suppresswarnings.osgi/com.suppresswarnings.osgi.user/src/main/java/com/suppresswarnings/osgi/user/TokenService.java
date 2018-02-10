package com.suppresswarnings.osgi.user;

import com.suppresswarnings.osgi.leveldb.LevelDB;

public interface TokenService {
	/**
	 * set token by username, set expire by token.
	 * @param user
	 * @return
	 */
	public String create(final User user);
	/**
	 * check token exist, check its ttl
	 * @param token
	 * @return uid:ttl or null
	 */
	public String check(final String token);
	/**
	 * simple check itself
	 * @param token
	 * @return
	 */
	public String valid(final String token);
	public LevelDB leveldb();
	public String token(String openid);
	public int token(String openid, String token);
}
