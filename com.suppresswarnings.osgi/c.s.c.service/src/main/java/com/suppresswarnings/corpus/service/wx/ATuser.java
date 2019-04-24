/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.wx;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;

public class ATuser implements Runnable {

	transient CorpusService service;
	
	String openid;
	String msg;
	long time;
	
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public ATuser(CorpusService service, String openid, String msg, long time){
		this.service = service;
		this.msg = msg;
		this.openid = openid;
		this.time = time;
	}
	
	@Override
	public void run() {
		service.sendTxtTo("@User", msg, openid);
	}

	public void save() {
		//TODO lijiaming: this key should be used later
		String key = String.join(Const.delimiter, Const.Version.V1, "@User", "Fail", ""+time, "OpenId", openid);
		service.account().put(key, service.toJson(this));
	}
	
	public static void main(String[] args) {
		String openid = "123";
		String code = "xxx";
		String message = "@Override \npublic String code() {\nreturn \""+code+"\";\n}\n//在实现Things接口的代码中增加这些代码";
		String json = "{\"touser\":\"" + openid + "\",\"msgtype\":\"text\",\"text\":{\"content\":\"" + message + "\"}}";
		System.out.println(message);
		System.out.println(json);
		Gson gson = new Gson();
		Map<String, String> map = new HashMap<>();
		map.put("touser", openid);
		map.put("msgtype", "text");
		Map<String, String> msg = new HashMap<>();
		msg.put("content", message);
		map.put("text", gson.toJson(msg));
		String result = gson.toJson(map);
		System.out.println(result);
	}
}
