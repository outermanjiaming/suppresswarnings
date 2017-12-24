package com.suppresswarnings.osgi.user;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class User {
	public static final String HEAD = "00000";
	String uid;
	Map<String, String> attributs;
	
	public static void main(String[] args) {
		User user = User.inviteUser("AOC032");
		user.set("nano", "that is this");
		System.out.println(user);
		System.out.println(user.createTime());
		long x = System.currentTimeMillis();
		System.out.println(x);
		System.out.println();
		System.out.println(Long.parseLong("160883d41d4", 16));
		System.out.println("1514114204116");
	}
	
	public void set(KEY key, String value) {
		attributs.put(key.name(), value);
	}
	
	public void set(String key, String value) {
		attributs.put(key, value);
	}
	
	public User() {
	}
	
	public static User newUser(){
		User user = new User();
		user.uid = randomUID();
		user.attributs = new HashMap<String, String>();
		user.set(KEY.UID, user.uid);
		return user;
	}
	
	public static User inviteUser(String invite){
		User user = newUser();
		user.set(KEY.Invited, invite);
		return user;
	} 
	
	public String createTime() {
		String hex = this.uid.substring(HEAD.length());
		long time = Long.parseLong(hex, 16);
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SS");
		return format.format(new Date(time));
	}
	
	public static String randomUID(){
		Random random = new Random();
		DecimalFormat format = new DecimalFormat(HEAD);
		return format.format(random.nextInt(9999)) + Long.toHexString(System.currentTimeMillis());
	}
	
	@Override
	public String toString() {
		return "User [uid=" + uid + ", attributs=" + attributs + "]";
	}
	
}
