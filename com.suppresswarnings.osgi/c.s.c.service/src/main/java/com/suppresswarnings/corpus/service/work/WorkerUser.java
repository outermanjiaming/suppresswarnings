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

public class WorkerUser {

	String openId;
	transient int state;
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
