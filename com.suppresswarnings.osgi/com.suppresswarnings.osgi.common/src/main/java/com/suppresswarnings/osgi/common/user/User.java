package com.suppresswarnings.osgi.common.user;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.suppresswarnings.osgi.common.protocol.KEY;
/**
 * 001:lijiaming:KEY:7:123123
 * 01788160b280ab7c
 * @author lijiaming
 *
 */
public class User {
	public static final String HEAD_UID = "00000";
	public final String uid;
	Map<String, String> attributs = new ConcurrentHashMap<String, String>();
	
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
	
	@Override
	public String toString() {
		return "User [uid=" + uid + ", attributs=" + attributs + "]";
	}
	
}
