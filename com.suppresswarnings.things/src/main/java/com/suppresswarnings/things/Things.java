/**
 * 
 * Copyright 2019 SuppressWarnings.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.things;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public interface Things {
	String SUCCESS = "success";
	String FAIL = "fail";
	String ERROR = "error";
	String INTERACTIVE = "interactive";
	interface Config {
		String CODE = "things.code";
	}
	interface Const {
		String HOST = "SuppressWarnings.com";
		Integer PORT = 6617;
		String UTF8 = "UTF-8";
		String CODE_FILE = "suppresswarnings.code";
		String DEBUG_SWITCH = "suppresswarnings.debug";
		String PING_FORAMT = "http://suppresswarnings.com/wx.http?action=ping&type=things&token=%s";
		String SHOW_QRCODE = "suppresswarnings.showqrcode";
		String PING_PONG = "ping";
		String QRCODE_FILE = "suppresswarnings.qrcode.jpg";
	}
	/**
	 * description of this thing
	 * @return String 描述
	 */
	String description();
	/**
	 * the unique code of this thing
	 * get it from 公众号: 素朴网联
	 * 在公众号输入"我要物联网",即可返回code
	 * @return String 操作物联网的唯一code
	 */
	default String code() {
		String path = System.getProperty("java.io.tmpdir", ".");
		File code = new File(path, Const.CODE_FILE);
		if(code.exists()) {
			try {
				byte[] bs = Files.readAllBytes(code.toPath());
				return new String(bs, Const.UTF8);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else {
			Random rand = new Random();
			long time = System.currentTimeMillis();
			String create = "T_AIIoT_" + time + "_" + rand.nextInt(99999);
			try {
				Files.write(code.toPath(), create.getBytes(Const.UTF8), StandardOpenOption.CREATE_NEW);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return code();
	}
	
	default void showQRCode(String remoteQRCodeURL, String text) {
		try {
			System.out.println("Please open QR Code Image at " + remoteQRCodeURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	String exception(String error);
}
