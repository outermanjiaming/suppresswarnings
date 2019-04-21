package com.suppresswarnings.osgi.like.model;

import java.util.List;

import com.suppresswarnings.corpus.common.KeyValue;

public class User {
	String openid;
	String uname;
	String face;
	List<KeyValue> likes;
	List<KeyValue> comments;
	List<KeyValue> projects;
	List<KeyValue> cashouts;
	List<KeyValue> moneys;
	
	
	public List<KeyValue> getMoneys() {
		return moneys;
	}
	public void setMoneys(List<KeyValue> moneys) {
		this.moneys = moneys;
	}
	public List<KeyValue> getComments() {
		return comments;
	}
	public void setComments(List<KeyValue> comments) {
		this.comments = comments;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public List<KeyValue> getLikes() {
		return likes;
	}
	public void setLikes(List<KeyValue> likes) {
		this.likes = likes;
	}
	public List<KeyValue> getProjects() {
		return projects;
	}
	public void setProjects(List<KeyValue> projects) {
		this.projects = projects;
	}
	public List<KeyValue> getCashouts() {
		return cashouts;
	}
	public void setCashouts(List<KeyValue> cashouts) {
		this.cashouts = cashouts;
	}
}
