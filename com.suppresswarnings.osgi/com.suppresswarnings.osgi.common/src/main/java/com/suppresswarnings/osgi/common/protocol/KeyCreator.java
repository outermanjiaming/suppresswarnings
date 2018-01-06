package com.suppresswarnings.osgi.common.protocol;

public class KeyCreator {
	public static String format = "%s:%s:%s:%s:%s";
	/**
	 * the format of the key
	 * @param version Version.Vn
	 * @param identity uid or username or other identity
	 * @param e enum
	 * @param complementary index or passcode or other complementary
	 * @return
	 */
	public static String key(String version, String identity, Enum<?> e, String complementary){
		return String.format(format, version, identity, e.getClass().getSimpleName(), e.ordinal(), complementary);
	}
	
	public static String counter(String version, String identity, Enum<?> e){
		return String.format(format, version, identity, e.getClass().getSimpleName(), e.ordinal(), -1);
	}
	
	public static void main(String[] args) {
		System.out.println(key(Version.V1, "022129123sa23d", Event.CHAT, "1"));
		System.out.println(counter("001", "022129123sa23d", Event.CHAT));
		System.out.println(key("001", "lijiaming", KEY.Login, "as893qnd!3"));
		System.out.println(counter("001", "022129123sa23d", KEY.Account));
	}
}
