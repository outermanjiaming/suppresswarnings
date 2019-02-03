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
}
