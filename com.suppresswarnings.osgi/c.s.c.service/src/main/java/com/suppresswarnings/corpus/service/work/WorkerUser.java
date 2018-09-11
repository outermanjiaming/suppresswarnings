/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.work;

import com.suppresswarnings.corpus.common.Type;

public class WorkerUser {
	String openId;
	Type type = Type.Reply;
	transient int state = 0;
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public void setFree() {
		this.state = 0;
	}
	public void setBusy() {
		this.state = 1;
	}
	@Override
	public String toString() {
		return "WorkerUser [openId=" + openId + ", state=" + state + "]";
	}
	public boolean isBusy() {
		return this.state == 1;
	}
	
}
