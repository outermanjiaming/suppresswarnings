package com.suppresswarnings.osgi.alone;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * according to BCrypt but implement simpler
 * @author lijiaming
 *
 */
public class ACrypt {
	public static final String md5 = "MD5";
	public static final String utf8 = "UTF-8";
	public static final Random rand = new Random();
	public static final String dollar = "$";
	public static final String version = "1";
	public static final int bound = 32;
	public static final int maxSize = 22;
	public static final int minSize = 6;
	public static final String message = "passwd short than 32 and not null";
	public static final String notimpl = "algorithm version not implemented: ";
	public static String md5(String str) {
		try {
			StringBuffer md5StrBuff = new StringBuffer();
			MessageDigest messageDigest = MessageDigest.getInstance(md5);
			messageDigest.reset();
			messageDigest.update(str.getBytes(utf8));
			byte[] byteArray = messageDigest.digest();
			for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
					md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
				} else {
					md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
				}
			}
			return md5StrBuff.toString();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	public static String encrypt(String passwd) {
		if(passwd == null || passwd.length() > bound) {
			throw new RuntimeException(message);
		}
		int length = Math.min(passwd.length(), maxSize);
		length = Math.max(minSize, length);
		
		StringBuffer buff = new StringBuffer();
		while(buff.length() < length) {
			buff.append(Long.toHexString(rand.nextLong()));
		}
		String salt = buff.substring(0, length);
		buff.setLength(0);
		String crypt = md5(salt + passwd);
		buff.append(dollar).append(version).append(dollar).append(salt).append(crypt);
		return buff.toString();
	}
	public static boolean decrypt(String passwd, String hash) {
		if(passwd == null || passwd.length() > bound) {
			throw new RuntimeException(message);
		}
		String v = hash.split('\\' + dollar)[1];
		StringBuffer buff = new StringBuffer();
		if(version.equals(v)) {
			buff.append(dollar).append(v).append(dollar);
			int length = Math.min(passwd.length(), maxSize);
			length = Math.max(minSize, length);
			String salt = hash.substring(buff.length(), buff.length() + length);
			String crypt = md5(salt + passwd);
			buff.append(salt).append(crypt);
		} else {
			throw new RuntimeException(notimpl + v);
		}
		return buff.toString().equals(hash);
	}
	public static void main(String[] args) {
			String hash = encrypt("a");
			System.out.println(hash);
			System.out.println(decrypt("1234", "$1$e9dcf408d86dc4bb38ef64102e1b95279c42db985"));
	}
}
