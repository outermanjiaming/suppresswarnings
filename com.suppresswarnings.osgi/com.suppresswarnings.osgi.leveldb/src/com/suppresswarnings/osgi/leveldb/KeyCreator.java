package com.suppresswarnings.osgi.leveldb;

public class KeyCreator {
	public static String format = "%s:%s:%s:%s";
	public static String key(String version, String uid, Event e, int id){
		return String.format(format, version, uid, e.ordinal(), id);
	}
	
	public static String counter(String version, String uid, Event e){
		return String.format(format, version, uid, e.ordinal(), Event.COUNT.ordinal());
	}
	
	public static void main(String[] args) {
		System.out.println(key("001", "022129123sa23d", Event.CHAT, 1));
		System.out.println(counter("001", "022129123sa23d", Event.CHAT));
	}
}
