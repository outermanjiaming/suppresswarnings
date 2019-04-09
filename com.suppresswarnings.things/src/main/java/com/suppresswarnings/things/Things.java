/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.things;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public interface Things {
	String SUCCESS = "success";
	String FAIL = "fail";
	String ERROR = "error";
	interface Config {
		String CODE = "things.code";
	}
	interface Const {
		String HOST = "SuppressWarnings.com";
		Integer PORT = 6617;
		String UTF8 = "UTF-8";
		String CODE_FILE = "suppresswarnings.code";
		String PING_FORAMT = "http://suppresswarnings.com/wx.http?action=ping&type=things&token=%s";
	}
	/**
	 * description of this thing
	 * @return
	 */
	String description();
	/**
	 * the unique code of this thing
	 * get it from 公众号: 素朴网联
	 * 在公众号输入"我要物联网",即可返回code
	 * @return
	 */
	default String code() {
		try {
			return InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			String path = System.getProperty("java.io.tmpdir", ".");
			File code = new File(path, Const.CODE_FILE);
			if(code.exists()) {
				try {
					byte[] bs = Files.readAllBytes(code.toPath());
					return new String(bs, Const.UTF8);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				Random rand = new Random();
				long time = System.currentTimeMillis();
				String create = "T_AIIoT_" + time + "_" + rand.nextInt(9999);
				try {
					Files.write(code.toPath(), create.getBytes(Const.UTF8), StandardOpenOption.CREATE_NEW);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return code();
		}
	}
	
	String exception(String error);
}
