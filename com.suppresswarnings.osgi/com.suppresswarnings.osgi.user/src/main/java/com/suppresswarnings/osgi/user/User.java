package com.suppresswarnings.osgi.user;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 001:lijiaming:KEY:7:123123
 * 01788160b280ab7c
 * @author lijiaming
 *
 */
public class User {
	public static final String HEAD_UID = "00000";
	public final String uid;
	Map<String, String> attributs = new HashMap<String, String>();
	
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
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("user", user.toString());
		final int NCPU = Runtime.getRuntime().availableProcessors();
		System.out.println(NCPU);
		
		String invite = user.inviteCode();
		System.out.println(invite);
	}
	
	public void set(KEY key, String value) {
		attributs.put(key.name(), value);
	}
	public String get(KEY key) {
		return attributs.get(key.name());
	}
	public void set(String key, String value) {
		attributs.put(key, value);
	}
	public String get(String key) {
		return attributs.get(key);
	}
	
	/**
	 * do not use this
	 */
	@Deprecated
	public User(){
		this.uid = null;
	}
	
	public User(String uid) {
		this.uid = uid;
	}
	
	public static User newUser(){
		User user = new User(randomUID());
		user.set(KEY.UID, user.uid);
		return user;
	}
	public static User oldUser(String uid) {
		if(uid == null) return null;
		User user = new User(uid);
		user.set(KEY.UID, user.uid);
		user.set(KEY.Created, user.createTime());
		return user;
	}
	public static User inviteUser(String invite){
		User user = newUser();
		user.set(KEY.Invited, invite);
		return user;
	} 
	
	public String createTime() {
		String hex = this.uid.substring(HEAD_UID.length());
		long time = Long.parseLong(hex, 16);
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SS");
		return format.format(new Date(time));
	}
	
	public static String randomUID(){
		Random random = new Random();
		DecimalFormat format = new DecimalFormat(HEAD_UID);
		return format.format(random.nextInt(9999)) + Long.toHexString(System.currentTimeMillis());
	}
	
	/**
	 * r(10) + r(char) + HEAD(uid)
	 * @param uid
	 * @return
	 */
	public String inviteCode() {
		StringBuffer inviteCode = new StringBuffer();
		Random random = new Random();
		inviteCode.append(random.nextInt(10));
		inviteCode.append((char) (random.nextInt(26) + 'A'));
		int start = random.nextInt(uid.length() - HEAD_UID.length());
		inviteCode.append(uid.subSequence(start, start + HEAD_UID.length()));
		return inviteCode.toString().toUpperCase();
	}
	
	@Override
	public String toString() {
		return "User:" + attributs;
	}
	
}
