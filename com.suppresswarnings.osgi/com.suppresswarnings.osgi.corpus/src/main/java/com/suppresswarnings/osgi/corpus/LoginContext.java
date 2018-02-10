package com.suppresswarnings.osgi.corpus;

import com.suppresswarnings.osgi.alone.Context;
import com.suppresswarnings.osgi.alone.State;

/**
 * each active user has a new LoginContext, period
 * @author lijiaming
 *
 */
public class LoginContext extends WXContext {
	String passcode;
	String username;
	boolean auth = false;
	public void loginOK() {
		this.auth = true;
	}
	public boolean auth() {
		return auth;
	} 
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}
	public boolean checkExist(String username) {
		if(content().accountService.exist(username)) return true;
		return false;
	}
	public boolean checkPasswd(String passwd) {
		if(username != null) {
			String uid = content().accountService.login(username, passwd);
			if(uid != null)	{
				return true;
			}
		}
		return false;
	}
	public LoginContext(String openid, WXService ctx, State<Context<WXService>> s) {
		super(openid, ctx, s);
	}
	
	public static void main(String[] args) {
		System.out.println(WXState.mailRegex.matcher("lijiaming@suppresswarnings.com").matches());
	}
}
