package com.suppresswarnings.osgi.like.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.osgi.like.LikeHandler;
import com.suppresswarnings.osgi.like.LikeService;
import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Project;

public class LikeHandlerImpl implements LikeHandler {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	LikeService service;
	public LikeHandlerImpl(LikeService service) {
		this.service = service;
	}

	@Override
	public Page<Project> get(String projectid, String openid) {
		if(projectid == null || projectid.length() < 1) {
			projectid = "Project";
		}
		String head = String.join(Const.delimiter, Const.Version.V1, "Projectid");
		String start = String.join(Const.delimiter, Const.Version.V1, "Projectid", projectid);
		List<String> projectids = new ArrayList<>();
		logger.info("start = " + start);
		service.account().page(head, start, null, 2, (k,v) ->{
			projectids.add(v);
		});
		logger.info("projectids = " + projectids);
		Page<Project> page = new Page<Project>();
		List<Project> data = new ArrayList<>();
		int i = 0;
		do {
			String s = projectids.get(i);
			Project project = new Project();
			project.setProjectid(s);
			String userid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Openid", project.getProjectid()));
			project.setTitle(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Title", project.getProjectid())));
			project.setBonusCent(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "BonusCent", project.getProjectid())));
			project.setOpenid(userid);
			KeyValue kv = service.user(userid);
			project.setUname(kv.key());
			project.setFace(kv.value());
			project.setPictures(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Pictures", project.getProjectid())));
			project.setTime(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Time", project.getProjectid())));
			project.setLikes(getLikes(s));
			data.add(project);
			i++;
		} while(i<projectids.size() - 1);
		
		if(projectids.size() > 1) {
			String s = projectids.get(i);
			page.setNext(s);
		} else {
			page.setNext("null");
		}
		page.setEntries(data);
		page.setStart(start);
		return page;
	}
	
	public Page<KeyValue> getLikes(String projectid) {
		Page<KeyValue> page = new Page<KeyValue>();
		List<KeyValue> data = new ArrayList<>();
		KeyValue kv = new KeyValue("风一样", "http://thirdwx.qlogo.cn/mmopen/iclj2ZoicVicfThpda5COqWAw57KVzOZDeVtgvWGLKib8xIc9hicZOQo5Hxn9LkG3elRDiaaCt8LqXZHWWytNMl5QTOue9omeUGucr/132");
		KeyValue kv2 = new KeyValue("玻利维亚", "http://thirdwx.qlogo.cn/mmopen/6XNMsXhEtvLVv0OfyfwsmEn7JOibSBkh9K5crTJk0bwVhuKQiclMx7TqvI8MuQZ6xFC8gZiavjWp1fsrM70P3NagbeBACb4sYNe/132");
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		data.add(kv);
		data.add(kv2);
		page.setEntries(data);
		return page;
	}

}
