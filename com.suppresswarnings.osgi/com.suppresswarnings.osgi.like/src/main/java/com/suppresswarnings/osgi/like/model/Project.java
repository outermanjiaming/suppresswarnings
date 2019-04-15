package com.suppresswarnings.osgi.like.model;

import com.suppresswarnings.corpus.common.KeyValue;

public class Project {

	String projectid;
	String openid;
	String face;
	String uname;
	String time;
	String title;
	String pictures;
	String bonusCent;
	Page<KeyValue> likes;
	Page<KeyValue> forwards;
	Page<KeyValue> invests;
	
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getProjectid() {
		return projectid;
	}
	public void setProjectid(String projectid) {
		this.projectid = projectid;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPictures() {
		return pictures;
	}
	public void setPictures(String pictures) {
		this.pictures = pictures;
	}
	public String getBonusCent() {
		return bonusCent;
	}
	public void setBonusCent(String bonusCent) {
		this.bonusCent = bonusCent;
	}
	public Page<KeyValue> getLikes() {
		return likes;
	}
	public void setLikes(Page<KeyValue> likes) {
		this.likes = likes;
	}
	public Page<KeyValue> getForwards() {
		return forwards;
	}
	public void setForwards(Page<KeyValue> forwards) {
		this.forwards = forwards;
	}
	public Page<KeyValue> getInvests() {
		return invests;
	}
	public void setInvests(Page<KeyValue> invests) {
		this.invests = invests;
	}
	public void addPicture(String image) {
		if(pictures == null || pictures.length() < 1) {
			pictures = image;
		} else {
			pictures = pictures + "," + image;
		}
	}
	@Override
	public String toString() {
		return "Project [projectid=" + projectid + ", openid=" + openid + ", time=" + time + ", title=" + title
				+ ", pictures=" + pictures + ", bonusCent=" + bonusCent + "]";
	}
	
}
