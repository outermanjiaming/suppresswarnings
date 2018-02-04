package com.suppresswarnings.osgi.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExampleContent {
	private static final Map<String, String> usr_pwd = new ConcurrentHashMap<String, String>();
	public ExampleContent() {
		usr_pwd.put("lijiaming", "123456");
		usr_pwd.put("lidongwei", "passcode");
		usr_pwd.put("zhouenlai", "password");
	}
	public String passcode;
	public String username;
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
		if(usr_pwd.containsKey(username)) return true;
		return false;
	}
	public boolean checkPasswd(String passwd) {
		if(usr_pwd.get(username).equals(passwd)) return true;
		return false;
	}
	@Override
	public String toString() {
		return "ExampleContent [passcode=" + passcode + ", username=" + username + ", auth=" + auth + "]";
	}
}
