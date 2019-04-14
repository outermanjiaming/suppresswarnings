package com.suppresswarnings.osgi.like.impl;

import java.util.ArrayList;
import java.util.List;

import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.osgi.like.LikeHandler;
import com.suppresswarnings.osgi.like.LikeService;
import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Project;

public class LikeHandlerImpl implements LikeHandler {
	LikeService service;
	public LikeHandlerImpl(LikeService service) {
		this.service = service;
	}

	@Override
	public Page<Project> get(String projectid, String openid) {
		Page<Project> page = new Page<Project>();
		List<Project> data = new ArrayList<>();
		Project project = new Project();
		project.setTitle("先赚一个亿，分享点赞一起分红～");
		project.setBonusCent("1000");
		project.setOpenid(openid);
		project.setPictures("http://suppresswarnings.com/download/DFile_29674_1555401702820.jpg,http://suppresswarnings.com/download/DFile_85048_1555401703880.jpg,http://suppresswarnings.com/download/DFile_32655_1555401705673.jpg,http://suppresswarnings.com/download/DFile_13999_1555401706279.jpg");
		project.setTime("1555228857981");
		project.setLikes(getLikes(projectid));
		data.add(project);
		page.setEntries(data);
		page.setNext("Project.1555228857981.oDqlM1TyKpSulfMC2OsZPwhi-9Wk");
		page.setStart(projectid);
		return page;
	}
	
	public Page<KeyValue> getLikes(String projectid) {
		Page<KeyValue> page = new Page<KeyValue>();
		List<KeyValue> data = new ArrayList<>();
		KeyValue kv = new KeyValue("风一样", "http://thirdwx.qlogo.cn/mmopen/iclj2ZoicVicfThpda5COqWAw57KVzOZDeVtgvWGLKib8xIc9hicZOQo5Hxn9LkG3elRDiaaCt8LqXZHWWytNMl5QTOue9omeUGucr/132");
		KeyValue kv2 = new KeyValue("玻利维亚", "http://thirdwx.qlogo.cn/mmopen/6XNMsXhEtvLVv0OfyfwsmEn7JOibSBkh9K5crTJk0bwVhuKQiclMx7TqvI8MuQZ6xFC8gZiavjWp1fsrM70P3NagbeBACb4sYNe/132");
		data.add(kv);
		data.add(kv2);
		page.setEntries(data);
		return page;
	}

}
